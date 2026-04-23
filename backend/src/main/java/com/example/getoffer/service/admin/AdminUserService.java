package com.example.getoffer.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.admin.AdminUserResponse;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.UserRepository;

@Service
public class AdminUserService {

    private final AdminAccessService adminAccessService;
    private final UserRepository userRepository;

    public AdminUserService(AdminAccessService adminAccessService, UserRepository userRepository) {
        this.adminAccessService = adminAccessService;
        this.userRepository = userRepository;
    }

    public PageResult<AdminUserResponse> getUsers(int page, int pageSize) {
        adminAccessService.requireAdmin();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<AdminUserResponse> mapped = userRepository.findAll(pageable).map(this::toResponse);
        return PageResult.from(mapped, page, pageSize);
    }

    public AdminUserResponse updateUserRole(Long userId, String role) {
        User currentAdmin = adminAccessService.requireAdmin();
        String normalizedRole = normalizeRole(role);

        if (currentAdmin.getId().equals(userId)) {
            throw new IllegalArgumentException("不能修改自己的角色");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        targetUser.setRole(normalizedRole);
        return toResponse(userRepository.save(targetUser));
    }

    private String normalizeRole(String role) {
        if ("ADMIN".equals(role) || "USER".equals(role)) {
            return role;
        }
        throw new IllegalArgumentException("角色不合法");
    }

    private AdminUserResponse toResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setRegisterTime(user.getRegisterTime());
        return response;
    }
}
