package com.yong.httpserver.web.dispatcher;

import com.yong.httpserver.context.HttpServeContextInternal;

public interface RequestDispatcher {
    void dispatch(HttpServeContextInternal context);
}
