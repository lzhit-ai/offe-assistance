package com.example.getoffer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.getoffer.common.ApiResponse;
import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.admin.AdminAiSessionResponse;
import com.example.getoffer.dto.admin.AdminDashboardResponse;
import com.example.getoffer.dto.admin.AdminUserResponse;
import com.example.getoffer.service.admin.AdminAiSessionService;
import com.example.getoffer.service.admin.AdminDashboardService;
import com.example.getoffer.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AdminUserService adminUserService;
    private final AdminAiSessionService adminAiSessionService;

    public AdminController(AdminDashboardService adminDashboardService,
                           AdminUserService adminUserService,
                           AdminAiSessionService adminAiSessionService) {
        this.adminDashboardService = adminDashboardService;
        this.adminUserService = adminUserService;
        this.adminAiSessionService = adminAiSessionService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard() {
        return ApiResponse.success(adminDashboardService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<AdminUserResponse>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminUserService.getUsers(page, pageSize));
    }

    @GetMapping("/ai/sessions")
    public ApiResponse<PageResult<AdminAiSessionResponse>> aiSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(adminAiSessionService.getSessions(page, pageSize));
    }
}
