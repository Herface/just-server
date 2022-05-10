package com.yong.httpserver.web.enums;

public enum StatusCode {
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),

    NOT_FOUND(404, "Not Found"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_ERROR(500, "Internal Error"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_MODIFIED(304, "Not Modified"),
    MOVE_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    UNAUTHENTICATED(401, "Unauthenticated"),
    UNAUTHORIZED(403, "Unauthorized"),
    ;
    public final int code;
    public final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
