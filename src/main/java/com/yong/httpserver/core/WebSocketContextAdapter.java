package com.yong.httpserver.core;

import com.yong.httpserver.codec.WebSocketParser;
import com.yong.httpserver.context.ProcessingContext;
import com.yong.httpserver.context.ProcessingStateEnum;
import com.yong.httpserver.context.WebSocketProcessingContext;
import com.yong.httpserver.web.util.ContextHolder;
import com.yong.httpserver.web.ws.DefaultWebSocketEventHandler;
import com.yong.httpserver.web.ws.Message;
import com.yong.httpserver.web.ws.Session;
import com.yong.httpserver.web.ws.WebSocketEventHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.*;

public class WebSocketContextAdapter implements ContextAdapter {

    private WebSocketEventHandler handler = new DefaultWebSocketEventHandler();

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private WebSocketParser parser = new WebSocketParser();


    private int maxMsgSize = 4096;

    private ThreadPoolExecutor executor;

    public WebSocketContextAdapter() {
    }


    public void setMaxMsgSize(int maxMsgSize) {
        parser.setMaxMsgSize(maxMsgSize);
    }


    private void createExecutor() {
        executor = new ThreadPoolExecutor(16, 32, 1, TimeUnit.HOURS,
                new ArrayBlockingQueue<>(200), (r, executor1) -> {
            WebSocketProcessingContext context = ContextHolder.get();
            ChannelWrapper channel = context.getChannel();
            Session session = sessionMap.remove(channel.id);
            if (session != null) {
                handler.onClosed(session);
            }
            close(context);
        });
    }

    public WebSocketEventHandler getHandler() {
        return handler;
    }

    public void setHandler(WebSocketEventHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean support(ProcessingContext context) {
        return context instanceof WebSocketProcessingContext;
    }

    @Override
    public ProcessingStateEnum serve(ProcessingContext context) {
        WebSocketProcessingContext webSocketProcessingContext = (WebSocketProcessingContext) context;
        try {
            if (!webSocketProcessingContext.isDone()) {
                parser.parse(webSocketProcessingContext);
            }
            if (!webSocketProcessingContext.isDone()) {
                return ProcessingStateEnum.CONTINUE;
            }
        } catch (Exception e) {
            close(webSocketProcessingContext);
            return ProcessingStateEnum.DONE;
        }
        executor.execute(new EventTask(webSocketProcessingContext));
        return ProcessingStateEnum.DONE;

    }

    private void close(WebSocketProcessingContext context) {
        ChannelWrapper channel = context.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) (WebSocketParser.MASK_FIN | WebSocketParser.FRAME_CLOSE));
        buffer.put((byte) 0);
        buffer.flip();
        channel.lock();
        try {
            channel.write(buffer);
            channel.close();
            sessionMap.remove(channel.id);
        } finally {
            channel.unlock();
        }

    }

    private class EventTask implements Runnable {

        private final WebSocketProcessingContext context;

        EventTask(WebSocketProcessingContext context) {
            this.context = context;
        }

        @Override
        public void run() {
            ChannelWrapper channel = context.getChannel();
            Session session = sessionMap.get(channel.id);
            if (context.getMessageType() == WebSocketParser.FRAME_CLOSE) {
                if (session != null) {
                    handler.onClosed(session);
                    close(context);
                }
            } else if (context.getMessageType() == WebSocketParser.FRAME_OPEN) {
                session = new Session(channel);
                sessionMap.putIfAbsent(channel.id, session);
                handler.onOpen(session);
            } else {
                Message message = new Message(session, ByteBuffer.wrap(context.getMessageStream().toByteArray()));
                handler.onMessage(message);
            }
        }
    }

    @Override
    public void init() {
        createExecutor();
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
