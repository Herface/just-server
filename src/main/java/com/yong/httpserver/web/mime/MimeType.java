package com.yong.httpserver.web.mime;

import java.util.HashMap;
import java.util.Map;

public enum MimeType {
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),
    IMAGE_BMP("image/bmp"),
    IMAGE_TIFF("image/tiff"),
    IMAGE_SVG("image/svg+xml"),
    APPLICATION_JSON("application/json"),
    APPLICATION_PDF("application/pdf"),
    WWW_FORM("application/x-www-form-urlencoded"),
    OCTET_STREAM("application/octet-stream"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    FONT_TTF("font/ttf"),
    FONT_WOFF("font/woff"),
    FONT_WOFF2("font/woff2");

    private static final Map<String, MimeType> MIME_TYPE_MAP;

    static {
        MIME_TYPE_MAP = new HashMap<>();
        MIME_TYPE_MAP.put("jpg", IMAGE_JPEG);
        MIME_TYPE_MAP.put("jpeg", IMAGE_JPEG);
        MIME_TYPE_MAP.put("png", IMAGE_PNG);
        MIME_TYPE_MAP.put("txt", TEXT_PLAIN);
        MIME_TYPE_MAP.put("html", TEXT_HTML);
        MIME_TYPE_MAP.put("gif", IMAGE_GIF);
        MIME_TYPE_MAP.put("css", TEXT_CSS);
        MIME_TYPE_MAP.put("js", TEXT_JAVASCRIPT);
        MIME_TYPE_MAP.put("svg", IMAGE_SVG);
        MIME_TYPE_MAP.put("ttf", FONT_TTF);
        MIME_TYPE_MAP.put("woff", FONT_WOFF);
        MIME_TYPE_MAP.put("woff2", FONT_WOFF2);
        MIME_TYPE_MAP.put("bmp", IMAGE_BMP);
        MIME_TYPE_MAP.put("tiff", IMAGE_TIFF);
        MIME_TYPE_MAP.put("pdf", APPLICATION_PDF);
        MIME_TYPE_MAP.put("json", APPLICATION_JSON);
        MIME_TYPE_MAP.put("application/x-www-form-urlencoded", WWW_FORM);


    }

    public final String value;

    MimeType(String value) {
        this.value = value;
    }

    public static MimeType getType(String name) {
        return MIME_TYPE_MAP.getOrDefault(name.toLowerCase(), OCTET_STREAM);
    }
}
