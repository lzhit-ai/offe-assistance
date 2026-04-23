package com.example.getoffer.dto.admin;

public class AdminDashboardResponse {

    private long userCount;
    private long articleCount;
    private long aiSessionCount;

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(long articleCount) {
        this.articleCount = articleCount;
    }

    public long getAiSessionCount() {
        return aiSessionCount;
    }

    public void setAiSessionCount(long aiSessionCount) {
        this.aiSessionCount = aiSessionCount;
    }
}
