package com.yong.httpserver.web.filter;

import com.yong.httpserver.context.HttpServeContext;
import com.yong.httpserver.web.servlet.Servlet;

import java.nio.channels.FileChannel;
import java.util.List;

public class DefaultFilterChain implements FilterChain {

    private int nextFilter = -1;

    private final List<Filter> filterList;

    private final Servlet originServlet;

    private final HttpServeContext context;

    public DefaultFilterChain(HttpServeContext context, Servlet originServlet, List<Filter> filterList) {
        this.filterList = filterList;
        this.context = context;
        this.originServlet = originServlet;

    }

    @Override
    public void next() {
        nextFilter++;
        if (nextFilter < filterList.size()) {
            Filter filter = filterList.get(nextFilter);
            filter.doFilter(context, this);
            return;
        }
        originServlet.serve(context);
    }
}
