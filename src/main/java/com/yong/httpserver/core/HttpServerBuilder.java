package com.yong.httpserver.core;

import com.yong.httpserver.web.filter.Filter;
import com.yong.httpserver.web.filter.FilterMapping;
import com.yong.httpserver.web.handler.ExceptionHandler;
import com.yong.httpserver.web.session.SessionManager;
import com.yong.httpserver.web.ws.WebSocketEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HttpServerBuilder {

    private WebConfig webConfig;

    private WebSocketConfig webSocketConfig;

    private HttpServer server;

    private Acceptor acceptor;


    public HttpServerBuilder webConfig(Consumer<WebConfig> consumer) {
        if (webConfig == null) {
            webConfig = new WebConfig();
        }
        consumer.accept(webConfig);
        return this;
    }

    public void enableWebSocket(Consumer<WebSocketConfig> consumer) {
        if (webSocketConfig == null) {
            webSocketConfig = new WebSocketConfig();
        }
        consumer.accept(webSocketConfig);
    }

    public static class WebConfig {
        WebConfig() {
        }

        String host = "0.0.0.0";
        int port = 8080;
        String staticPath = "static";
        int maxUploadFileSize = 1024 * 1024;
        int maxMsgSize = 1024 * 1024;
        List<String> basePackages = new ArrayList<>();
        List<FilterMapping> filterMappings = new ArrayList<>();
        SessionManager sessionManager;
        ExceptionHandler exceptionHandler;

        public WebConfig host(String host) {
            this.host = host;
            return this;
        }

        public WebConfig port(int port) {
            this.port = port;
            return this;
        }

        public WebConfig basePackage(String basePackage) {
            this.basePackages.add(basePackage);
            return this;
        }

        public WebConfig addFilter(String pattern, Filter filter) {
            this.filterMappings.add(new FilterMapping(pattern, filter));
            return this;
        }

        public WebConfig staticPath(String staticPath) {
            this.staticPath = staticPath;
            return this;
        }

        public WebConfig maxUploadFileSize(int maxUploadFileSize) {
            this.maxUploadFileSize = maxUploadFileSize;
            return this;
        }

        public WebConfig maxMsgSize(int maxMsgSize) {
            this.maxMsgSize = maxMsgSize;
            return this;
        }

        public WebConfig errorHanding(ExceptionHandler handler) {
            exceptionHandler = handler;
            return this;
        }

        public WebConfig sessionManager(ExceptionHandler handler) {
            exceptionHandler = handler;
            return this;
        }
    }

    public static class WebSocketConfig {
        WebSocketConfig() {
        }

        String path = "/ws";
        WebSocketEventHandler eventHandler;
        int maxMsgSize = 4096;

        public WebSocketConfig path(String path) {
            this.path = path;
            return this;
        }

        public WebSocketConfig eventHandler(WebSocketEventHandler eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        public WebSocketConfig maxMsgSize(int maxMsgSize) {
            this.maxMsgSize = maxMsgSize;
            return this;
        }
    }

    public HttpServer build() {
        server = new HttpServer();
        buildAcceptor();
        buildWebSocket();
        return server;
    }

    private void buildWebSocket() {
        if (webSocketConfig == null) {
            return;
        }
        WebSocketContextAdapter contextAdapter = new WebSocketContextAdapter();
        contextAdapter.setHandler(webSocketConfig.eventHandler);
        contextAdapter.setMaxMsgSize(webSocketConfig.maxMsgSize);
        acceptor.addAdapter(contextAdapter);
    }

    private void buildAcceptor() {
        acceptor = new Http11Acceptor();
        acceptor.setHost(webConfig.host);
        acceptor.setPort(webConfig.port);
        buildHttpAdapter();
        server.acceptor = acceptor;
    }

    private void buildHttpAdapter() {
        HttpContextAdapter adapter = new HttpContextAdapter();
        if (webSocketConfig != null) {
            adapter.setWsPath(webSocketConfig.path);
        }
        adapter.setBaseServletPackages(webConfig.basePackages);
        adapter.setExceptionHandler(webConfig.exceptionHandler);
        adapter.setFilterMappings(webConfig.filterMappings);
        adapter.setMaxMsgSize(webConfig.maxMsgSize);
        adapter.setMaxUploadSize(webConfig.maxUploadFileSize);
        adapter.setSessionManager(webConfig.sessionManager);
        adapter.setStaticPath(webConfig.staticPath);
        acceptor.addAdapter(adapter);
    }
}
