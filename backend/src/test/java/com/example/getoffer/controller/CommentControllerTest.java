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
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void usersCanCommentListAndDeleteTheirOwnComments() throws Exception {
        String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "comment_author", "13800138150", "123456");
        String readerToken = TestAuthHelper.registerAndGetToken(mockMvc, "comment_reader", "13800138151", "123456");

        String createArticleResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "评论测试文章",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Spring"],
                                  "content": "# comments"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createArticleResponse, "$.data.id").toString();
        articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        String createCommentResponse = mockMvc.perform(post("/api/v1/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "这篇文章写得很清楚"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content").value("这篇文章写得很清楚"))
                .andExpect(jsonPath("$.data.author.username").value("comment_reader"))
                .andExpect(jsonPath("$.data.canDelete").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String commentId = JsonTestUtils.readJsonPath(createCommentResponse, "$.data.id").toString();

        mockMvc.perform(get("/api/v1/articles/" + articleId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].content").value("这篇文章写得很清楚"))
                .andExpect(jsonPath("$.data[0].author.username").value("comment_reader"));

        mockMvc.perform(get("/api/v1/articles/" + articleId)
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentCount").value(1));

        mockMvc.perform(delete("/api/v1/articles/" + articleId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.commentCount").value(0));
    }

    @Test
    void usersCannotDeleteOtherUsersComments() throws Exception {
        String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "comment_guard_author", "13800138152", "123456");
        String readerToken = TestAuthHelper.registerAndGetToken(mockMvc, "comment_guard_reader", "13800138153", "123456");
        String anotherToken = TestAuthHelper.registerAndGetToken(mockMvc, "comment_guard_other", "13800138154", "123456");

        String createArticleResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "评论权限文章",
                                  "category": "前端",
                                  "type": 1,
                                  "tags": ["Vue"],
                                  "content": "# permissions"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createArticleResponse, "$.data.id").toString();
        articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
            article.setStatus("APPROVED");
            articleRepository.save(article);
        });

        String createCommentResponse = mockMvc.perform(post("/api/v1/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "我先留个言"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String commentId = JsonTestUtils.readJsonPath(createCommentResponse, "$.data.id").toString();

        mockMvc.perform(delete("/api/v1/articles/" + articleId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isBadRequest());
    }
}
