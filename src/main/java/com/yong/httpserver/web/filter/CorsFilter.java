package com.yong.httpserver.web.filter;

import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.enums.StatusCode;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorsFilter implements Filter {

    private String allowOrigin;

    private String allowMethods;

    private String allowHeaders;


    public void setAllowOrigin(String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    public void setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
    }

    public void setAllowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
    }


    @Override
    public void doFilter(HttpServingContext context, FilterChain chain) {
        RequestMethod method = RequestMethod.getMethod(context.getMethod());
        switch (check(context)) {
            case DENIED -> context.setStatusCode(StatusCode.UNAUTHORIZED);
            case YES -> {
                if (method == RequestMethod.OPTIONS) {
                    context.setStatusCode(StatusCode.NO_CONTENT);
                }
                if (!"*".equals(allowOrigin)) {
                    context.setHeader("Vary", "Origin");
                }
                context.setHeader("Access-Control-Allow-Origin", allowOrigin);
                context.setHeader("Access-Control-Allow-Methods", allowMethods);
                context.setHeader("Access-Control-Allow-Headers", allowHeaders);
                chain.next();
            }
            case NO -> chain.next();

        }


    }

    private CorsStatus check(HttpServingContext context) {
        String origin = context.getHeader("Origin");
        RequestMethod method = RequestMethod.getMethod(context.getMethod());
        if (origin == null) {
            return CorsStatus.NO;
        }
        if (checkOrigin(origin) && checkMethod(method) && checkHeader(context)) {
            return CorsStatus.YES;
        }
        return CorsStatus.DENIED;
    }

    private boolean checkHeader(HttpServingContext context) {
        if ("*".equals(allowHeaders)) {
            return true;
        }
        String[] split = allowHeaders.split(",");
        Map<String, String> map = context.getHeaderMap();
        if (split.length != map.size()) {
            return false;
        }
        for (String s : split) {
            if (!map.containsKey(s.trim())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMethod(RequestMethod method) {
        if ("*".equals(allowMethods)) {
            return true;
        }
        String[] split = allowMethods.split(",");
        for (String s : split) {
            if (s.trim().equalsIgnoreCase(method.name)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOrigin(String origin) {
        if ("*".equals(allowOrigin) || origin == null) {
            return true;
        }
        return match(allowOrigin);
    }

    private boolean match(String allowOrigin) {

        int index1 = allowOrigin.indexOf("/");
        int index2 = this.allowOrigin.indexOf("/");

        String protocol = allowOrigin.substring(0, index1);
        String allowedProtocol = this.allowOrigin.substring(0, index2);
        if (!protocol.equalsIgnoreCase(allowedProtocol)) {
            return false;
        }

        String url = allowOrigin.substring(index1 + 2);
        String allowedUrl = this.allowOrigin.substring(index2 + 2);
        String[] strings = allowedUrl.split("\\.");
        StringBuilder builder = new StringBuilder();
        builder.append("^");
        for (String s : strings) {
            if ("*".equals(s)) {
                builder.append(".*");
            } else {
                builder.append(s);
            }
            builder.append("\\.");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
        builder.append("$");
        Pattern pattern = Pattern.compile(builder.toString());
        Matcher matcher = pattern.matcher(url);
        boolean matches = matcher.matches();

        return matches;

    }


    private enum CorsStatus {
        NO,
        YES,
        DENIED
    }

}
