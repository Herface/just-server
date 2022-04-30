package com.yong.httpserver.web.servlet;

import com.yong.httpserver.context.HttpServingContext;

@FunctionalInterface
public interface Servlet {
    void serve(HttpServingContext context);
}
