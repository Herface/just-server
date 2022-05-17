package com.yong.httpserver.web.servlet;

import com.yong.httpserver.context.HttpServingContext;

import java.lang.reflect.Method;

public record ServletMethod(Method method, Object target) implements Servlet {

    @Override
    public void serve(HttpServingContext context) {
        try {
            method.invoke(target, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
