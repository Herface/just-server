package com.yong.httpserver.core;

import com.yong.httpserver.context.ProcessingContext;

import java.nio.ByteBuffer;

public abstract class AbstractProcessor<T extends ProcessingContext> implements Processor {

    protected T context;

    protected ByteBuffer buffer;

    public AbstractProcessor() {
        buffer = ByteBuffer.allocateDirect(4096);
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void reset() {
        context = null;
    }

    @Override
    public void recycle() {
        reset();
        buffer.clear();
    }
}
