package com.example.getoffer.service.ai;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.ai.AiChatMessageRequest;
import com.example.getoffer.dto.ai.AiCreateSessionRequest;
import com.example.getoffer.dto.ai.AiMessageResponse;
import com.example.getoffer.dto.ai.AiSessionResponse;
import com.example.getoffer.dto.ai.AiStreamDoneResponse;
import com.example.getoffer.entity.AiMessage;
import com.example.getoffer.entity.AiSession;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.AiMessageRepository;
import com.example.getoffer.repository.AiSessionRepository;
import com.example.getoffer.service.CurrentUserService;

@Service
public class AiChatService {

    private final AiSessionRepository aiSessionRepository;
    private final AiMessageRepository aiMessageRepository;
    private final CurrentUserService currentUserService;
    private final DeepSeekClient deepSeekClient;
    private final ExecutorService aiExecutorService;

    public AiChatService(AiSessionRepository aiSessionRepository,
                         AiMessageRepository aiMessageRepository,
                         CurrentUserService currentUserService,
                         DeepSeekClient deepSeekClient,
                         ExecutorService aiExecutorService) {
        this.aiSessionRepository = aiSessionRepository;
        this.aiMessageRepository = aiMessageRepository;
        this.currentUserService = currentUserService;
        this.deepSeekClient = deepSeekClient;
        this.aiExecutorService = aiExecutorService;
    }

    @Transactional
    public AiSessionResponse createSession(AiCreateSessionRequest request) {
        User currentUser = currentUserService.requireCurrentUser();
        AiSession session = new AiSession();
        session.setUser(currentUser);
        session.setTitle(resolveSessionTitle(request != null ? request.getTitle() : null, "新对话"));
        AiSession saved = aiSessionRepository.save(session);
        return toSessionResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public PageResult<AiSessionResponse> listSessions(int page, int pageSize) {
        User currentUser = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<AiSessionResponse> mapped = aiSessionRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId(), pageable)
                .map(session -> toSessionResponse(session, aiMessageRepository.findTopBySessionIdOrderByCreatedAtDescIdDesc(session.getId()).orElse(null)));
        return PageResult.from(mapped, page, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResult<AiMessageResponse> listMessages(Long sessionId, int page, int pageSize) {
        AiSession session = requireOwnedSession(sessionId);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id")));
        Page<AiMessageResponse> mapped = aiMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId(), pageable)
                .map(this::toMessageResponse);
        return PageResult.from(mapped, page, pageSize);
    }

    public SseEmitter streamMessage(Long sessionId, AiChatMessageRequest request) {
        String content = request != null ? request.getContent() : null;
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        AiSession session = requireOwnedSession(sessionId);
        AiMessage userMessage = saveUserMessage(session, content.trim());
        List<AiModelMessage> promptMessages = aiMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId).stream()
                .map(message -> new AiModelMessage(message.getRole(), message.getContent()))
                .toList();

        SseEmitter emitter = new SseEmitter(0L);
        aiExecutorService.submit(() -> handleStream(emitter, session, userMessage, promptMessages));
        return emitter;
    }

    @Transactional
    public Map<String, Object> deleteSession(Long sessionId) {
        AiSession session = requireOwnedSession(sessionId);
        aiMessageRepository.deleteBySessionId(session.getId());
        aiSessionRepository.delete(session);
        return Map.of("success", true);
    }

    private void handleStream(SseEmitter emitter,
                              AiSession session,
                              AiMessage userMessage,
                              List<AiModelMessage> promptMessages) {
        StringBuilder builder = new StringBuilder();

        try {
            deepSeekClient.streamChat(promptMessages, chunk -> {
                builder.append(chunk);
                sendEvent(emitter, "chunk", chunk);
            });

            AiMessage assistantMessage = saveAssistantMessage(session, builder.toString());
            AiStreamDoneResponse done = new AiStreamDoneResponse();
            done.setSessionId(session.getId());
            done.setAssistantMessageId(assistantMessage.getId());
            done.setContent(assistantMessage.getContent());

            sendEvent(emitter, "done", done);
            emitter.complete();
        } catch (Exception ex) {
            sendEvent(emitter, "error", ex.getMessage());
            emitter.completeWithError(ex);
        }
    }

    @Transactional
    protected AiMessage saveUserMessage(AiSession session, String content) {
        if ("新对话".equals(session.getTitle())) {
            session.setTitle(resolveSessionTitle(content, "新对话"));
        }
        session.setUpdatedAt(java.time.LocalDateTime.now());
        aiSessionRepository.save(session);

        AiMessage message = new AiMessage();
        message.setSession(session);
        message.setRole("user");
        message.setContent(content);
        return aiMessageRepository.save(message);
    }

    @Transactional
    protected AiMessage saveAssistantMessage(AiSession session, String content) {
        session.setUpdatedAt(java.time.LocalDateTime.now());
        aiSessionRepository.save(session);

        AiMessage message = new AiMessage();
        message.setSession(session);
        message.setRole("assistant");
        message.setContent(content == null ? "" : content);
        return aiMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    protected AiSession requireOwnedSession(Long sessionId) {
        User currentUser = currentUserService.requireCurrentUser();
        AiSession session = aiSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("无权访问该会话");
        }
        return session;
    }

    private AiSessionResponse toSessionResponse(AiSession session, AiMessage lastMessage) {
        AiSessionResponse response = new AiSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        response.setLastMessagePreview(lastMessage == null ? "" : toPreview(lastMessage.getContent()));
        return response;
    }

    private AiMessageResponse toMessageResponse(AiMessage message) {
        AiMessageResponse response = new AiMessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private String resolveSessionTitle(String title, String fallback) {
        String raw = title == null ? "" : title.trim();
        if (raw.isEmpty()) {
            return fallback;
        }
        return raw.length() > 30 ? raw.substring(0, 30) : raw;
    }

    private String toPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String compact = content.replaceAll("\\s+", " ").trim();
        return compact.length() > 60 ? compact.substring(0, 60) : compact;
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException ex) {
            throw new IllegalStateException("SSE 事件发送失败", ex);
        }
    }
}
