package com.yong.httpserver.web.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSession implements Session {

    private Map<String, Object> attrMap = new ConcurrentHashMap<>();

    private long expireAt;

    private final String id;

    public DefaultSession(long ttl, String id) {
        this.expireAt = System.currentTimeMillis() + ttl;
        this.id = id;
    }

    public DefaultSession() {
        this(3600 * 30, UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
    }

    @Override
    public Object getValue(String name) {
        return attrMap.get(name);
    }

    @Override
    public void invalidate() {
        expireAt = System.currentTimeMillis();
    }

    @Override
    public void setValue(String name, Object value) {
        attrMap.put(name, value);
    }

    @Override
    public boolean valid() {
        return System.currentTimeMillis() < expireAt;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Session getSession(String id) {
        return this;
    }

}
