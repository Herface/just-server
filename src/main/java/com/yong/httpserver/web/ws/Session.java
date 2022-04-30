package com.yong.httpserver.web.ws;

import com.yong.httpserver.codec.WebSocketParser;
import com.yong.httpserver.core.ChannelWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Session {

    private ByteArrayOutputStream messageStream = new ByteArrayOutputStream();

    private ChannelWrapper channel;

    public final String id;

    public Session(ChannelWrapper channel) {
        this.channel = channel;
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
    }


    public void write(String msg) {
        try {
            messageStream.write(msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) {
        }
    }

    /**
     * indicate
     */
    public void complete() {
        int size = messageStream.size();
        int sizeLen = 0;
        byte sizeByte;
        if (size > 125 && size <= 0xFFFF) {
            sizeLen = 2;
            sizeByte = 126;
        } else if (size > 0xFFFF) {
            sizeLen = 8;
            sizeByte = 127;
        } else {
            sizeByte = (byte) size;
        }
        ByteBuffer buffer = ByteBuffer.allocate(sizeLen + 2 + size);
        buffer.put((byte) (WebSocketParser.MASK_FIN | WebSocketParser.FRAME_TEXT));
        buffer.put(sizeByte);
        if (sizeLen == 2) {
            buffer.putShort((short) size);
        } else if (sizeLen == 8) {
            buffer.putLong(size);
        }
        buffer.put(messageStream.toByteArray());
        buffer.flip();
        messageStream.reset();
        try {
            channel.lock();
            channel.write(buffer);
        } finally {
            channel.unlock();
        }
    }
}
