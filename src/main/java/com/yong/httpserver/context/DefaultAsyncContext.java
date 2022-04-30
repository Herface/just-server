package com.yong.httpserver.context;

import com.yong.httpserver.core.ChannelWrapper;
import com.yong.httpserver.web.MimeType;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.enums.HttpVersion;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.Session;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultAsyncContext implements AsyncContext {

    private HttpServeContextInternal context;

    private ChannelWrapper channel;

    public DefaultAsyncContext(HttpServeContextInternal context, ChannelWrapper channel) {
        this.context = context;
        this.channel = channel;
    }

    @Override
    public AsyncContext startAsync() {
        return null;
    }

    @Override
    public String asString() {
        return context.asString();
    }

    @Override
    public ByteBuffer getRaw() {
        return context.getRaw();
    }

    @Override
    public String getVersion() {
        return context.getVersion();
    }

    @Override
    public void complete() {
        ByteArrayOutputStream bodyStream = context.getBodyStream();
        ByteArrayOutputStream headerStream = context.getHeaderStream();
        int size = bodyStream.size();
        StatusCode statusCode = getStatusCode();
        String headLine = HttpVersion.HTTP_11.name + " " + statusCode.code + " " + statusCode.message + "\r\n";
        context.setHeader("Connection", "keep-alive");
        context.setHeader("Content-Length", String.valueOf(size));
        context.setHeader("Content-Type", context.getContentType().value);
        channel.write(ByteBuffer.wrap(headLine.getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap(headerStream.toByteArray()));
        channel.write(ByteBuffer.wrap("\r\n".getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap(bodyStream.toByteArray()));
    }

    @Override
    public Cookie getCookie(String name) {
        return context.getCookie(name);
    }

    @Override
    public void setCookie(Cookie cookie) {
    }

    @Override
    public void setContentType(MimeType mimeType) {
    }

    @Override
    public void setStatusCode(StatusCode statusCode) {
    }

    @Override
    public String getHeader(String name) {
        return context.getHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
    }


    @Override
    public String getParam(String name) {
        return context.getHeader(name);
    }

    @Override
    public List<String> getParams(String name) {
        return context.getParams(name);
    }

    @Override
    public String getPathVar(String name) {
        return context.getPathVar(name);
    }

    @Override
    public void json(Object value) {
    }

    @Override
    public void write(ByteBuffer buffer) {
        context.write(buffer);
    }

    @Override
    public void write(String msg) {
        context.write(msg);
    }

    @Override
    public void render(String templateName) {
    }

    @Override
    public String getQuery(String name) {
        return context.getQuery(name);
    }

    @Override
    public FormFile getFormFile(String name) {
        return context.getFormFile(name);
    }

    @Override
    public List<FormFile> getFormFiles(String name) {
        return context.getFormFiles(name);
    }

    @Override
    public String getPath() {
        return context.getPath();
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getRemoteAddress() {
        return context.getRemoteAddress();
    }

    @Override
    public StatusCode getStatusCode() {
        return context.getStatusCode();
    }

    @Override
    public void redirect(String url) {
        context.redirect(url);
    }

    @Override
    public void forward(String url) {
    }

    @Override
    public Session getSession() {
        return context.getSession();
    }


}
