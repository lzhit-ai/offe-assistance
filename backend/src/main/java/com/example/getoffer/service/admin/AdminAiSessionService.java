package com.example.getoffer.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.admin.AdminAiSessionResponse;
import com.example.getoffer.entity.AiSession;
import com.example.getoffer.repository.AiSessionRepository;

@Service
public class AdminAiSessionService {

    private final AdminAccessService adminAccessService;
    private final AiSessionRepository aiSessionRepository;

    public AdminAiSessionService(AdminAccessService adminAccessService, AiSessionRepository aiSessionRepository) {
        this.adminAccessService = adminAccessService;
        this.aiSessionRepository = aiSessionRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<AdminAiSessionResponse> getSessions(int page, int pageSize) {
        adminAccessService.requireAdmin();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<AdminAiSessionResponse> mapped = aiSessionRepository.findAll(pageable).map(this::toResponse);
        return PageResult.from(mapped, page, pageSize);
    }

    private AdminAiSessionResponse toResponse(AiSession session) {
        AdminAiSessionResponse response = new AdminAiSessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUser().getId());
        response.setUsername(session.getUser().getUsername());
        response.setTitle(session.getTitle());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }
}
