package com.yong.httpserver.web.enums;

public enum RequestMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    OPTIONS("OPTIONS"),
    HEAD("HEAD"),
    DELETE("DELETE");

    public final String name;

    RequestMethod(String name) {
        this.name = name;
    }

    public static RequestMethod getMethod(String method) {
        for (RequestMethod value : values()) {
            if (value.name.equals(method.toUpperCase())) {
                return value;
            }
        }
        return null;
    }
}
