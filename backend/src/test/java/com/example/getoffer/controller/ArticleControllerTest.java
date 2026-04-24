package com.example.getoffer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.getoffer.repository.ArticleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void articleCrudAndListEndpointsWork() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "writer1", "13800138001", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Vue3 Composition API 详解",
                                  "category": "前端",
                                  "type": 1,
                                  "tags": ["Vue3", "Composition API"],
                                  "content": "# 正文内容"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Vue3 Composition API 详解"))
                .andExpect(jsonPath("$.data.author.username").value("writer1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();
        Long createdArticleId = Long.valueOf(articleId);
        articleRepository.findById(createdArticleId).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        mockMvc.perform(get("/api/v1/articles")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("type", "1")
                        .param("category", "前端")
                        .param("tag", "Vue3")
                        .param("keyword", "Composition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].title").value("Vue3 Composition API 详解"));

        mockMvc.perform(get("/api/v1/articles/hot").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("Vue3 Composition API 详解"));

        mockMvc.perform(get("/api/v1/articles/" + articleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content").value("# 正文内容"))
                .andExpect(jsonPath("$.data.canEdit").value(true))
                .andExpect(jsonPath("$.data.isFavorited").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.liked").value(false));

        mockMvc.perform(put("/api/v1/articles/" + articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Vue3 Composition API 深入实战",
                                  "category": "前端",
                                  "type": 1,
                                  "tags": ["Vue3", "实战"],
                                  "content": "# 更新后的正文"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Vue3 Composition API 深入实战"))
                .andExpect(jsonPath("$.data.tags[1]").value("实战"));
    }

    @Test
    void publicArticleEndpointsOnlyExposeApprovedArticles() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "writer_public", "13800138123", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "不会公开",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Spring"],
                                  "content": "# hidden"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        mockMvc.perform(get("/api/v1/articles")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(0));

        mockMvc.perform(get("/api/v1/articles/hot").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/v1/articles/" + articleId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/articles/" + articleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
