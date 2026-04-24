package com.example.getoffer.service.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginRateLimitService {

    private final int maxAttempts;
    private final long windowMs;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public LoginRateLimitService(
            @Value("${app.security.login-rate-limit.max-attempts}") int maxAttempts,
            @Value("${app.security.login-rate-limit.window-ms}") long windowMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    public void checkAllowed(String username, String clientIp) {
        String key = buildKey(username, clientIp);
        AttemptWindow attemptWindow = attempts.get(key);
        if (attemptWindow == null) {
            return;
        }

        long now = System.currentTimeMillis();
        synchronized (attemptWindow) {
            if (attemptWindow.isExpired(now, windowMs)) {
                attempts.remove(key);
                return;
            }

            if (attemptWindow.failures >= maxAttempts) {
                throw new TooManyRequestsException("too many login attempts, please try again later");
            }
        }
    }

    public void recordFailure(String username, String clientIp) {
        long now = System.currentTimeMillis();
        attempts.compute(buildKey(username, clientIp), (key, existing) -> {
            if (existing == null || existing.isExpired(now, windowMs)) {
                return new AttemptWindow(1, now);
            }

            existing.failures += 1;
            return existing;
        });
    }

    public void recordSuccess(String username, String clientIp) {
        attempts.remove(buildKey(username, clientIp));
    }

    private String buildKey(String username, String clientIp) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedIp = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim();
        return normalizedIp + "|" + normalizedUsername;
    }

    private static final class AttemptWindow {
        private int failures;
        private final long firstFailureAt;

        private AttemptWindow(int failures, long firstFailureAt) {
            this.failures = failures;
            this.firstFailureAt = firstFailureAt;
        }

        private boolean isExpired(long now, long windowMs) {
            return now - firstFailureAt >= windowMs;
        }
    }
}
