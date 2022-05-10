package com.yong.httpserver.core;


import com.yong.httpserver.web.dispatcher.HttpRequestDispatcher;
import com.yong.httpserver.web.filter.CorsFilter;
import com.yong.httpserver.web.filter.FilterMapping;
import com.yong.httpserver.web.servlet.ServletMapping;
import com.yong.httpserver.web.session.DefaultSessionManager;
import com.yong.httpserver.web.util.ClassPathServletScanner;
import com.yong.httpserver.web.util.ServletMappingComparator;
import com.yong.httpserver.web.util.ServletScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


/**
 * server
 */
public class HttpServer implements Lifecycle {

    private Acceptor acceptor;

    private HttpServerBuilder builder;

    private static final int CREATED = -1;

    private static final int INIT = 0;

    private static final int STARTED = 1;

    private static final int CLOSED = 2;

    private int status = CREATED;

    HttpServer(HttpServerBuilder builder) {
        this.builder = builder;
    }


    @Override
    public void init() {
        if (status >= INIT) {
            return;
        }
        initAcceptor();
        acceptor.init();
        registerShutdownHook();
        status = INIT;
    }

    private void initAcceptor() {
        acceptor = new Http11Acceptor();
        acceptor.setHost(builder.webConfig.host);
        acceptor.setPort(builder.webConfig.port);
        List<ContextAdapter> adapter = getAdapters();
        for (ContextAdapter contextAdapter : adapter) {
            acceptor.addAdapter(contextAdapter);
        }
    }

    private List<ContextAdapter> getAdapters() {
        List<ContextAdapter> adapterList = new ArrayList<>();
        HttpContextAdapter adapter = new HttpContextAdapter();
        adapter.setDispatcher(getRequestDispatcher());
        adapter.setSessionManager(new DefaultSessionManager());
        adapter.setMaxUploadSize(builder.webConfig.maxUploadFileSize);
        adapter.setMaxMsgSize(builder.webSocketConfig.maxMsgSize);
        adapterList.add(adapter);
        if (builder.webSocketConfig != null) {
            WebSocketContextAdapter webSocketContextAdapter = new WebSocketContextAdapter();
            webSocketContextAdapter.setMaxMsgSize(builder.webSocketConfig.maxMsgSize);
            webSocketContextAdapter.setHandler(builder.webSocketConfig.eventHandler);
            adapter.setWsPath(builder.webSocketConfig.path);
            adapterList.add(webSocketContextAdapter);
        }
        return adapterList;
    }

    private HttpRequestDispatcher getRequestDispatcher() {
        HttpRequestDispatcher dispatcher = new HttpRequestDispatcher();
        ServletScanner scanner = new ClassPathServletScanner();
        Set<ServletMapping> servletMappingSet = new TreeSet<>(new ServletMappingComparator());
        for (String basePackage : builder.webConfig.basePackages) {
            List<ServletMapping> mappings = scanner.scan(basePackage);
            for (ServletMapping mapping : mappings) {
                if (servletMappingSet.contains(mapping)) {
                    throw new RuntimeException("ambiguous request pattern: " + mapping.getPattern());
                }
                servletMappingSet.add(mapping);
            }
        }
        dispatcher.setServletMapping(servletMappingSet);
        dispatcher.setFilterMapping(builder.webConfig.filterMappings);
        dispatcher.setExceptionHandler(builder.webConfig.exceptionHandler);
        dispatcher.setStaticPath(builder.webConfig.staticPath);

        if (builder.corsConfig != null) {
            CorsFilter corsFilter = new CorsFilter();
            corsFilter.setAllowHeaders(builder.corsConfig.allowHeaders);
            corsFilter.setAllowMethods(builder.corsConfig.allowMethods);
            corsFilter.setAllowOrigin(builder.corsConfig.allowOrigin);
            dispatcher.addFilterMapping(new FilterMapping("/**", corsFilter));
        }

        return dispatcher;
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


    public Acceptor getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(Acceptor acceptor) {
        this.acceptor = acceptor;
    }
}
