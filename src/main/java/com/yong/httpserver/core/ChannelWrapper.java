package com.yong.httpserver.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * wrap underlying socket channel
 */
public class ChannelWrapper {

    public final String id;

    private AsynchronousSocketChannel channel;

    private Processor processor;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final ReentrantLock lock = new ReentrantLock();

    public ByteBuffer getBuffer() {
        return processor.getBuffer();
    }

    public void readyForRead() {
        ByteBuffer buffer = processor.getBuffer();
        if (buffer.position() != 0) {
            buffer.flip();
        }
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public ChannelWrapper(AsynchronousSocketChannel channel) {
        this.channel = channel;
        this.id = UUID.randomUUID().toString();
    }


    /**
     * acquire write lock
     * <br/>
     * a channel may be shared among threads
     */
    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void write(ByteBuffer buffer) {
        try {
            if (!closed.getAcquire()) {
                channel.write(buffer).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        try {
            if (closed.compareAndSet(false, true)) {
                channel.shutdownInput();
                channel.shutdownOutput();
                channel.close();
                processor.recycle();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getRemoteAddress() {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void continueRead() {
        ByteBuffer buffer = getBuffer();
        if (closed.getAcquire()) {
            return;
        }
        if (buffer.position() != 0 && buffer.hasRemaining()) {
            buffer.compact();
        } else {
            buffer.clear();
        }
        channel.read(buffer, this, processor);
    }

}
