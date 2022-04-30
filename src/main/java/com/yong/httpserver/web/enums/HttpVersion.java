package com.yong.httpserver.web.enums;

public enum HttpVersion {
    HTTP_11("HTTP/1.1"),
    ;
    public final String name;

    HttpVersion(String name) {
        this.name = name;
    }

    public static HttpVersion getVersion(String version) {
        for (HttpVersion value : values()) {
            if (value.name.equalsIgnoreCase(version)) {
                return value;
            }
        }
        return null;

    }
}
