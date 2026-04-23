package com.example.getoffer.service.admin;

import org.springframework.stereotype.Service;

import com.example.getoffer.entity.User;
import com.example.getoffer.service.CurrentUserService;

@Service
public class AdminAccessService {

    private final CurrentUserService currentUserService;

    public AdminAccessService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public User requireAdmin() {
        User user = currentUserService.requireCurrentUser();
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("无权限访问后台");
        }
        return user;
    }
}
