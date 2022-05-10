package com.yong.httpserver.core;

import com.yong.httpserver.web.filter.Filter;
import com.yong.httpserver.web.filter.FilterMapping;
import com.yong.httpserver.web.handler.ExceptionHandler;
import com.yong.httpserver.web.session.SessionManager;
import com.yong.httpserver.web.ws.DefaultWebSocketEventHandler;
import com.yong.httpserver.web.ws.WebSocketEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HttpServerBuilder {

    WebConfig webConfig;

    WebSocketConfig webSocketConfig;

    CorsConfig corsConfig;




    public HttpServerBuilder http(Consumer<WebConfig> consumer) {
        if (webConfig == null) {
            webConfig = new WebConfig();
        }
        consumer.accept(webConfig);
        return this;
    }

    public HttpServerBuilder enableWebSocket(Consumer<WebSocketConfig> consumer) {
        if (webSocketConfig == null) {
            webSocketConfig = new WebSocketConfig();
        }
        consumer.accept(webSocketConfig);
        return this;
    }

    public HttpServerBuilder cors(Consumer<CorsConfig> consumer) {
        if (corsConfig == null) {
            corsConfig = new CorsConfig();
        }
        consumer.accept(corsConfig);
        return this;
    }


    public static class WebConfig {
        WebConfig() {
        }

        CorsConfig corsConfig;
        String host = "0.0.0.0";
        int port = 8080;
        String staticPath = "static";
        int maxUploadFileSize = 1024 * 1024;
        int maxMsgSize = 1024 * 1024;
        List<String> basePackages = new ArrayList<>();
        List<FilterMapping> filterMappings = new ArrayList<>();
        SessionManager sessionManager;
        ExceptionHandler exceptionHandler;
        int maxConnection;

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

        public WebConfig maxConnection(int maxConnection) {
            this.maxConnection = maxConnection;
            return this;
        }

        public WebConfig corsConfig(Consumer<CorsConfig> consumer) {
            if (corsConfig == null) {
                corsConfig = new CorsConfig();
            }
            consumer.accept(corsConfig);
            return this;
        }

    }

    public static class CorsConfig {
        String allowOrigin = "*";
        String allowMethods = "*";
        String allowHeaders = "*";

        public CorsConfig allowOrigin(String allowOrigin) {
            this.allowOrigin = allowOrigin;
            return this;
        }

        public CorsConfig allowMethods(String allowMethods) {
            this.allowMethods = allowMethods;
            return this;
        }


        public CorsConfig allowHeaders(String allowHeaders) {
            this.allowHeaders = allowHeaders;
            return this;
        }


    }

    public static class WebSocketConfig {
        WebSocketConfig() {
        }

        String path = "/ws";
        WebSocketEventHandler eventHandler = new DefaultWebSocketEventHandler();
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
        return new HttpServer(this);
    }



}
