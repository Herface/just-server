package com.yong.httpserver.context;

public interface AsyncContext extends HttpServeContext {
    void complete();
}
