package com.yong.httpserver.web.session;

public class SessionFacade implements Session {


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
        return session.getSession(id);
    }
}
