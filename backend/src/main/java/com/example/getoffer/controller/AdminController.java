package com.example.getoffer.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.admin.AdminAiSessionResponse;
import com.example.getoffer.dto.admin.AdminArticleResponse;
import com.example.getoffer.dto.admin.AdminDashboardResponse;
import com.example.getoffer.dto.admin.AdminUserRoleUpdateRequest;
import com.example.getoffer.dto.admin.AdminUserResponse;
import com.example.getoffer.service.admin.AdminAiSessionService;
import com.example.getoffer.service.admin.AdminArticleService;
import com.example.getoffer.service.admin.AdminDashboardService;
import com.example.getoffer.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AdminUserService adminUserService;
    private final AdminAiSessionService adminAiSessionService;
    private final AdminArticleService adminArticleService;

    public AdminController(AdminDashboardService adminDashboardService,
                           AdminUserService adminUserService,
                           AdminAiSessionService adminAiSessionService,
                           AdminArticleService adminArticleService) {
        this.adminDashboardService = adminDashboardService;
        this.adminUserService = adminUserService;
        this.adminAiSessionService = adminAiSessionService;
        this.adminArticleService = adminArticleService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard() {
        return ApiResponse.success(adminDashboardService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<AdminUserResponse>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminUserService.getUsers(page, pageSize));
    }

    @PatchMapping("/users/{userId}/role")
    public ApiResponse<AdminUserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestBody AdminUserRoleUpdateRequest request) {
        return ApiResponse.success(adminUserService.updateUserRole(userId, request.getRole()));
    }

    @GetMapping("/ai/sessions")
    public ApiResponse<PageResult<AdminAiSessionResponse>> aiSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminAiSessionService.getSessions(page, pageSize));
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<AdminArticleResponse>> articles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminArticleService.getArticles(page, pageSize, keyword, category, status));
    }

    @PatchMapping("/articles/{articleId}/approve")
    public ApiResponse<AdminArticleResponse> approve(@PathVariable Long articleId) {
        return ApiResponse.success(adminArticleService.approve(articleId));
    }

    @PatchMapping("/articles/{articleId}/reject")
    public ApiResponse<AdminArticleResponse> reject(@PathVariable Long articleId) {
        return ApiResponse.success(adminArticleService.reject(articleId));
    }

    @DeleteMapping("/articles/{articleId}")
    public ApiResponse<Map<String, Object>> deleteArticle(@PathVariable Long articleId) {
        return ApiResponse.success(adminArticleService.delete(articleId));
    }
}
