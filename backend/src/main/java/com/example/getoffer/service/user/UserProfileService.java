package com.example.getoffer.service.user;

import org.springframework.stereotype.Service;

import com.example.getoffer.dto.auth.UserProfileResponse;
import com.example.getoffer.dto.user.UpdateProfileRequest;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.UserRepository;
import com.example.getoffer.service.auth.AuthService;

@Service
public class UserProfileService {

    private static final int MAX_NICKNAME_LENGTH = 10;

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserProfileService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String nickname = normalizeNickname(request == null ? null : request.getNickname());
        validateNickname(nickname, user.getId());
        user.setNickname(nickname);

        return authService.toUserProfile(userRepository.save(user));
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }

    private void validateNickname(String nickname, Long userId) {
        if (nickname.isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        if (nickname.codePointCount(0, nickname.length()) > MAX_NICKNAME_LENGTH) {
            throw new IllegalArgumentException("昵称最长 10 个字符");
        }

        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new IllegalArgumentException("nickname already exists");
        }
    }
}
