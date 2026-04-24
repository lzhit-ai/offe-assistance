package com.example.getoffer.dto.comment;

public class ArticleCommentDeleteResponse {

    private Long commentId;
    private long commentCount;

    public ArticleCommentDeleteResponse() {
    }

    public ArticleCommentDeleteResponse(Long commentId, long commentCount) {
        this.commentId = commentId;
        this.commentCount = commentCount;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }
}
