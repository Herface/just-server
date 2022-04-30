package com.yong.httpserver.web.session;

import java.util.Date;

public record Cookie(String name, String value, String path, String domain, Date expireDate, boolean secured,
                     boolean httpOnly) {

    public Cookie(String name, String value) {
        this(name, value, "/", null, null, false, true);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("=").append(value)
                .append("; ");
        if (expireDate != null) {
            builder.append("Expires=")
                    .append(expireDate)
                    .append("; ");
        }
        if (path != null) {
            builder.append("path=")
                    .append(path)
                    .append("; ");
        }
        if (domain != null) {
            builder.append("domain=")
                    .append(domain)
                    .append("; ");
        }
        builder.append(secured ? "Secure; " : "")
                .append(httpOnly ? "HttpOnly; " : "");
        return builder.toString();
    }
}
