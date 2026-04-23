package com.example.getoffer.service.admin;

import org.springframework.stereotype.Service;

import com.example.getoffer.dto.admin.AdminDashboardResponse;
import com.example.getoffer.repository.AiSessionRepository;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.UserRepository;

@Service
public class AdminDashboardService {

    private final AdminAccessService adminAccessService;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final AiSessionRepository aiSessionRepository;

    public AdminDashboardService(AdminAccessService adminAccessService,
                                 UserRepository userRepository,
                                 ArticleRepository articleRepository,
                                 AiSessionRepository aiSessionRepository) {
        this.adminAccessService = adminAccessService;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.aiSessionRepository = aiSessionRepository;
    }

    public AdminDashboardResponse getDashboard() {
        adminAccessService.requireAdmin();
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setUserCount(userRepository.count());
        response.setArticleCount(articleRepository.count());
        response.setAiSessionCount(aiSessionRepository.count());
        return response;
    }
}
