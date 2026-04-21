package com.example.getoffer.dto.auth;

public class UserStatsResponse {

    private long articleCount;
    private long favoriteCount;
    private long likeCount;

    public UserStatsResponse() {
    }

    public UserStatsResponse(long articleCount, long favoriteCount, long likeCount) {
        this.articleCount = articleCount;
        this.favoriteCount = favoriteCount;
        this.likeCount = likeCount;
    }

    public long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(long articleCount) {
        this.articleCount = articleCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
