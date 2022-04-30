package com.yong.httpserver.core;


public interface Acceptor extends Lifecycle {


    void setHost(String host);

    void setPort(int port);


    void addAdapter(ContextAdapter adapter);
}
