package com.example.getoffer.controller;

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

import com.example.getoffer.repository.ArticleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void registerReturnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "phone": "13800138000",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.user.username").value("alice"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.stats.articleCount").value(0));
    }

    @Test
    void loginAndMeUseJwtAuthentication() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "bob",
                                  "phone": "13900139000",
                                  "password": "abcdef"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonTestUtils.readJsonPath(registerResponse, "$.data.accessToken").toString();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "bob",
                                  "password": "abcdef"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value("bob"))
                .andExpect(jsonPath("$.data.user.role").value("USER"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("bob"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.stats.favoriteCount").value(0))
                .andExpect(jsonPath("$.data.stats.likeCount").value(0));
    }

    @Test
    void meReturnsTotalLikesReceivedByAuthorsArticles() throws Exception {
        String authorResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "stats_author",
                                  "phone": "13900139001",
                                  "password": "abcdef"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String readerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "stats_reader",
                                  "phone": "13900139002",
                                  "password": "abcdef"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String authorToken = JsonTestUtils.readJsonPath(authorResponse, "$.data.accessToken").toString();
        String readerToken = JsonTestUtils.readJsonPath(readerResponse, "$.data.accessToken").toString();

        String articleResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "获赞统计文章",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Java"],
                                  "content": "# like stats"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(articleResponse, "$.data.id").toString();
        articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stats.likeCount").value(1));
    }
}
