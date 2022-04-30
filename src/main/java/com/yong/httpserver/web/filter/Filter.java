package com.yong.httpserver.web.filter;

import com.yong.httpserver.context.HttpServeContext;

public interface Filter {
    void doFilter(HttpServeContext context, FilterChain chain);
}
