package com.yong.httpserver.web.servlet;

import com.yong.httpserver.context.HttpServeContext;

@FunctionalInterface
public interface Servlet {
    void serve(HttpServeContext context);
}
