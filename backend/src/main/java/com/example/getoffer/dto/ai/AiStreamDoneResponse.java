package com.example.getoffer.dto.ai;

public class AiStreamDoneResponse {

    private Long sessionId;
    private Long assistantMessageId;
    private String content;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getAssistantMessageId() {
        return assistantMessageId;
    }

    public void setAssistantMessageId(Long assistantMessageId) {
        this.assistantMessageId = assistantMessageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
