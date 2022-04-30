package com.yong.httpserver.context;

public interface AsyncContext extends HttpServingContext {
    void complete();
}
