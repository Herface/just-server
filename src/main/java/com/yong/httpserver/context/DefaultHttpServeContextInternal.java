package com.yong.httpserver.context;

import com.yong.httpserver.core.ChannelWrapper;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.Session;
import com.yong.httpserver.web.session.SessionManager;
import com.yong.httpserver.web.util.JsonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DefaultHttpServeContextInternal implements HttpServeContextInternal {

    private Map<String, String> queryMap = Collections.emptyMap();

    private Map<String, Cookie> cookieMap = Collections.emptyMap();

    private Map<String, String> headerMap = Collections.emptyMap();

    private SessionManager sessionManager;

    private MimeType contentType = MimeType.TEXT_HTML;

    private Map<String, String> responseHeader = new HashMap<>();

    private ByteArrayOutputStream headerStream = new ByteArrayOutputStream();

    private PrintWriter headerWriter = new PrintWriter(headerStream);

    private ByteArrayOutputStream body = new ByteArrayOutputStream();

    private PrintWriter bodyWriter = new PrintWriter(body, true, StandardCharsets.UTF_8);

    private Session session;

    private String path;

    private ByteBuffer raw;

    boolean async;

    private String method;

    private ChannelWrapper channelWrapper;

    private boolean complete;

    private StatusCode statusCode = StatusCode.OK;

    private Map<String, List<String>> paramMap = Collections.emptyMap();

    private Map<String, String> pathVarMap = Collections.emptyMap();

    private Map<String, List<FormFile>> fileMap = Collections.emptyMap();

    private List<ByteBuffer> bufferList = new ArrayList<>();

    private List<Cookie> cookieList = new ArrayList<>();
    private String redirectUrl;

    private String forwardUrl;

    private String version;

    private String queryString;

    public DefaultHttpServeContextInternal(Http11ProcessingContext context) {
        this.version = context.getVersion();
        this.method = context.getMethod();
        this.headerMap = context.getHeaders();
        this.channelWrapper = context.getChannel();
        String decode = URLDecoder.decode(context.getPath(), StandardCharsets.UTF_8);
        int indexOf = decode.indexOf("?");
        if (indexOf == -1) {
            path = decode;
        } else {
            queryString = decode.substring(indexOf + 1);
            path = decode.substring(0, indexOf);
        }
    }


    public void setPathVarMap(Map<String, String> pathVarMap) {
        this.pathVarMap = pathVarMap;
    }

    @Override
    public boolean complete() {
        return complete;
    }

    @Override
    public void complete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public ByteArrayOutputStream getBodyStream() {
        return body;
    }

    @Override
    public ByteArrayOutputStream getHeaderStream() {
        return headerStream;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void redirect(String url) {
        setStatusCode(StatusCode.FOUND);
        setHeader("Location", url);
    }

    @Override
    public void forward(String url) {
        this.forwardUrl = url;
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }


    @Override
    public String getForwardUrl() {
        return forwardUrl;
    }

    @Override
    public Cookie getCookie(String name) {
        return cookieMap.get(name);
    }

    @Override
    public Map<String, String> getHeaderMap() {
        return Collections.unmodifiableMap(headerMap);
    }

    @Override
    public void setCookie(Cookie cookie) {
        setHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public void setContentType(MimeType mimeType) {
        this.contentType = mimeType;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public Session getSession() {
        if (this.session != null) {
            return this.session;
        }
        Cookie cookie = getCookie("JSESSIONID");
        String id = cookie != null ? cookie.value() : "";
        Session session = sessionManager.getSession(id);
        if (!Objects.equals(session.getId(), id)) {
            this.session = session;
            setCookie(new Cookie("JSESSIONID", session.getId()));
        }
        return session;
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void setRawBody(ByteBuffer body) {
        this.raw = body;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public AsyncContext startAsync() {
        complete(true);
        return new DefaultAsyncContext(this, channelWrapper);
    }

    @Override
    public String asString() {
        return new String(raw.array(), StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer getRaw() {
        return raw;
    }

    @Override
    public String getVersion() {
        return version;
    }


    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setCookieMap(Map<String, Cookie> cookieMap) {
        this.cookieMap = cookieMap;
    }


    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemoteAddress() {
        return channelWrapper.getRemoteAddress();
    }

    public void setFileMap(Map<String, List<FormFile>> fileMap) {
        this.fileMap = fileMap;
    }

    @Override
    public void setParamMap(HashMap<String, List<String>> paramMap) {
        this.paramMap = paramMap;
    }

    @Override
    public MimeType getContentType() {
        return contentType;
    }


    public void setQueryMap(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }


    @Override
    public ChannelWrapper getChannel() {
        return channelWrapper;
    }

    @Override
    public String getHeader(String name) {
        return headerMap.get(name);
    }

    @Override
    public void setHeader(String name, String value) {
        headerWriter.println(name + ": " + value);
        headerWriter.flush();
    }


    @Override
    public String getParam(String name) {
        List<String> list = paramMap.getOrDefault(name, Collections.emptyList());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<String> getParams(String name) {
        return paramMap.get(name);
    }

    @Override
    public String getPathVar(String name) {
        return pathVarMap.get(name);
    }

    @Override
    public void json(Object value) {
        setContentType(MimeType.APPLICATION_JSON);
        write(JsonUtils.toJson(value));
    }

    @Override
    public void write(ByteBuffer buffer) {
        try {
            body.write(buffer.array());
        } catch (IOException ignore) {
        }
    }


    @Override
    public void write(String msg) {
        bodyWriter.write(msg);
        bodyWriter.flush();
    }

    @Override
    public void render(String templateName) {

    }

    @Override
    public String getQuery(String name) {
        return queryMap.get(name);
    }

    @Override
    public FormFile getFormFile(String name) {
        List<FormFile> files = fileMap.get(name);
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }


    @Override
    public List<FormFile> getFormFiles(String name) {
        return fileMap.get(name);
    }


}
