package com.yong.httpserver.web.ws;

public interface WebSocketEventHandler {

    void onOpen(Session session);

    void onMessage(Message message);

    void onClosed(Session session);

    void onError(Session session, Throwable throwable);
}
