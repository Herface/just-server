package com.yong.httpserver.codec;

/**
 * websocket报文解析状态
 */
public enum WebSocketProcessingStatusEnum {
    /**
     * 初始状态
     */
    INIT,
    /**
     * 读取消息头
     */
    HEAD,
    /**
     * 读取消息长度
     */
    LENGTH_IN_BYTES,
    /**
     * 读取掩码
     */
    MASK,
    /**
     * 消息
     */
    MESSAGE,
    /**
     * 解析完成
     */
    DONE;
}
