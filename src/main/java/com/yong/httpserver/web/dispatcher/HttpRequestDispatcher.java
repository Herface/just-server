package com.yong.httpserver.web.dispatcher;

import com.yong.httpserver.context.HttpServeContextFacade;
import com.yong.httpserver.context.HttpServeContextInternal;
import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.filter.DefaultFilterChain;
import com.yong.httpserver.web.filter.Filter;
import com.yong.httpserver.web.filter.FilterChain;
import com.yong.httpserver.web.filter.FilterMapping;
import com.yong.httpserver.web.handler.ExceptionHandler;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.servlet.DefaultServlet;
import com.yong.httpserver.web.servlet.Servlet;
import com.yong.httpserver.web.servlet.ServletMapping;
import com.yong.httpserver.web.util.AntPathMatcher;
import com.yong.httpserver.web.util.FilterMappingComparator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 请求分发
 * <br/>
 * 静态资源
 */
public class HttpRequestDispatcher implements RequestDispatcher {

    private List<FilterMapping> filterMappings;

    private Set<ServletMapping> servletMappings;

    private ExceptionHandler exceptionHandler;

    private final DefaultServlet defaultServlet = new DefaultServlet();

    private final AntPathMatcher matcher = new AntPathMatcher();

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void setStaticPath(String staticPath) {
        defaultServlet.setStaticPath(staticPath);
    }

    public void dispatch(HttpServeContextInternal context) {
        String path = context.getPath();
        String method = context.getMethod();
        RequestMethod requestMethod = RequestMethod.getMethod(method);
        List<ServletMapping> mappings = getServletMapping(path);
        ServletMapping mapping = mappings.stream()
                .filter(servletMapping -> servletMapping.getSupportedMethods()
                        .contains(requestMethod)).findFirst().orElse(null);
        if (mapping == null) {
            if (requestMethod != RequestMethod.GET) {
                methodNotAllowedError(context);
                return;
            }
            defaultServlet.serve(context);
            return;
        }
        context.setPathVarMap(matcher.extractUriTemplateVariables(mapping.getPattern(), path));
        HttpServeContextFacade facade = new HttpServeContextFacade(context);
        try {
            obtainFilterChain(facade, mapping.getServlet()).next();
        } catch (Exception e) {
            try {
                ByteArrayOutputStream outputStream = context.getBodyStream();
                outputStream.reset();
                if (exceptionHandler != null) {
                    exceptionHandler.handle(facade, e);
                } else {
                    fallback(context, e);
                }
            } catch (Exception ex) {
                fallback(context, ex);
            }
        }
    }

    public void setServletMapping(Set<ServletMapping> mappings) {
        this.servletMappings = mappings;
    }

    public void setFilterMapping(List<FilterMapping> mappings) {
        mappings.sort(new FilterMappingComparator());
        this.filterMappings = mappings;
    }

    private void fallback(HttpServeContextInternal context, Exception e) {
        ByteArrayOutputStream outputStream = context.getBodyStream();
        outputStream.reset();
        PrintStream printStream = new PrintStream(outputStream);
        e.printStackTrace(printStream);
        context.setStatusCode(StatusCode.INTERNAL_ERROR);
        context.setContentType(MimeType.TEXT_PLAIN);
        context.write(ByteBuffer.wrap(outputStream.toByteArray()));
    }

    //    private void forward(String forwardUrl, HttpServeContextInternal context) {
//        ServletMapping mapping = getServletMapping(forwardUrl);
//        if (mapping == null) {
//            defaultServlet.serve(context);
//            return;
//        }
//        if (!mapping.getSupportedMethods().contains(RequestMethod.getMethod(context.getMethod()))) {
//            methodNotAllowedError(context);
//            return;
//        }
//        obtainFilterChain(context,mapping.getServlet()).next();
//    }
    private FilterChain obtainFilterChain(HttpServingContext context, Servlet servlet) {
        String path = context.getPath();
        List<Filter> filters = getFilters(path);
        return new DefaultFilterChain(context, servlet, filters);
    }

    private List<Filter> getFilters(String path) {
        return filterMappings.stream().filter(f -> matcher.match(f.pattern(), path))
                .map(FilterMapping::filter).collect(Collectors.toList());
    }

    private void methodNotAllowedError(HttpServeContextInternal context) {
        context.setStatusCode(StatusCode.METHOD_NOT_ALLOWED);
    }

    private List<ServletMapping> getServletMapping(String path) {
        return servletMappings.stream()
                .filter(s -> matcher.match(s.getPattern(), path))
                .collect(Collectors.toList());
    }
}
