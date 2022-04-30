package com.yong.httpserver.web.ws;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record Message(Session session, ByteBuffer buffer) {

    public String getString() {
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public ByteBuffer getRaw() {
        return buffer;
    }


}
