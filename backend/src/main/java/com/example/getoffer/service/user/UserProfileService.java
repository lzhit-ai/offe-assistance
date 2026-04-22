package com.example.getoffer.service.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.getoffer.dto.auth.UserProfileResponse;
import com.example.getoffer.dto.user.AvatarUploadResponse;
import com.example.getoffer.dto.user.UpdateProfileRequest;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.UserRepository;
import com.example.getoffer.service.auth.AuthService;

@Service
public class UserProfileService {

    private static final int MAX_NICKNAME_LENGTH = 10;
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

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

    public AvatarUploadResponse uploadAvatar(String username, MultipartFile file) {
        validateAvatarFile(file);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        try {
            Path avatarDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("avatars");
            Files.createDirectories(avatarDir);

            String filename = buildAvatarFilename(user.getId(), file.getOriginalFilename());
            Path target = avatarDir.resolve(filename);
            file.transferTo(target);

            String avatarPath = "/uploads/avatars/" + filename;
            user.setAvatar(avatarPath);
            userRepository.save(user);
            return new AvatarUploadResponse(avatarPath);
        } catch (IOException ex) {
            throw new IllegalStateException("头像上传失败", ex);
        }
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

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("头像文件不能为空");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("请上传图片文件");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("头像大小不能超过 2MB");
        }
    }

    private String buildAvatarFilename(Long userId, String originalFilename) {
        String extension = "";
        if (originalFilename != null) {
            int index = originalFilename.lastIndexOf('.');
            if (index >= 0) {
                extension = originalFilename.substring(index);
            }
        }

        return userId + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
