package com.yong.httpserver.web.util;

import com.yong.httpserver.web.enums.StatusCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ResponseUtils {
    public ResponseUtils() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, String> COMMON_HEADER;

    static {
        COMMON_HEADER = Map.of(
                "Server", "uki",
                "Connection", "keep-alive"
        );
    }


    public static ByteBuffer buildResponse(Map<String, String> header, ByteBuffer body) {
        return buildResponse(header, body, StatusCode.OK);
    }

    public static ByteBuffer buildHeaderBuffer(StatusCode statusCode, Map<String, String> header) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(statusCode.code).append(" ").append(statusCode.message).append("\r\n");
        COMMON_HEADER.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        header.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        builder.append("\r\n");
        return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
    }


    public static ByteBuffer buildResponse(Map<String, String> header, ByteBuffer body, StatusCode statusCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(statusCode.code).append(" ").append(statusCode.message).append("\r\n");
        COMMON_HEADER.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        header.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        if (body.capacity() > 0) {
            builder.append("Content-Length: ").append(body.capacity()).append("\r\n");
        }
        builder.append("\r\n");
        try {
            response.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            response.write(body.array());
        } catch (IOException ignore) {
        }
        return ByteBuffer.wrap(response.toByteArray());
    }

    public static ByteBuffer buildDefaultErrorResponse(StatusCode statusCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(statusCode.code).append(" ").append(statusCode.message).append("\r\n");
        COMMON_HEADER.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        builder.append("Content-Type: ").append("text/html\r\n");
        builder.append("Content-Length: ").append(statusCode.message.length()).append("\r\n");
        builder.append("\r\n");
        builder.append(statusCode.message);
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try {
            response.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) {
        }
        return ByteBuffer.wrap(response.toByteArray());
    }

    public static ByteBuffer buildDefaultEmptyResponse(StatusCode statusCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(statusCode.code).append(" ").append(statusCode.message).append("\r\n");
        COMMON_HEADER.forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        builder.append("\r\n");
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try {
            response.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) {
        }
        return ByteBuffer.wrap(response.toByteArray());
    }
}
