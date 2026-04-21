package com.example.getoffer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleDetailResponse;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.dto.article.ArticleUpsertRequest;
import com.example.getoffer.service.article.ArticleService;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<PageResult<ArticleSummaryResponse>> listArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String sort) {
        return ApiResponse.success(articleService.getArticleList(page, pageSize, type, category, tag, keyword, authorId, sort));
    }

    @GetMapping("/hot")
    public ApiResponse<List<ArticleSummaryResponse>> hotArticles(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Integer type) {
        return ApiResponse.success(articleService.getHotArticles(type, limit));
    }

    @GetMapping("/{articleId}")
    public ApiResponse<ArticleDetailResponse> getArticleDetail(@PathVariable Long articleId) {
        return ApiResponse.success(articleService.getDetail(articleId));
    }

    @PostMapping
    public ApiResponse<ArticleDetailResponse> createArticle(@RequestBody ArticleUpsertRequest request) {
        return ApiResponse.success(articleService.createArticle(request));
    }

    @PutMapping("/{articleId}")
    public ApiResponse<ArticleDetailResponse> updateArticle(@PathVariable Long articleId,
                                                            @RequestBody ArticleUpsertRequest request) {
        return ApiResponse.success(articleService.updateArticle(articleId, request));
    }
}
