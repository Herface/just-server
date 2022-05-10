package com.yong.httpserver.core;


import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface Processor extends Runnable, CompletionHandler<Integer, ChannelWrapper> {
    void recycle();

    void reset();

    ByteBuffer getBuffer();
}
