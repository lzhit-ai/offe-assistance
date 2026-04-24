package com.example.getoffer.service.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
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
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp");

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
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        String nickname = normalizeNickname(request == null ? null : request.getNickname());
        validateNickname(nickname, user.getId());
        user.setNickname(nickname);

        return authService.toUserProfile(userRepository.save(user));
    }

    public AvatarUploadResponse uploadAvatar(String username, MultipartFile file) {
        validateAvatarFile(file);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

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
            throw new IllegalStateException("avatar upload failed", ex);
        }
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }

    private void validateNickname(String nickname, Long userId) {
        if (nickname.isEmpty()) {
            throw new IllegalArgumentException("nickname is required");
        }

        if (nickname.codePointCount(0, nickname.length()) > MAX_NICKNAME_LENGTH) {
            throw new IllegalArgumentException("nickname must be 10 characters or fewer");
        }

        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new IllegalArgumentException("nickname already exists");
        }
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("avatar file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("please upload a PNG, JPG, or WEBP image");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("avatar file must be 2MB or smaller");
        }

        String extension = extractFileExtension(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("please upload a PNG, JPG, or WEBP image");
        }
    }

    private String buildAvatarFilename(Long userId, String originalFilename) {
        String extension = extractFileExtension(originalFilename);
        return userId + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String extractFileExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int index = originalFilename.lastIndexOf('.');
        if (index < 0) {
            return "";
        }

        return originalFilename.substring(index).toLowerCase(Locale.ROOT);
    }
}
