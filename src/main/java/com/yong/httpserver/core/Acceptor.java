package com.yong.httpserver.core;


public interface Acceptor extends Runnable, Lifecycle {


    void setHost(String host);

    void setPort(int port);


    void addAdapter(ContextAdapter adapter);
}
