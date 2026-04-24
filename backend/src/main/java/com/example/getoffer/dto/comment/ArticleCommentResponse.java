package com.example.getoffer.dto.comment;

import java.time.LocalDateTime;

public class ArticleCommentResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private CommentAuthorResponse author;
    private boolean canDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CommentAuthorResponse getAuthor() {
        return author;
    }

    public void setAuthor(CommentAuthorResponse author) {
        this.author = author;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}
