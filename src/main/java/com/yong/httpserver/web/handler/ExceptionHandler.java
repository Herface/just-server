package com.yong.httpserver.web.handler;

import com.yong.httpserver.context.HttpServeContext;

@FunctionalInterface
public interface ExceptionHandler {
    void handle(HttpServeContext context, Exception e);
}
