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
import com.example.getoffer.dto.article.LikeToggleResponse;
import com.example.getoffer.dto.comment.ArticleCommentCreateRequest;
import com.example.getoffer.dto.comment.ArticleCommentDeleteResponse;
import com.example.getoffer.dto.comment.ArticleCommentResponse;
import com.example.getoffer.service.article.ArticleLikeService;
import com.example.getoffer.service.article.ArticleCommentService;
import com.example.getoffer.service.article.ArticleService;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleLikeService articleLikeService;
    private final ArticleCommentService articleCommentService;

    public ArticleController(ArticleService articleService,
                             ArticleLikeService articleLikeService,
                             ArticleCommentService articleCommentService) {
        this.articleService = articleService;
        this.articleLikeService = articleLikeService;
        this.articleCommentService = articleCommentService;
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

    @PostMapping("/{articleId}/like")
    public ApiResponse<LikeToggleResponse> likeArticle(@PathVariable Long articleId) {
        return ApiResponse.success(articleLikeService.like(articleId));
    }

    @GetMapping("/{articleId}/comments")
    public ApiResponse<List<ArticleCommentResponse>> listComments(@PathVariable Long articleId) {
        return ApiResponse.success(articleCommentService.listComments(articleId));
    }

    @PostMapping("/{articleId}/comments")
    public ApiResponse<ArticleCommentResponse> createComment(@PathVariable Long articleId,
                                                             @RequestBody ArticleCommentCreateRequest request) {
        return ApiResponse.success(articleCommentService.createComment(articleId, request));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{articleId}/comments/{commentId}")
    public ApiResponse<ArticleCommentDeleteResponse> deleteComment(@PathVariable Long articleId,
                                                                   @PathVariable Long commentId) {
        return ApiResponse.success(articleCommentService.deleteComment(articleId, commentId));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{articleId}/like")
    public ApiResponse<LikeToggleResponse> unlikeArticle(@PathVariable Long articleId) {
        return ApiResponse.success(articleLikeService.unlike(articleId));
    }

    @PutMapping("/{articleId}")
    public ApiResponse<ArticleDetailResponse> updateArticle(@PathVariable Long articleId,
                                                            @RequestBody ArticleUpsertRequest request) {
        return ApiResponse.success(articleService.updateArticle(articleId, request));
    }
}
