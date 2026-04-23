package com.example.getoffer.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMyArticlesReturnsPagedList() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "writer2", "13800138005", "123456");

        mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "MySQL 索引",
                                  "category": "数据库",
                                  "type": 1,
                                  "tags": ["MySQL", "索引"],
                                  "content": "# 索引正文"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me/articles")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].title").value("MySQL 索引"))
                .andExpect(jsonPath("$.data.list[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void updateProfileReturnsUpdatedNickname() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "writer3", "13800138011", "123456");

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "new_name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("writer3"))
                .andExpect(jsonPath("$.data.nickname").value("new_name"));
    }

    @Test
    void uploadAvatarStoresImageAndReturnsAvatarPath() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "avatar_user", "13800138012", "123456");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.avatar").value(startsWith("/uploads/avatars/")));
    }

    @Test
    void uploadAvatarRejectsNonImageFile() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "avatar_user_2", "13800138013", "123456");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "plain-text".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
