package com.yong.httpserver.web.msg;

import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.enums.HttpVersion;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

public class HeaderBuilder {

    private final ByteArrayOutputStream stream;

    private final PrintWriter writer;

    private static final String CRLF_PATTERN = "[\r\n]";

    public HeaderBuilder(HttpVersion version, StatusCode code) {
        stream = new ByteArrayOutputStream();
        writer = new PrintWriter(stream);
        writeHeadLine(version, code);
    }


    private void writeHeadLine(HttpVersion version, StatusCode code) {
        writer.append(version.name)
                .append(" ")
                .append(String.valueOf(code.code))
                .append(" ")
                .append(code.message)
                .append("\r\n");
        writer.flush();
    }

    public HeaderBuilder setHeader(String name, String value) {
        name = escape(name);
        value = escape(value);
        writer.append(name)
                .append(": ")
                .append(value)
                .append("\r\n");
        writer.flush();
        return this;
    }

    public String escape(String s) {
        return s.replaceAll(CRLF_PATTERN, "");
    }

    public ByteBuffer toBuffer() {
        writer.append("\r\n");
        writer.flush();
        return ByteBuffer.wrap(stream.toByteArray());
    }


}
