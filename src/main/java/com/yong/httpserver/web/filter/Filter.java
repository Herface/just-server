package com.yong.httpserver.web.filter;

import com.yong.httpserver.context.HttpServingContext;

public interface Filter {
    void doFilter(HttpServingContext context, FilterChain chain);
}
