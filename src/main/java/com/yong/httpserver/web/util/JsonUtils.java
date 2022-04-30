package com.yong.httpserver.web.util;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonUtils {
    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd")
                .serializeNulls();
        gson = builder.create();
    }

    private JsonUtils() {
        throw new UnsupportedOperationException();
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> type) {
        return gson.fromJson(str, type);
    }

    public static <T> List<T> fromJsonArray(String str, Class<T> type) {
        Objects.requireNonNull(str);
        JsonArray jsonArray = JsonParser.parseString(str).getAsJsonArray();
        List<T> objs = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            objs.add(gson.fromJson(jsonArray.get(i), type));
        }
        return objs;
    }
}
