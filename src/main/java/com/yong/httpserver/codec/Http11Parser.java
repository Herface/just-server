package com.yong.httpserver.codec;

import com.yong.httpserver.context.Http11ProcessingContext;
import com.yong.httpserver.core.ChannelWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Http11Parser {

    private int maxMsgSize;


    public Http11Parser() {
        this(1024 * 1024);
    }

    public Http11Parser(int maxMsgSize) {
        this.maxMsgSize = maxMsgSize;
    }

    public void setMaxMsgSize(int maxMsgSize) {
        this.maxMsgSize = maxMsgSize;
    }

    public void parse(Http11ProcessingContext context) {
        if (!context.isHeaderOk()) {
            readHeader(context);
        }
        if (context.isHeaderOk()) {
            context.setHeaders(parseHeader(context));
        }
        if (context.hasBody()) {
            readBody(context);
        } else {
            context.setBodyOk(true);
        }
    }

    public void readHeader(Http11ProcessingContext context) {
        ChannelWrapper channel = context.getChannel();
        ByteBuffer buffer = channel.getBuffer();
        ByteArrayOutputStream stream = context.headerStream;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            Http11ProcessingStateEnum state = context.getCurrentState();
            switch (state) {
                case INIT -> {
                    if (b == '\r' || b == '\n') {
                        throw new RuntimeException();
                    }
                    context.setCurrentState(Http11ProcessingStateEnum.CHAR);
                }
                case CHAR -> {
                    if (b == '\r') {
                        context.setCurrentState(Http11ProcessingStateEnum.CR);
                    }
                    if (b == '\n') {
                        throw new RuntimeException();
                    }
                }
                case CR -> {
                    if (b == '\n') {
                        context.setCurrentState(Http11ProcessingStateEnum.LF);
                        break;
                    }
                    throw new RuntimeException();
                }
                case LF -> {
                    if (b == '\n') {
                        throw new RuntimeException();
                    }
                    if (b == '\r') {
                        context.setCurrentState(Http11ProcessingStateEnum.END_CR);
                    } else {
                        context.setCurrentState(Http11ProcessingStateEnum.CHAR);
                    }
                }
                case END_CR -> {
                    if (b != '\n') {
                        throw new RuntimeException();
                    }
                    context.setCurrentState(Http11ProcessingStateEnum.END_LF);
                    context.setHeaderOk(true);
                    stream.write(b);
                    return;
                }
            }
            stream.write(b);
            if (stream.size() > maxMsgSize) {
                throw new RuntimeException("Max size succeed！");
            }
        }
    }


    private Map<String, String> parseHeader(Http11ProcessingContext context) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(context.headerStream.toByteArray()));
        Map<String, String> headers = new HashMap<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if ("".equals(line)) {
                continue;
            }
            if (context.getMethod() == null) {
                String[] split = line.split(" ");
                if (split.length < 3) {
                    throw new RuntimeException();
                }
                String method = split[0].trim();
                String path = split[1].trim();
                String version = split[2].trim();
                context.setMethod(method);
                context.setPath(path);
                context.setVersion(version);
            } else {
                String[] split = line.split(":", 2);
                if (split.length < 2) {
                    continue;
                }
                String key = split[0].trim(), value = split[1].trim();
                if ("Content-Length".equalsIgnoreCase(key)) {
                    headers.put("Content-Length", value);
                    try {
                        context.setBodySize(Integer.parseInt(value));
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                } else if ("Content-Type".equalsIgnoreCase(key)) {
                    headers.put("Content-Type", value);
                    context.setContentType(value);
                } else {
                    headers.put(key, value);
                }
            }
        }
        return headers;
    }

    private void readBody(Http11ProcessingContext context) {
        ByteBuffer buffer = context.getChannel().getBuffer();
        ByteArrayOutputStream stream = context.bodyStream;
        byte[] buf = new byte[4096];
        while (buffer.hasRemaining()) {
            if (stream.size() >= context.getBodySize()) {
                context.setBodyOk(true);
                break;
            }
            int start = buffer.position();
            buffer.get(buf, 0, buffer.remaining());
            stream.write(buf, 0, buffer.position() - start);
            if (stream.size() > maxMsgSize) {
                throw new RuntimeException("Max size succeed！");
            }
        }
        if (stream.size() >= context.getBodySize()) {
            context.setBodyOk(true);
        }

    }
}
