package com.yong.httpserver.core;


import java.util.logging.Logger;

/**
 * server
 */
public class HttpServer implements Lifecycle {

    Acceptor acceptor;

    private static final int CREATED = -1;

    private static final int INIT = 0;

    private static final int STARTED = 1;

    private static final int CLOSED = 2;

    private int status = CREATED;

    @Override
    public void init() {
        if (status >= INIT) {
            return;
        }
        acceptor.init();
        registerShutdownHook();
        status = INIT;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }


    @Override
    public void close() {
        if (status >= CLOSED) {
            return;
        }
        Logger logger = Logger.getGlobal();
        logger.warning("closing......");
        acceptor.close();
    }

    @Override
    public void start() {
        if (status >= STARTED) {
            return;
        }
        if (status < INIT) {
            init();
        }
        acceptor.start();
        status = STARTED;
    }


}
