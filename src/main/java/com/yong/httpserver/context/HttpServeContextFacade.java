package com.yong.httpserver.context;

import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.Session;

import java.nio.ByteBuffer;
import java.util.List;

public class HttpServeContextFacade implements HttpServingContext {

    private final HttpServingContext contextInternal;

    public HttpServeContextFacade(HttpServingContext context) {
        this.contextInternal = context;
    }

    @Override
    public Cookie getCookie(String name) {

        return contextInternal.getCookie(name);
    }

    @Override
    public void setCookie(Cookie cookie) {
        contextInternal.setCookie(cookie);
    }

    @Override
    public void setContentType(MimeType mimeType) {
        contextInternal.setContentType(mimeType);
    }

    @Override
    public void setStatusCode(StatusCode statusCode) {
        contextInternal.setStatusCode(statusCode);
    }

    @Override
    public String getHeader(String name) {
        return contextInternal.getHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
        contextInternal.setHeader(name, value);
    }


    @Override
    public String getParam(String name) {
        return contextInternal.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        return contextInternal.getParams(name);
    }

    @Override
    public String getPathVar(String name) {
        return contextInternal.getPathVar(name);
    }

    @Override
    public void json(Object value) {
        contextInternal.json(value);
    }

    @Override
    public void write(ByteBuffer buffer) {
        contextInternal.write(buffer);
    }

    @Override
    public void write(String msg) {
        contextInternal.write(msg);
    }

    @Override
    public void render(String templateName) {
        contextInternal.render(templateName);
    }

    @Override
    public String getQuery(String name) {
        return contextInternal.getQuery(name);
    }

    @Override
    public FormFile getFormFile(String name) {
        return contextInternal.getFormFile(name);
    }

    @Override
    public List<FormFile> getFormFiles(String name) {
        return contextInternal.getFormFiles(name);
    }

    @Override
    public String getPath() {
        return contextInternal.getPath();
    }

    @Override
    public String getMethod() {
        return contextInternal.getMethod();
    }

    @Override
    public String getRemoteAddress() {
        return contextInternal.getRemoteAddress();
    }

    @Override
    public StatusCode getStatusCode() {
        return contextInternal.getStatusCode();
    }

    @Override
    public void redirect(String url) {
        contextInternal.redirect(url);
    }

    @Override
    public void forward(String url) {
        contextInternal.forward(url);
    }

    @Override
    public Session getSession() {
        return contextInternal.getSession();
    }


    @Override
    public AsyncContext startAsync() {
        return contextInternal.startAsync();
    }

    @Override
    public String asString() {
        return contextInternal.asString();
    }

    @Override
    public ByteBuffer getRaw() {
        return contextInternal.getRaw();
    }

    @Override
    public String getVersion() {
        return contextInternal.getVersion();
    }
}
