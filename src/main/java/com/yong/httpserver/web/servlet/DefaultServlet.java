package com.yong.httpserver.web.servlet;

import com.yong.httpserver.context.HttpServeContextInternal;
import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.core.ChannelWrapper;
import com.yong.httpserver.web.enums.HttpVersion;
import com.yong.httpserver.web.enums.StatusCode;
import com.yong.httpserver.web.mime.MimeType;
import com.yong.httpserver.web.msg.HeaderBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Objects;


public class DefaultServlet implements Servlet {

    private String staticPath = "static";

    private static final int MAX_BUFFER_SIZE = 4096;

    private static final String DEFAULT_INDEX = "index.html";

    @Override
    public void serve(HttpServingContext context) {
        try {
            doServe(context);
        } catch (Exception e) {
            e.printStackTrace();
            notFoundError(context);
        }
    }


    private void doServe(HttpServingContext context) {
        HttpServeContextInternal internalHttpServeContext = (HttpServeContextInternal) context;
        String path = context.getPath();
        if ("/".equals(path)) {
            defaultIndex(internalHttpServeContext);
            return;
        }
        Path p = getResourcePath(path);
        if (p == null || Files.isDirectory(p)) {
            notFoundError(context);
            return;
        }
        transferFile(internalHttpServeContext, p);
    }

    private void transferFile(HttpServeContextInternal context, Path p) {
        String path = p.toString();
        ChannelWrapper wrapper = context.getChannel();
        String etag = context.getHeader("If-None-Match");
        String newEtag = getEtag(p);
        if (etag != null && Objects.equals(newEtag, etag)) {
            notModified(context);
            return;
        }
        etag = newEtag;
        try (
                FileChannel channel = FileChannel.open(p, StandardOpenOption.READ)
        ) {
            long size = channel.size();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String extName = fileName.substring(path.lastIndexOf(".") + 1);
            MimeType mimeType = MimeType.getType(extName);
            HeaderBuilder headerBuilder = new HeaderBuilder(HttpVersion.HTTP_11, StatusCode.OK);
            headerBuilder.setHeader("Content-Length", String.valueOf(size));
            if (mimeType == MimeType.OCTET_STREAM) {
                headerBuilder.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }
            headerBuilder.setHeader("Content-Type", mimeType.value);
            if (etag != null) {
                headerBuilder.setHeader("ETag", etag);
            }
            wrapper.write(headerBuilder.toBuffer());
            trunkTransfer(channel, wrapper, 0, (int) size);
            context.complete(true);
        } catch (Exception e) {
            e.printStackTrace();
            notFoundError(context);
            wrapper.close();
        }
    }

    private Path getResourcePath(String path) {
        Path p = Path.of(staticPath + "/" + path);
        if (Files.exists(p)) {
            return p;
        }
        return null;
    }

    private void defaultIndex(HttpServeContextInternal context) {
        Path path = getResourcePath(DEFAULT_INDEX);
        if (path == null) {
            notFoundError(context);
            return;
        }
        transferFile(context, path);
    }

    private void trunkTransfer(FileChannel channel, ChannelWrapper wrapper, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        int trunkSize = length / MAX_BUFFER_SIZE;
        int remaining = 0;
        if (length % MAX_BUFFER_SIZE != 0) {
            remaining = length % MAX_BUFFER_SIZE;
        }
        channel.position(offset);
        for (int i = 0; i < trunkSize; i++) {
            channel.read(buffer);
            buffer.flip();
            wrapper.write(buffer);
            buffer.clear();
        }
        if (remaining > 0) {
            buffer.limit(remaining);
            channel.read(buffer);
            buffer.flip();
            wrapper.write(buffer);
        }
    }

    private void mapTransfer(FileChannel channel, ChannelWrapper wrapper, int offset, int length) throws IOException {
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, offset, length);
        wrapper.write(map);
    }

    private void notModified(HttpServingContext context) {
        context.setStatusCode(StatusCode.NOT_MODIFIED);
    }

    private String getEtag(Path p) {
        try {
            FileTime time = Files.getLastModifiedTime(p);
            long millis = time.toMillis();
            long size = Files.size(p);
            return "W/\"" + size + "-" + millis + "\"";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void notFoundError(HttpServingContext context) {
        context.setStatusCode(StatusCode.NOT_FOUND);
    }

    public void setStaticPath(String staticPath) {
        this.staticPath = staticPath;
    }
}
