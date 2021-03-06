package com.yong.httpserver.core;

import com.yong.httpserver.codec.WebSocketParser;
import com.yong.httpserver.codec.WebSocketProcessingStatusEnum;
import com.yong.httpserver.context.Http11ProcessingContext;
import com.yong.httpserver.context.ProcessingContext;
import com.yong.httpserver.context.ProcessingStateEnum;
import com.yong.httpserver.context.WebSocketProcessingContext;
import com.yong.httpserver.web.enums.HttpVersion;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.msg.HeaderBuilder;
import com.yong.httpserver.web.util.ContextHolder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class Http11Acceptor implements Acceptor, CompletionHandler<AsynchronousSocketChannel, Void> {

    private Semaphore connectionSemaphore;

    private AsynchronousServerSocketChannel serverSocketChannel;

    private AsynchronousChannelGroup group;

    private final Queue<Processor> processorQueue = new ConcurrentLinkedQueue<>();

    private final Queue<Processor> wsProcessorQueue = new ConcurrentLinkedQueue<>();

    private ThreadPoolExecutor executor;

    private final List<ContextAdapter> adapterList = new ArrayList<>();

    private String host;

    private int port;

    private int maxConnection;

    private ThreadPoolExecutor acceptorThread;

    public HttpServerBuilder.WebConfig config;


    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void addAdapter(ContextAdapter adapter) {
        adapterList.add(adapter);
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Void attachment) {
        if (connectionSemaphore.availablePermits() > 0) {
            connectionSemaphore.tryAcquire();
        }
        ChannelWrapper channelWrapper = new ChannelWrapper(channel);
        Processor processor = processorQueue.poll();
        if (processor == null) {
            processor = createProcessor();
        }
        channelWrapper.setProcessor(processor);
        channel.read(channelWrapper.getBuffer(), channelWrapper, processor);
        if (connectionSemaphore.availablePermits() <= 0) {
            executor.execute(this);
        } else {
            serverSocketChannel.accept(null, this);
        }
    }

    private Processor createProcessor() {
        return new Http11Processor();
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
        exc.printStackTrace();
    }

    @Override
    public void init() {
        initExecutor();
        initChannel();
        initAdapter();
        initConnectionLimit();
    }

    private void initConnectionLimit() {
        if (maxConnection > 0) {
            connectionSemaphore = new Semaphore(maxConnection);
        } else {
            connectionSemaphore = new Semaphore(1024);
        }
    }

    private void initAdapter() {
        for (ContextAdapter adapter : adapterList) {
            adapter.init();
        }
    }

    private void initExecutor() {
        executor = new ThreadPoolExecutor(16, 64, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        acceptorThread = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    private void initChannel() {
        try {
            group = AsynchronousChannelGroup.withThreadPool(executor);
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(group);
            this.serverSocketChannel.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        acceptorThread.execute(() -> {
            serverSocketChannel.accept(null, this);
            LockSupport.park();
        });
        for (ContextAdapter adapter : adapterList) {
            adapter.start();
        }
    }

    @Override
    public void close() {
        try {
            serverSocketChannel.close();
            acceptorThread.shutdownNow();
            group.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ContextAdapter adapter : adapterList) {
            adapter.close();
        }

    }

    private ContextAdapter getAdapter(ProcessingContext context) {
        for (ContextAdapter adapter : adapterList) {
            if (adapter.support(context)) {
                return adapter;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            connectionSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        serverSocketChannel.accept(null, this);
    }


    private void release() {
        connectionSemaphore.release();
    }

    class Http11Processor extends AbstractProcessor<Http11ProcessingContext> {


        @Override
        public void run() {
        }

        @Override
        public void recycle() {
            super.recycle();
            processorQueue.offer(this);
            release();
        }

        @Override
        public void completed(Integer result, ChannelWrapper channelWrapper) {
            if (result == -1) {
                reset();
                close(channelWrapper);
                return;
            }
            channelWrapper.readyForRead();
            initContext(channelWrapper);
            ContextAdapter adapter = getAdapter(context);
            assert adapter != null;
            ProcessingStateEnum stateEnum = adapter.serve(context);
            if (stateEnum == ProcessingStateEnum.UPGRADING) {
                doUpgrade(context);
                processorQueue.offer(this);
                super.recycle();
                return;
            } else if (stateEnum == ProcessingStateEnum.DONE) {
                reset();
            } else if (stateEnum == ProcessingStateEnum.ERROR) {
                reset();
                close(channelWrapper);
            }
            channelWrapper.continueRead();
        }

        private void doUpgrade(Http11ProcessingContext context) {
            WebSocketProcessor processor = (WebSocketProcessor) wsProcessorQueue.poll();
            ChannelWrapper channelWrapper = context.getChannel();
            String key = context.getHeaders().get("Sec-WebSocket-Key");
            if (processor == null) {
                processor = new WebSocketProcessor();
            }
            processor.channelWrapper = channelWrapper;
            processor.shakeKey = key;
            channelWrapper.setProcessor(processor);
            executor.execute(processor);
        }

        private void close(ChannelWrapper channelWrapper) {
            channelWrapper.close();
        }


        private void initContext(ChannelWrapper channelWrapper) {
            if (context == null) {
                this.context = new Http11ProcessingContext(channelWrapper);
            }
        }

        @Override
        public void failed(Throwable exc, ChannelWrapper attachment) {

        }
    }

    class WebSocketProcessor extends AbstractProcessor<WebSocketProcessingContext> {

        ChannelWrapper channelWrapper;

        String shakeKey;

        private static final String WEBSOCKET_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        WebSocketProcessor() {
        }

        @Override
        public void recycle() {
            super.recycle();
            channelWrapper = null;
            wsProcessorQueue.offer(this);
            release();
        }
        @Override
        public void run() {
            handShake();
            channelWrapper.continueRead();
        }
        private void handShake() {
            String key = hashKey(shakeKey + WEBSOCKET_KEY);
            HeaderBuilder builder = new HeaderBuilder(HttpVersion.HTTP_11, StatusCode.SWITCHING_PROTOCOLS);
            builder.setHeader("Upgrade", "websocket")
                    .setHeader("Connection", "Upgrade")
                    .setHeader("Sec-WebSocket-Accept", key);
            channelWrapper.write(builder.toBuffer());
            propagate(WebSocketParser.FRAME_OPEN);
        }

        private String hashKey(String key) {
            try {
                MessageDigest instance = MessageDigest.getInstance("SHA-1");
                Base64.Encoder encoder = Base64.getEncoder();
                return new String(encoder.encode(instance.digest(key.getBytes(StandardCharsets.UTF_8))));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void completed(Integer result, ChannelWrapper channelWrapper) {
            System.out.println(result);
            if (result == -1) {
                if (channelWrapper.close()) {
                    propagate(WebSocketParser.FRAME_CLOSE);
                }
                return;
            }
            channelWrapper.readyForRead();
            initContext(channelWrapper);
            ContextHolder.set(context);
            ContextAdapter adapter = getAdapter(context);
            assert adapter != null;
            ProcessingStateEnum stateEnum = adapter.serve(context);
            if (stateEnum == ProcessingStateEnum.DONE) {
                reset();
            }
            ContextHolder.clear();
            channelWrapper.continueRead();
        }

        private void propagate(int opt) {
            WebSocketProcessingContext processingContext = new WebSocketProcessingContext(channelWrapper);
            ContextHolder.set(processingContext);
            processingContext.setMessageType(opt);
            ContextAdapter adapter = getAdapter(processingContext);
            if (adapter == null) {
                close(channelWrapper);
                return;
            }
            processingContext.setStatus(WebSocketProcessingStatusEnum.DONE);
            adapter.serve(processingContext);
            ContextHolder.clear();
        }

        private void close(ChannelWrapper channelWrapper) {
            channelWrapper.close();
        }

        private void initContext(ChannelWrapper channelWrapper) {
            if (context == null) {
                context = new WebSocketProcessingContext(channelWrapper);
            }
        }

        @Override
        public void failed(Throwable exc, ChannelWrapper attachment) {
            exc.printStackTrace();
        }
    }
}
