package com.yong.httpserver.web.push;

public interface SSEEmitter {
    void emit(String event, String message);

    void emit(String message);

    void complete();
}
