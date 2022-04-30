package com.yong.httpserver.core;

import com.yong.httpserver.context.Context;

public interface Acceptor extends Lifecycle {


    void setHost(String host);

    void setPort(int port);


    void addAdapter(ContextAdapter adapter);
}
