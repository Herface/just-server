package com.yong.httpserver.codec;

import com.yong.httpserver.context.WebSocketProcessingContext;
import com.yong.httpserver.core.ChannelWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WebSocketParser {
    public static final int FRAME_CONTINUATION = 0x0;

    public static final int FRAME_TEXT = 0x1;

    public static final int FRAME_BINARY = 0x1;

    public static final int FRAME_OPEN = 0x666;

    public static final int FRAME_CLOSE = 0x8;

    public static final int FRAME_PING = 0x9;

    public static final int FRAME_PONG = 0xA;

    public static final byte MASK_FIN = (byte) (1 << 7);

    public static final byte MASK_OPCODE = 0xF;

    public static final byte MASK_LENGTH = 0x7F;

    /**
     * 消息长度占16位
     */
    public static final int LENGTH_BITS_16 = 0x7E;

    /**
     * 消息长度占64位
     */
    public static final int LENGTH_BITS_64 = 0x7F;

    private int maxMsgSize;

    public int getMaxMsgSize() {
        return maxMsgSize;
    }

    public void setMaxMsgSize(int maxMsgSize) {
        this.maxMsgSize = maxMsgSize;
    }

    public void parse(WebSocketProcessingContext context) {
        ChannelWrapper channel = context.getChannel();
        ByteBuffer buffer = channel.getBuffer();
        ByteArrayOutputStream stream = context.getMessageStream();

        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            switch (context.getStatus()) {
                case INIT -> {
                    if ((b & MASK_FIN) == MASK_FIN) {
                        // 最后一个报文
                        context.setLast(true);
                    }
                    byte messageType = (byte) (b & MASK_OPCODE);
                    context.setMessageType(messageType);
                    context.setStatus(WebSocketProcessingStatusEnum.HEAD);
                }
                case HEAD -> {
                    int mask = b & MASK_FIN;
                    if (mask == MASK_FIN) {
                        context.setMasked(true);
                    }
                    // 消息长度
                    int length = b & MASK_LENGTH;
                    if (length == LENGTH_BITS_16) {
                        context.setLengthInNBytes(2);
                        context.setStatus(WebSocketProcessingStatusEnum.LENGTH_IN_BYTES);
                    } else if (length == LENGTH_BITS_64) {
                        context.setStatus(WebSocketProcessingStatusEnum.LENGTH_IN_BYTES);
                        context.setLengthInNBytes(8);
                    } else {
                        context.setMessageLength(length);
                        if (context.isMasked()) {
                            context.setStatus(WebSocketProcessingStatusEnum.MASK);
                        } else {
                            context.setStatus(WebSocketProcessingStatusEnum.MESSAGE);
                        }
                    }
                }
                case LENGTH_IN_BYTES -> {
                    // 消息长度
                    long length = context.getMessageLength();
                    long newLength = (length << 8) | Byte.toUnsignedInt(b);
                    if (newLength < length) {
                        throw new RuntimeException("Max message length exceeded");
                    }
                    context.setMessageLength(newLength);
                    // 消息长度剩余字节数
                    int nBytes = context.getLengthInNBytes();
                    nBytes--;
                    if (nBytes <= 0) {
                        if (context.isMasked()) {
                            context.setStatus(WebSocketProcessingStatusEnum.MASK);
                        } else {
                            context.setStatus(WebSocketProcessingStatusEnum.MESSAGE);
                        }
                    }
                    context.setLengthInNBytes(nBytes);
                }
                case MASK -> {
                    int maskCount = context.getMaskByteCount();
                    byte[] maskBytes = context.getMaskBytes();
                    maskBytes[maskCount++] = b;
                    context.setMaskByteCount(maskCount);
                    if (maskCount >= maskBytes.length) {
                        if (context.getMessageLength() == 0) {
                            context.setStatus(WebSocketProcessingStatusEnum.DONE);
                        } else {
                            context.setStatus(WebSocketProcessingStatusEnum.MESSAGE);
                        }
                    }
                }
                case MESSAGE -> {
                    stream.write(b);
                    // 报文解析完毕
                    if (stream.size() >= context.getMessageLength()) {
                        if (context.isLast()) {
                            context.setStatus(WebSocketProcessingStatusEnum.DONE);
                            if (context.isMasked()) {
                                unmask(context);
                            }
                            return;
                        }
                        // 准备第二个报文 一个报文
                        context.setStatus(WebSocketProcessingStatusEnum.INIT);
                    }
                }
            }
        }
        if (stream.size() > maxMsgSize) {
            throw new RuntimeException("Max message size exceeded");
        }
    }

    /**
     * 掩码运算 获取真实消息
     *
     * @param context context
     */
    private void unmask(WebSocketProcessingContext context) {
        ByteArrayOutputStream stream = context.getMessageStream();
        byte[] bytes = stream.toByteArray();
        byte[] maskBytes = context.getMaskBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            b = (byte) (b ^ maskBytes[i % 4]);
            bytes[i] = b;
        }
        stream.reset();
        try {
            stream.write(bytes);
        } catch (IOException ignore) {
        }

    }
}
