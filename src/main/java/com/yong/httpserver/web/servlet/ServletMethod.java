package com.yong.httpserver.web.servlet;

import com.yong.httpserver.context.HttpServeContext;

import java.lang.reflect.Method;

public class ServletMethod implements Servlet {

    private Method method;

    private Object target;

    public ServletMethod(Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    @Override
    public void serve(HttpServeContext context) {
        try {
            method.invoke(target, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
