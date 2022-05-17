## Simple and easy-to-use toy http server

## Do not use in production environment
- [x] ant-style request dispatching
- [x] session/cookie
- [x] websocket
- [x] filter chain
- [x] file uploading
- [x] static content serving
- [ ] http2(coming soon...)

## Requirement

- JDK17+

## Usage

```java
package com.yong.httpserver.servlet;

@RequestMapping(path = "/user")
public class TestServlet11 {
    @RequestMapping(path = "", methods = {RequestMethod.GET})
    public void test(HttpServingContext context) {
        context.write("GET /user");
    }
}
HttpServerBuilder builder = new HttpServerBuilder();
builder.http(webConfig->webConfig.host("0.0.0.0")
        .port(8080)
        .basePackage("com.yong.httpserver.servlet")
        .staticPath("path-to-resource")
        .maxUploadFileSize(1024*1024))
        .enableWebSocket(webSocketConfig->webSocketConfig.path("/ws")
        .eventHandler(new DefaultWebSocketEventHandler());
        HttpServer server=builder.build();
        server.start();
```

