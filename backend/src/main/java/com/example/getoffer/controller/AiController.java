package com.example.getoffer.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.ai.AiChatMessageRequest;
import com.example.getoffer.dto.ai.AiCreateSessionRequest;
import com.example.getoffer.dto.ai.AiMessageResponse;
import com.example.getoffer.dto.ai.AiSessionResponse;
import com.example.getoffer.service.ai.AiChatService;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiChatService aiChatService;

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/sessions")
    public ApiResponse<PageResult<AiSessionResponse>> listSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(aiChatService.listSessions(page, pageSize));
    }

    @PostMapping("/sessions")
    public ApiResponse<AiSessionResponse> createSession(@RequestBody(required = false) AiCreateSessionRequest request) {
        return ApiResponse.success(aiChatService.createSession(request));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<PageResult<AiMessageResponse>> listMessages(@PathVariable Long sessionId,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "50") int pageSize) {
        return ApiResponse.success(aiChatService.listMessages(sessionId, page, pageSize));
    }

    @PostMapping(path = "/sessions/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessages(@PathVariable Long sessionId,
                                     @RequestBody AiChatMessageRequest request) {
        return aiChatService.streamMessage(sessionId, request);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Map<String, Object>> deleteSession(@PathVariable Long sessionId) {
        return ApiResponse.success(aiChatService.deleteSession(sessionId));
    }
}
