package com.yong.httpserver.core.io;

import com.yong.httpserver.web.enums.HttpVersion;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.mime.MimeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ResponseOutputStream extends OutputStream {

    private ByteArrayOutputStream headerStream;

    private PrintWriter headerWriter;

    private ByteArrayOutputStream bodyStream;

    private PrintWriter bodyWriter;

    private StatusCode statusCode = StatusCode.OK;

    private MimeType mimeType = MimeType.TEXT_HTML;

    public ResponseOutputStream() {
        this.bodyStream = new ByteArrayOutputStream();
        this.bodyWriter = new PrintWriter(bodyStream, true);
        this.headerStream = new ByteArrayOutputStream();
        this.headerWriter = new PrintWriter(headerStream, true);
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public void write(int b) throws IOException {
        bodyStream.write(b);
    }

    public void write(String s) {
        bodyWriter.write(s);
        bodyWriter.flush();
    }

    public void write(byte[] b) {
        try {
            bodyStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHeader(String name, String value) {
        headerWriter.println(name + ": " + value);
    }

    public void write(byte[] b, int off, int len) {
        bodyStream.write(b, off, len);
    }

    public ByteBuffer toBuffer() {
        String headerLine = HttpVersion.HTTP_11.name + " " + statusCode.code + " " + statusCode.message + System.lineSeparator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int size = bodyStream.size();
        setHeader("Connection", "keep-alive");
        setHeader("Content-Length", String.valueOf(size));
        setHeader("Content-Type", mimeType.value);
        try {
            out.write(headerLine.getBytes(StandardCharsets.UTF_8));
            headerWriter.println();
            out.write(headerStream.toByteArray());
            out.write(bodyStream.toByteArray());

        } catch (Exception ignore) {
        }
        return ByteBuffer.wrap(out.toByteArray());
    }


    public int size() {
        return bodyStream.size();
    }
}
