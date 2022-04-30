package com.yong.httpserver.web.mime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FormFile {

    private String fileName;
    private String fileType;
    private ByteBuffer buffer;
    private int length;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void save(String path) {
        try (
                FileChannel channel = FileChannel.open(Path.of(path), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
        ) {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
