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
