package com.yong.httpserver.context;

import com.yong.httpserver.codec.Http11ProcessingStateEnum;
import com.yong.httpserver.core.ChannelWrapper;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class Http11ProcessingContext implements ProcessingContext {

    private ChannelWrapper channelWrapper;

    public final ByteArrayOutputStream headerStream = new ByteArrayOutputStream();

    public final ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();

    private Map<String, String> headers;

    public int bodySize = 0;

    private Http11ProcessingStateEnum currentState = Http11ProcessingStateEnum.INIT;

    private String method;

    private String version;

    private String path;

    private boolean headerOk;

    private boolean bodyOk;

    private String contentType;

    private boolean error;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isMultipart() {
        return contentType.startsWith("multipart/form-data");
    }

    public boolean hasBody() {
        return bodySize > 0;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isHeaderOk() {
        return headerOk;
    }

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public void setHeaderOk(boolean headerOk) {
        this.headerOk = headerOk;
    }


    public void setBodyOk(boolean bodyOk) {
        this.bodyOk = bodyOk;
    }

    public Http11ProcessingStateEnum getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Http11ProcessingStateEnum currentState) {
        this.currentState = currentState;
    }

    public Http11ProcessingContext(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    @Override
    public boolean isDone() {
        return (headerOk && bodyOk) || error;
    }

    public ChannelWrapper getChannel() {
        return channelWrapper;
    }

    public void setChannelWrapper(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    public boolean isUpgrade() {
        return headers.getOrDefault("Connection", "").equals("Upgrade") &&
                headers.getOrDefault("Upgrade", "").equals("websocket") &&
                !headers.getOrDefault("Sec-WebSocket-Key", "").equals("");
    }

    public String getVersion() {
        return version;
    }
}
