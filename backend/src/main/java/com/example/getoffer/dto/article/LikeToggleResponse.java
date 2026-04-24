package com.example.getoffer.dto.article;

public class LikeToggleResponse {

    private Long articleId;
    private boolean liked;
    private long likeCount;

    public LikeToggleResponse() {
    }

    public LikeToggleResponse(Long articleId, boolean liked, long likeCount) {
        this.articleId = articleId;
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
