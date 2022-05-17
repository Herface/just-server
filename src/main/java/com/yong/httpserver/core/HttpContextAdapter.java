package com.yong.httpserver.core;

import com.yong.httpserver.codec.Http11Parser;
import com.yong.httpserver.context.*;
import com.yong.httpserver.core.io.ResponseOutputStream;
import com.yong.httpserver.web.dispatcher.RequestDispatcher;
import com.yong.httpserver.web.enums.HttpVersion;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.filter.FilterMapping;
import com.yong.httpserver.web.handler.ExceptionHandler;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.msg.HeaderBuilder;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.SessionManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Adapter
 * context 转换 请求处理
 */
public class HttpContextAdapter implements ContextAdapter {

    private RequestDispatcher dispatcher;

    private String wsPath;

    private Http11Parser http11Parser;

    private ExceptionHandler exceptionHandler;

    private List<FilterMapping> filterMappings;

    private int maxUploadSize;

    private int maxMsgSize;

    private SessionManager sessionManager;


    private String staticPath;

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setWsPath(String wsPath) {
        this.wsPath = wsPath;
    }

    public void setStaticPath(String staticPath) {
        this.staticPath = staticPath;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void setFilterMappings(List<FilterMapping> filterMappings) {
        this.filterMappings = filterMappings;
    }



    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public void setMaxMsgSize(int maxMsgSize) {
        this.maxMsgSize = maxMsgSize;
    }

    @Override
    public boolean support(ProcessingContext context) {
        return context instanceof Http11ProcessingContext;
    }


    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ProcessingStateEnum serve(ProcessingContext context) {
        Http11ProcessingContext processingContext = (Http11ProcessingContext) context;
        HttpServeContextInternal to;
        try {
            if (!processingContext.isDone()) {
                http11Parser.parse(processingContext);
            }
            if (!processingContext.isDone()) {
                return ProcessingStateEnum.CONTINUE;
            }
            if (processingContext.isUpgrade()) {
                // 未开启 ws 或路径不正确
                if (!Objects.equals(processingContext.getPath(), wsPath)) {
                    return ProcessingStateEnum.ERROR;
                }
                return ProcessingStateEnum.UPGRADING;
            }
            to = new DefaultHttpServeContextInternal(processingContext);
            parseRequest(processingContext, to);
//            preServe(to);
        } catch (Exception e) {
            badRequest(processingContext.getChannel(), e);
            return ProcessingStateEnum.DONE;
        }
        to.setSessionManager(sessionManager);
        dispatcher.dispatch(to);
        postServe(to);
        return ProcessingStateEnum.DONE;
    }

    private void preServe(HttpServeContextInternal contextInternal) {
        String version = contextInternal.getVersion();
        if (HttpVersion.getVersion(version) == null) {
            throw new RuntimeException("Http version unsupported");
        }
    }

    private void badRequest(ChannelWrapper channel, Exception e) {
        e.printStackTrace();
        HeaderBuilder headerBuilder = new HeaderBuilder(HttpVersion.HTTP_11, StatusCode.BAD_REQUEST);
        channel.write(headerBuilder.toBuffer());
        channel.close();
    }

    private void postServe(HttpServeContextInternal context) {
        if (context.complete()) {
            return;
        }
        ResponseOutputStream outputStream = context.getOutputStream();
        context.getChannel().write(outputStream.toBuffer());
    }


    private void parseRequest(Http11ProcessingContext from, HttpServeContextInternal to) {
        to.setQueryMap(parseQueryParam(to.getQueryString()));
        String cookie = from.getHeaders().get("Cookie");
        if (cookie != null) {
            to.setCookieMap(parseCookie(cookie));
        }
        if (!from.hasBody()) {
            ByteBuffer buffer = from.getChannel().getBuffer();
            buffer.clear();
            buffer.position(buffer.capacity());
            return;
        }
        if (from.isMultipart()) {
            String boundary = parseBoundary(from.getContentType());
            if (boundary == null) {
                return;
            }
            to.setFileMap(parseMultipartFormData(boundary, ByteBuffer.wrap(from.bodyStream.toByteArray())));
        } else if (MimeType.getType(from.getContentType()) == MimeType.WWW_FORM) {
            to.setParamMap(parseHtmlForm(ByteBuffer.wrap(from.bodyStream.toByteArray())));
        } else {
            to.setRawBody(ByteBuffer.wrap(from.bodyStream.toByteArray()));
        }
    }

    private Map<String, Cookie> parseCookie(String cookieLine) {
        Map<String, Cookie> cookieMap = new HashMap<>();

        String[] kvs = cookieLine.split(";");
        for (String kv : kvs) {
            String[] nameValue = kv.split("=");
            if (nameValue.length < 2) {
                continue;
            }
            String name = nameValue[0].trim();
            String value = nameValue[1].trim();
            if (name.length() > 0 && value.length() > 0) {
                cookieMap.put(name, new Cookie(name, value));
            }
        }
        return cookieMap;
    }

    private String parseBoundary(String contentType) {
        String[] split = contentType.split(";", 2);
        if (split.length < 2) {
            return null;
        }
        String[] kv = split[1].split("=");
        if (kv.length < 2) {
            return null;
        }
        String boundary = kv[1].trim();
        return "--" + boundary;
    }

    private Map<String, String> parseQueryParam(String query) {
        Map<String, String> queryMap = new HashMap<>();
        if (query == null) {
            return queryMap;
        }
        String[] queries = query.split("&");
        for (String q : queries) {
            String[] split = q.split("=", 2);
            if (split.length < 2) {
                continue;
            }
            queryMap.put(split[0], split[1]);
        }
        return queryMap;
    }

    private HashMap<String, List<String>> parseHtmlForm(ByteBuffer wrap) {
        String paramStr = new String(wrap.array()).trim();
        String[] params = paramStr.split("&");
        HashMap<String, List<String>> paramMap = new HashMap<>();
        for (String param : params) {
            String[] pair = param.split("=", 2);
            if (pair.length < 2) {
                continue;
            }
            String k = pair[0], v = pair[1];
            paramMap.putIfAbsent(k, new ArrayList<>());
            paramMap.get(k).add(v);
        }
        return paramMap;
    }

    /**
     * multipart/form-data 解析
     * 0 初始状态 头分割行
     * 1 文件头
     * 2 文件内容
     *
     * @param boundary 分割行
     * @param buffer   请求体
     * @return 文件列表
     */
    private Map<String, List<FormFile>> parseMultipartFormData(String boundary, ByteBuffer buffer) {
        Map<String, List<FormFile>> fileMap = new HashMap<>();
        byte[] boundaryBytes = boundary.getBytes();
        byte b;
        int curState = 0;
        FormFile curFile = null;
        String curName = null;
        int curSize = 0;
        while (buffer.hasRemaining()) {
            switch (curState) {
                case 0 -> {
                    for (int i = 0; i < boundaryBytes.length && buffer.hasRemaining(); i++) {
                        b = buffer.get();
                        if (b != boundaryBytes[i]) {
                            throw new RuntimeException();
                        }
                    }
                    if (buffer.hasRemaining() && buffer.get() == '\r' && buffer.hasRemaining() && buffer.get() == '\n') {
                        curState = 1;
                        curFile = new FormFile();
                        continue;
                    }
                    throw new RuntimeException();

                }
                case 1 -> {
                    StringBuilder builder = new StringBuilder();
                    // read line util \r
                    while (buffer.hasRemaining() && (b = buffer.get()) != '\r') {
                        builder.append((char) b);
                    }
                    // skip \n
                    if (buffer.hasRemaining() && buffer.get() != '\n') {
                        throw new RuntimeException();
                    }
                    // check if a headers are fully read
                    buffer.mark();
                    if (buffer.hasRemaining() && buffer.get() == '\r' && buffer.hasRemaining() && buffer.get() == '\n') {
                        curState = 2;
                    } else {
                        buffer.reset();
                    }
                    String line = builder.toString();
                    if (line.startsWith("Content-Disposition")) {
                        String[] split = line.split(";", 2);
                        if (split.length < 2) {
                            throw new RuntimeException();
                        }
                        String s = split[1].trim();
                        String[] kvs = s.split(";");
                        if (kvs.length < 2) {
                            continue;
                        }
                        for (String kv : kvs) {
                            String[] pair = kv.split("=");
                            String k = pair[0].trim(), v = pair[1].trim();
                            if ("name".equalsIgnoreCase(k)) {
                                if (v.startsWith("\"") && v.endsWith("\"") && v.length() > 3) {
                                    v = v.substring(1, v.length() - 1);
                                }
                                curName = v;
                            }
                            if ("filename".equalsIgnoreCase(k)) {
                                curFile.setFileName(v);
                            }
                        }
                    }
                }
                case 2 -> {
                    ByteArrayOutputStream file = new ByteArrayOutputStream();
                    while (buffer.hasRemaining()) {
                        buffer.mark();
                        boolean end = true;
                        // 文件分割行
                        if (buffer.hasRemaining() && buffer.get() == '\r' && buffer.hasRemaining() && buffer.get() == '\n') {
                            for (int i = 0; i < boundaryBytes.length && buffer.hasRemaining(); i++) {
                                b = buffer.get();
                                if (b != boundaryBytes[i]) {
                                    end = false;
                                    break;
                                }
                            }
                            if (end) {
                                // 跳过换行
                                if (buffer.hasRemaining() && buffer.get() == '\r' && buffer.hasRemaining()) {
                                    buffer.get();
                                }
                                fileMap.putIfAbsent(curName, new ArrayList<>());
                                fileMap.get(curName).add(curFile);
                                curFile.setBuffer(ByteBuffer.wrap(file.toByteArray()));
                                curState = 1;
                                curFile = new FormFile();
                                curName = null;
                                break;
                            }
                        }
                        buffer.reset();

                        if (buffer.hasRemaining()) {
                            file.write(buffer.get());
                            curSize++;
                            if (curSize > maxUploadSize) {
                                throw new RuntimeException("Max upload size exceeded");
                            }
                        }
                    }
                }
            }
        }
        return fileMap;
    }

    @Override
    public void init() {
        http11Parser = new Http11Parser();
        http11Parser.setMaxMsgSize(maxMsgSize);

    }

    @Override
    public void start() {
        sessionManager.start();
    }

}
