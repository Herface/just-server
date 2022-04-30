package com.yong.httpserver.web.filter;

import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.web.servlet.Servlet;

/**
 * translate exception to error message
 * always be invoked last
 *
 * @author Hes
 */
public class ExceptionTranslationFilter implements Filter {
    private Servlet exceptionHandler;

    @Override
    public void doFilter(HttpServingContext context, FilterChain chain) {
        try {
            chain.next();
        } catch (Throwable e) {

        }
    }
}
