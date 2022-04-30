package com.yong.httpserver.web.session;

import com.yong.httpserver.core.Lifecycle;

public interface SessionManager extends Lifecycle {

    Session createSession();

    Session getSession(String name);


}
