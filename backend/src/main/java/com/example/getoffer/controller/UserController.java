package com.example.getoffer.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.dto.auth.UserProfileResponse;
import com.example.getoffer.dto.user.UpdateProfileRequest;
import com.example.getoffer.service.article.ArticleService;
import com.example.getoffer.service.user.UserProfileService;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final ArticleService articleService;
    private final UserProfileService userProfileService;

    public UserController(ArticleService articleService, UserProfileService userProfileService) {
        this.articleService = articleService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<ArticleSummaryResponse>> myArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type) {
        return ApiResponse.success(articleService.getMyArticles(page, pageSize, type));
    }

    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(userProfileService.updateProfile(username, request));
    }
}
