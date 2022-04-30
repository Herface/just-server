package com.yong.httpserver.servlet;

import com.yong.httpserver.context.HttpServeContext;
import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.servlet.RequestMapping;


@RequestMapping(path = "/user")
public class TestServlet11 {

    @RequestMapping(path = "", methods = {RequestMethod.GET})
    public void test(HttpServeContext context) {
        context.write("GET /user");
    }

    @RequestMapping(path = "{id}", methods = {RequestMethod.GET})
    public void test0(HttpServeContext context) {
        context.write("GET /user/{id}");

    }

    @RequestMapping(path = "", methods = {RequestMethod.POST})
    public void test1(HttpServeContext context) {
        context.write("POST /user");
    }

    @RequestMapping(path = "", methods = {RequestMethod.PUT})
    public void test2(HttpServeContext context) {
        context.write("PUT /user");
    }

    @RequestMapping(path = "/{id}", methods = {RequestMethod.DELETE})
    public void test3(HttpServeContext context) {
        context.write("DELETE /user/{id}");
    }

}
