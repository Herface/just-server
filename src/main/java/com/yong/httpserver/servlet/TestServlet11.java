package com.yong.httpserver.servlet;

import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.servlet.RequestMapping;


@RequestMapping(path = "/test")
public class TestServlet11 {

    @RequestMapping(path = "", methods = {RequestMethod.GET})
    public void test(HttpServingContext context) {
        context.write("GET /user");
    }

    @RequestMapping(path = "{id}", methods = {RequestMethod.GET, RequestMethod.POST})
    public void test0(HttpServingContext context) {
        context.write("GET /user/" + context.getPathVar("id"));

    }

    @RequestMapping(path = "", methods = {RequestMethod.POST})
    public void test1(HttpServingContext context) {
        context.write("POST /user");
    }

    @RequestMapping(path = "", methods = {RequestMethod.PUT})
    public void test2(HttpServingContext context) {
        context.write("PUT /user");
    }

    @RequestMapping(path = "/{id}", methods = {RequestMethod.DELETE})
    public void test3(HttpServingContext context) {
        context.write("DELETE /user/{id}");
    }


    @RequestMapping(path = "/v1", methods = {RequestMethod.GET})
    public void test4(HttpServingContext context) {
//        context.setHeader("Content-Length", "0");
        context.write("USER /v1");
    }

    @RequestMapping(path = "/v2", methods = {RequestMethod.GET})
    public void test5(HttpServingContext context) {
        context.write("USER /v2");
    }


}
