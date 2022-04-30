package com.yong.httpserver.web.mime;

public interface MineTypeParser {

    MimeObject parse(byte[] buf);

    boolean support();
}
