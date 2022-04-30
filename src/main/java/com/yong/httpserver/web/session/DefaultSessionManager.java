package com.yong.httpserver.web.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class DefaultSessionManager implements SessionManager {


    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private ScheduledThreadPoolExecutor watchDog;


    public DefaultSessionManager() {
    }


    @Override
    public Session createSession() {
        DefaultSession session = new DefaultSession(1000 * 60 * 30, generateId());
        sessionMap.putIfAbsent(session.getId(), session);
        return session;
    }

    private String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    @Override
    public Session getSession(String id) {
        return sessionMap.getOrDefault(id, createSession());
    }

    @Override
    public void start() {
        watchDog = new ScheduledThreadPoolExecutor(1);
        watchDog.scheduleAtFixedRate(() -> {
            sessionMap.forEach((k, v) -> {
                if (!v.valid()) {
                    sessionMap.remove(k);
                }
            });
        }, 30, 30, TimeUnit.SECONDS);
    }
}
