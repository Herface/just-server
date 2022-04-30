package com.yong.httpserver.context;

import com.yong.httpserver.core.ChannelWrapper;
import com.yong.httpserver.web.MimeType;
import com.yong.httpserver.web.mime.FormFile;
import com.yong.httpserver.web.session.Cookie;
import com.yong.httpserver.web.session.Session;
import com.yong.httpserver.web.session.SessionManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface HttpServeContextInternal extends HttpServeContext {

    ByteArrayOutputStream getBodyStream();

    ByteArrayOutputStream getHeaderStream();

    ChannelWrapper getChannel();

    void setSession(Session session);

    void setCookieMap(Map<String, Cookie> cookieMap);

    void setPathVarMap(Map<String, String> extractUriTemplateVariables);

    boolean complete();

    void complete(boolean complete);

    void setQueryMap(Map<String, String> queryMap);

    void setFileMap(Map<String, List<FormFile>> fileMap);

    void setParamMap(HashMap<String, List<String>> parseHtmlForm);

    MimeType getContentType();

    String getRedirectUrl();

    String getForwardUrl();

    void setSessionManager(SessionManager sessionManager);


    void setRawBody(ByteBuffer body);

    String getQueryString();


}
