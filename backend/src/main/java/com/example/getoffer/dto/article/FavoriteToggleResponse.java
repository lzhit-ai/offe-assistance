package com.example.getoffer.dto.article;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteToggleResponse {

    private Long articleId;
    @JsonProperty("isFavorited")
    private boolean isFavorited;
    private long favoriteCount;

    public FavoriteToggleResponse() {
    }

    public FavoriteToggleResponse(Long articleId, boolean isFavorited, long favoriteCount) {
        this.articleId = articleId;
        this.isFavorited = isFavorited;
        this.favoriteCount = favoriteCount;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
