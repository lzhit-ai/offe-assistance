package com.example.getoffer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.example.getoffer.entity.AiSession;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.AiSessionRepository;
import com.example.getoffer.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiSessionRepository aiSessionRepository;

    @Test
    void adminEndpointsRejectNormalUsers() throws Exception {
        String token = TestAuthHelper.registerAndGetToken(mockMvc, "normal_user", "13800138125", "123456");

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanLoadDashboardUsersAndAiSessions() throws Exception {
        String adminToken = TestAuthHelper.registerAndGetToken(mockMvc, "root_admin", "13800138126", "123456");
        User adminUser = userRepository.findByUsername("root_admin").orElseThrow();
        adminUser.setRole("ADMIN");
        userRepository.save(adminUser);

        String memberToken = TestAuthHelper.registerAndGetToken(mockMvc, "writer_admin", "13800138127", "123456");
        User member = userRepository.findByUsername("writer_admin").orElseThrow();

        mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "后台审核草稿",
                                  "category": "后端",
                                  "type": 1,
                                  "tags": ["Spring"],
                                  "content": "# admin review"
                                }
                                """))
                .andExpect(status().isOk());

        AiSession session = new AiSession();
        session.setUser(member);
        session.setTitle("模拟面试");
        aiSessionRepository.save(session);

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userCount").value(2))
                .andExpect(jsonPath("$.data.articleCount").value(1))
                .andExpect(jsonPath("$.data.aiSessionCount").value(1));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].role").exists())
                .andExpect(jsonPath("$.data.total").value(2));

        mockMvc.perform(get("/api/v1/admin/ai/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].title").value("模拟面试"))
                .andExpect(jsonPath("$.data.list[0].username").value("writer_admin"));
    }

    @Test
    void adminCanApproveRejectAndDeleteArticles() throws Exception {
        String adminToken = TestAuthHelper.registerAndGetToken(mockMvc, "moderator", "13800138128", "123456");
        User adminUser = userRepository.findByUsername("moderator").orElseThrow();
        adminUser.setRole("ADMIN");
        userRepository.save(adminUser);

        String memberToken = TestAuthHelper.registerAndGetToken(mockMvc, "writer_review", "13800138129", "123456");
        String createResponse = mockMvc.perform(post("/api/v1/articles")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "需要审核的文章",
                                  "category": "前端",
                                  "type": 1,
                                  "tags": ["Vue"],
                                  "content": "# waiting"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();

        mockMvc.perform(get("/api/v1/admin/articles")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].title").value("需要审核的文章"))
                .andExpect(jsonPath("$.data.list[0].status").value("PENDING"));

        mockMvc.perform(patch("/api/v1/admin/articles/" + articleId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(patch("/api/v1/admin/articles/" + articleId + "/reject")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        mockMvc.perform(delete("/api/v1/admin/articles/" + articleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }
}
