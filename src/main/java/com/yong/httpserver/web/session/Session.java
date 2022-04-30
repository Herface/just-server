package com.yong.httpserver.web.session;

public interface Session {

    Object getValue(String name);

    void invalidate();

    void setValue(String name, Object value);

    boolean valid();

    String getId();

    Session getSession(String id);


}
