package com.example.getoffer.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.dto.article.FavoriteToggleResponse;
import com.example.getoffer.service.article.FavoriteService;

@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/api/v1/articles/{articleId}/favorite")
    public ApiResponse<FavoriteToggleResponse> addFavorite(@PathVariable Long articleId) {
        return ApiResponse.success(favoriteService.addFavorite(articleId));
    }

    @DeleteMapping("/api/v1/articles/{articleId}/favorite")
    public ApiResponse<FavoriteToggleResponse> removeFavorite(@PathVariable Long articleId) {
        return ApiResponse.success(favoriteService.removeFavorite(articleId));
    }

    @GetMapping("/api/v1/users/me/favorites")
    public ApiResponse<PageResult<ArticleSummaryResponse>> myFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(favoriteService.getMyFavorites(page, pageSize));
    }
}
