package com.yong.httpserver.web.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DefaultWebSocketEventHandler implements WebSocketEventHandler {

    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void onOpen(Session session) {
        sessionMap.putIfAbsent(session.id, session);
        System.out.println(session.id + " connected");
    }

    @Override
    public void onMessage(Message message) {
        String string = message.getString();
        Session session = message.session();
        sessionMap.forEach((k, v) -> {
            if (!session.id.equals(k)) {
                v.write(string);
                v.complete();
            }
        });
    }

    @Override
    public void onClosed(Session session) {
        sessionMap.remove(session.id);
        System.out.println(session.id + " closed");
    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
