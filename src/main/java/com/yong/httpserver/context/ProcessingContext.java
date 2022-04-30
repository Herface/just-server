package com.yong.httpserver.context;

import com.yong.httpserver.core.ChannelWrapper;


public interface ProcessingContext {


    boolean isDone();


    ChannelWrapper getChannel();


}
