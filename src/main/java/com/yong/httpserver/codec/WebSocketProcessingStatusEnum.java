package com.yong.httpserver.codec;

public enum WebSocketProcessingStatusEnum {
    INIT,
    HEAD,
    LENGTH_IN_BYTES,
    MASK,
    MESSAGE,
    DONE;
}
