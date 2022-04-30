package com.yong.httpserver.web.util;

import com.yong.httpserver.web.servlet.Servlet;
import com.yong.httpserver.web.servlet.ServletMapping;

import java.util.List;

public interface ServletScanner {

    List<ServletMapping> scan(String... path);

}
