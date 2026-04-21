package com.example.getoffer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.service.article.ArticleService;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final ArticleService articleService;

    public UserController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<ArticleSummaryResponse>> myArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type) {
        return ApiResponse.success(articleService.getMyArticles(page, pageSize, type));
    }
}
