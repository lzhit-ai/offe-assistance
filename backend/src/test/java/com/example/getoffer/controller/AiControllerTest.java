package com.example.getoffer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aiSessionEndpointsRequireLoginAndReturnCurrentUsersData() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "ai_user_1", "13800138111", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "前端问答"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("前端问答"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String sessionId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        mockMvc.perform(get("/api/v1/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].title").value("前端问答"));

        mockMvc.perform(get("/api/v1/ai/sessions/" + sessionId + "/messages")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));

        mockMvc.perform(delete("/api/v1/ai/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.success").value(true));

        mockMvc.perform(post("/api/v1/ai/sessions/" + sessionId + "/messages/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "解释事件循环"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamEndpointUsesServerSentEventsContentType() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "ai_user_2", "13800138112", "123456");
        String createResponse = mockMvc.perform(post("/api/v1/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "流式会话"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String sessionId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        mockMvc.perform(post("/api/v1/ai/sessions/" + sessionId + "/messages/stream")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void streamEndpointReturnsSseErrorEventWithoutBreakingTheHttpResponse() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "ai_user_3", "13800138113", "123456");
        String createResponse = mockMvc.perform(post("/api/v1/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "错误流测试"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String sessionId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/sessions/" + sessionId + "/messages/stream")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "解释事件循环"
                                }
                                """))
                .andExpect(request().asyncStarted())
                .andReturn();

        result.getAsyncResult();

        org.springframework.mock.web.MockHttpServletResponse response = result.getResponse();
        org.junit.jupiter.api.Assertions.assertEquals(200, response.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(
                response.getContentType() != null && response.getContentType().contains(MediaType.TEXT_EVENT_STREAM_VALUE));
        org.junit.jupiter.api.Assertions.assertTrue(response.getContentAsString().contains("event:error"));
    }
}
