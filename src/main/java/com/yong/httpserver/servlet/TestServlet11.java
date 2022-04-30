package com.yong.httpserver.servlet;

import com.yong.httpserver.context.HttpServingContext;
import com.yong.httpserver.web.enums.RequestMethod;
import com.yong.httpserver.web.servlet.RequestMapping;


@RequestMapping(path = "/user")
public class TestServlet11 {

    @RequestMapping(path = "", methods = {RequestMethod.GET})
    public void test(HttpServingContext context) {
        context.write("GET /user");
    }

    @RequestMapping(path = "{id}", methods = {RequestMethod.GET})
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

}
