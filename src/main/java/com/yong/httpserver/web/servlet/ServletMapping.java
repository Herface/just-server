package com.yong.httpserver.web.servlet;

import com.yong.httpserver.web.enums.RequestMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class ServletMapping {
    private String pattern;
    private Servlet servlet;
    private Set<RequestMethod> supportedMethods;

    public ServletMapping(String pattern, Servlet servlet, Set<RequestMethod> supportedMethods) {
        this.pattern = pattern;
        this.servlet = servlet;
        this.supportedMethods = supportedMethods;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public Set<RequestMethod> getSupportedMethods() {
        return supportedMethods;
    }

    public void setSupportedMethods(Set<RequestMethod> supportedMethods) {
        this.supportedMethods = supportedMethods;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServletMapping mapping)) return false;
        return getPattern().equals(mapping.getPattern()) && getSupportedMethods().equals(mapping.getSupportedMethods());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPattern(), Arrays.toString(supportedMethods.toArray()));
    }
}
