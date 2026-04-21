package com.example.getoffer.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class TestAuthHelper {

    private TestAuthHelper() {
    }

    public static String registerAndGetToken(MockMvc mockMvc, String username, String phone, String password)
            throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "phone": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, phone, password)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonTestUtils.readJsonPath(response, "$.data.accessToken").toString();
    }
}
