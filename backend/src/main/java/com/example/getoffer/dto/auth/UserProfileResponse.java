package com.example.getoffer.dto.auth;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private LocalDateTime registerTime;
    private UserStatsResponse stats;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }

    public UserStatsResponse getStats() {
        return stats;
    }

    public void setStats(UserStatsResponse stats) {
        this.stats = stats;
    }
}
