package com.example.getoffer.controller;

import com.jayway.jsonpath.JsonPath;

public final class JsonTestUtils {

    private JsonTestUtils() {
    }

    public static Object readJsonPath(String json, String path) {
        return JsonPath.read(json, path);
    }
}
