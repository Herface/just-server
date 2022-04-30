package com.yong.httpserver.context;

import com.yong.httpserver.codec.WebSocketProcessingStatusEnum;
import com.yong.httpserver.core.ChannelWrapper;

import java.io.ByteArrayOutputStream;

public class WebSocketProcessingContext implements ProcessingContext {


    private ChannelWrapper channelWrapper;

    private ByteArrayOutputStream messageStream = new ByteArrayOutputStream();

    private boolean last;

    private boolean masked;

    private WebSocketProcessingStatusEnum status = WebSocketProcessingStatusEnum.INIT;

    private int messageType;

    private long messageLength;

    private byte[] maskBytes = new byte[4];


    private int lengthInNBytes;


    private int maskByteCount = 0;

    public int getMaskByteCount() {
        return maskByteCount;
    }

    public void setMaskByteCount(int maskByteCount) {
        this.maskByteCount = maskByteCount;
    }

    public WebSocketProcessingStatusEnum getStatus() {
        return status;
    }

    public void setStatus(WebSocketProcessingStatusEnum status) {
        this.status = status;
    }

    public int getLengthInNBytes() {
        return lengthInNBytes;
    }

    public void setLengthInNBytes(int lengthInNBytes) {
        this.lengthInNBytes = lengthInNBytes;
    }

    public boolean isMasked() {
        return masked;
    }

    public void setMasked(boolean masked) {
        this.masked = masked;
    }


    public void setChannelWrapper(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    public ByteArrayOutputStream getMessageStream() {
        return messageStream;
    }

    public void setMessageStream(ByteArrayOutputStream messageStream) {
        this.messageStream = messageStream;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public long getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(long messageLength) {
        this.messageLength = messageLength;
    }

    public byte[] getMaskBytes() {
        return maskBytes;
    }

    public void setMaskBytes(byte[] maskBytes) {
        this.maskBytes = maskBytes;
    }

    public WebSocketProcessingContext(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }


    @Override
    public boolean isDone() {
        return status == WebSocketProcessingStatusEnum.DONE;
    }

    @Override
    public ChannelWrapper getChannel() {
        return channelWrapper;
    }
}
