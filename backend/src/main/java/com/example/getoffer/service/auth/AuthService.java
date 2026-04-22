package com.example.getoffer.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.getoffer.dto.auth.AuthPayload;
import com.example.getoffer.dto.auth.LoginRequest;
import com.example.getoffer.dto.auth.RegisterRequest;
import com.example.getoffer.dto.auth.UserProfileResponse;
import com.example.getoffer.dto.auth.UserStatsResponse;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.FavoriteRepository;
import com.example.getoffer.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ArticleRepository articleRepository;
    private final FavoriteRepository favoriteRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       ArticleRepository articleRepository,
                       FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.articleRepository = articleRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public AuthPayload register(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthPayload(token, toUserProfile(user));
    }

    public AuthPayload login(LoginRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception ex) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));

        String token = jwtService.generateToken(user.getUsername());
        return new AuthPayload(token, toUserProfile(user));
    }

    public UserProfileResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toUserProfile(user);
    }

    public UserProfileResponse toUserProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setRegisterTime(user.getRegisterTime());
        response.setStats(new UserStatsResponse(
                articleRepository.countByAuthorId(user.getId()),
                favoriteRepository.countByUserId(user.getId()),
                0
        ));
        return response;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPhone()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("用户名、手机号和密码不能为空");
        }
        if (request.getUsername().trim().length() < 3 || request.getUsername().trim().length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3 到 20 之间");
        }
        if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度至少为 6 位");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
