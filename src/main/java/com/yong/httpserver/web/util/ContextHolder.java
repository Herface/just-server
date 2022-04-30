package com.yong.httpserver.web.util;

import com.yong.httpserver.context.ProcessingContext;

public class ContextHolder {

    private static final ThreadLocal<ProcessingContext> contextHolder = new ThreadLocal<>();

    private ContextHolder() {
    }

    public static <T extends ProcessingContext> void set(T context) {
        contextHolder.set(context);
    }


    @SuppressWarnings("unchecked")
    public static <T extends ProcessingContext> T get() {
        return (T) contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }

}
