package com.example.getoffer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addRemoveAndListFavoritesWork() throws Exception {
        String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "author1", "13800138002", "123456");
        String favoriteToken = TestAuthHelper.registerAndGetToken(mockMvc, "reader1", "13800138003", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Redis 高频题",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Redis", "缓存"],
                                  "content": "# Redis 正文"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/favorite")
                        .header("Authorization", "Bearer " + favoriteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isFavorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").value(1));

        mockMvc.perform(get("/api/v1/users/me/favorites")
                        .header("Authorization", "Bearer " + favoriteToken)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].title").value("Redis 高频题"));

        mockMvc.perform(delete("/api/v1/articles/" + articleId + "/favorite")
                        .header("Authorization", "Bearer " + favoriteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isFavorited").value(false))
                .andExpect(jsonPath("$.data.favoriteCount").value(0));
    }
}
