package com.yong.httpserver.web.handler;

import com.yong.httpserver.context.HttpServingContext;

@FunctionalInterface
public interface ExceptionHandler {
    void handle(HttpServingContext context, Exception e);
}
