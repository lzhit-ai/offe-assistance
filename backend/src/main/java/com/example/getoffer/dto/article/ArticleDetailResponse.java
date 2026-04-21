package com.example.getoffer.dto.article;

public class ArticleDetailResponse extends ArticleSummaryResponse {

    private String content;
    private boolean canEdit;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
