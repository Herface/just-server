## Simple and easy-to-use aio based http server

## Usage

```java
package com.yong.httpserver.servlet;

@RequestMapping(path = "/user")
public class TestServlet11 {
    @RequestMapping(path = "", methods = {RequestMethod.GET})
    public void test(HttpServeContext context) {
        context.write("GET /user");
    }
}
HttpServerBuilder builder = new HttpServerBuilder();
builder.webConfig(webConfig->webConfig.host("0.0.0.0")
        .port(8080)
        .basePackage("com.yong.httpserver.servlet")
        .staticPath("path-to-resource")
        .maxUploadFileSize(1024*1024))
        .enableWebSocket(webSocketConfig->webSocketConfig.path("/ws")
        .eventHandler(new DefaultWebSocketEventHandler());
        HttpServer server=builder.build();
        server.start();
```

