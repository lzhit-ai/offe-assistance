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

import com.example.getoffer.repository.ArticleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void usersCanLikeUnlikeArticlesIncludingTheirOwn() throws Exception {
        String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_author", "13800138140", "123456");
        String readerToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_reader", "13800138141", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "可点赞的文章",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Spring"],
                                  "content": "# like"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();
        articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(2));

        mockMvc.perform(delete("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(1));
    }

    @Test
    void articleDetailAndAuthMeExposeRealLikeInformationIncludingSelfLikes() throws Exception {
        String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_profile_author", "13800138142", "123456");
        String readerToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_profile_reader", "13800138143", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "统计点赞的文章",
                                  "category": "前端",
                                  "type": 1,
                                  "tags": ["Vue"],
                                  "content": "# stats"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();
        articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/articles/" + articleId)
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(2))
                .andExpect(jsonPath("$.data.liked").value(true));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stats.likeCount").value(2));
    }
}
