package com.yong.httpserver.context;

import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.Session;

import java.nio.ByteBuffer;
import java.util.List;


public interface HttpServingContext {

    Cookie getCookie(String name);

    void setCookie(Cookie cookie);

    void setContentType(MimeType mimeType);

    void setStatusCode(StatusCode statusCode);

    String getHeader(String name);

    void setHeader(String name, String value);


    String getParam(String name);

    List<String> getParams(String name);

    String getPathVar(String name);

    void json(Object value);

    void write(ByteBuffer buffer);

    void write(String msg);

    void render(String templateName);

    String getQuery(String name);

    FormFile getFormFile(String name);

    List<FormFile> getFormFiles(String name);

    String getPath();

    String getMethod();

    String getRemoteAddress();

    StatusCode getStatusCode();

    void redirect(String url);

    void forward(String url);

    Session getSession();


    AsyncContext startAsync();

    String asString();

    ByteBuffer getRaw();


    String getVersion();
}
