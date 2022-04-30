package com.yong.httpserver.web.util;

import com.yong.httpserver.context.HttpServeContext;
import com.yong.httpserver.web.servlet.RequestMapping;
import com.yong.httpserver.web.servlet.Servlet;
import com.yong.httpserver.web.servlet.ServletMapping;
import com.yong.httpserver.web.servlet.ServletMethod;
import org.apache.coyote.Request;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClassPathServletScanner implements ServletScanner {
    @Override
    public List<ServletMapping> scan(String... basePackages) {
        ArrayList<ServletMapping> servletMappings = new ArrayList<>();
        for (String basePackage : basePackages) {
            String s = basePackage.replaceAll("\\.", "/");
            try {
                URL resource = ClassLoader.getSystemResource(s);
                URI uri = resource.toURI();
                Path p = Path.of(uri);
                scan0(servletMappings, basePackage, p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return servletMappings;
    }

    private void scan0(List<ServletMapping> servletMappings, String basePackage, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path, f -> {
                if (Files.isDirectory(f)) {
                    return true;
                }
                return f.getFileName().toString().endsWith(".class");
            });
            for (Path path1 : stream) {
                scan0(servletMappings, basePackage, path1);
            }
        } else {
            String filename = path.getFileName().toString();
            String className = filename.substring(0, filename.lastIndexOf("."));
            String fullName = basePackage + "." + className;
            try {
                Class<?> aClass = Class.forName(fullName);
                List<ServletMapping> servletMethods = findServletMethod(aClass);
                servletMappings.addAll(servletMethods);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<ServletMapping> findServletMethod(Class<?> aClass) throws Exception {
        List<ServletMapping> list = new ArrayList<>();
        Constructor<?> constructor = aClass.getConstructor();
        Object target = null;
        constructor.setAccessible(true);
        RequestMapping mapping = aClass.getAnnotation(RequestMapping.class);
        String basePath = "";
        if (mapping != null) {
            basePath = mapping.path();
        }
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            if (annotation != null) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == HttpServeContext.class) {
                    String finalPath = adjust(basePath) + adjust(annotation.path());
                    if (target == null) {
                        target = constructor.newInstance();
                    }
                    list.add(new ServletMapping(finalPath, new ServletMethod(method, target), Set.of(annotation.methods())));
                }
            }
        }
        return list;
    }

    private String adjust(String path) {
        path = path.replaceAll("/+$", "");
        if (path.length() <= 0) {
            return path;
        }
        return "/" + path.replaceAll("^/+", "");
    }
}
