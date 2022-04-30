package com.yong.httpserver.web.session;

/**
 * stateless
 */
public class StatelessSessionManager implements SessionManager {
    @Override
    public Session createSession() {
        return null;
    }

    @Override
    public Session getSession(String name) {
        return null;
    }
}
