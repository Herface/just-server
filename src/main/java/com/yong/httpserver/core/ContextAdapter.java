package com.yong.httpserver.core;

import com.yong.httpserver.context.ProcessingContext;
import com.yong.httpserver.context.ProcessingStateEnum;


public interface ContextAdapter extends Lifecycle {

    boolean support(ProcessingContext context);

    ProcessingStateEnum serve(ProcessingContext context);
}
