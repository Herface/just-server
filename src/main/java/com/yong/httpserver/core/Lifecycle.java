package com.yong.httpserver.core;

public interface Lifecycle {
    default void init() {
    }

    default void start() {
    }

    default void close() {
    }
}
