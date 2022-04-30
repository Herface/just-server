package com.yong.httpserver.web.session;

public class SessionProxy implements Session {

    private SessionManager sessionManager;


    public SessionProxy(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private Session session;

    @Override
    public Object getValue(String name) {
        return session.getValue(name);
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    @Override
    public void setValue(String name, Object value) {
        session.setValue(name, value);
    }

    @Override
    public boolean valid() {
        return session.valid();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public Session getSession(String id) {
        if (session == null) {
            session = sessionManager.getSession(id);
        }
        return session;
    }


}
