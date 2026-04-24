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
import com.example.getoffer.repository.LikeRepository;
import com.example.getoffer.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ArticleRepository articleRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikeRepository likeRepository;
    private final LoginRateLimitService loginRateLimitService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       ArticleRepository articleRepository,
                       FavoriteRepository favoriteRepository,
                       LikeRepository likeRepository,
                       LoginRateLimitService loginRateLimitService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.articleRepository = articleRepository;
        this.favoriteRepository = favoriteRepository;
        this.likeRepository = likeRepository;
        this.loginRateLimitService = loginRateLimitService;
    }

    public AuthPayload register(RegisterRequest request) {
        validateRegisterRequest(request);
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthPayload(token, toUserProfile(user));
    }

    public AuthPayload login(LoginRequest request, String clientIp) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("username and password are required");
        }

        loginRateLimitService.checkAllowed(request.getUsername(), clientIp);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception ex) {
            loginRateLimitService.recordFailure(request.getUsername(), clientIp);
            throw new BadCredentialsException("invalid credentials");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("invalid credentials"));

        loginRateLimitService.recordSuccess(request.getUsername(), clientIp);
        String token = jwtService.generateToken(user.getUsername());
        return new AuthPayload(token, toUserProfile(user));
    }

    public UserProfileResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
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
        response.setRole(user.getRole());
        response.setRegisterTime(user.getRegisterTime());
        response.setStats(new UserStatsResponse(
                articleRepository.countByAuthorId(user.getId()),
                favoriteRepository.countByUserId(user.getId()),
                likeRepository.countByArticleAuthorId(user.getId())
        ));
        return response;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPhone()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("username, phone, and password are required");
        }
        if (request.getUsername().trim().length() < 3 || request.getUsername().trim().length() > 20) {
            throw new IllegalArgumentException("username length must be between 3 and 20");
        }
        if (!request.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("invalid phone number");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
