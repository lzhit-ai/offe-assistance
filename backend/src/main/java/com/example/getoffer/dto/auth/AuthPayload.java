package com.example.getoffer.dto.auth;

public class AuthPayload {

    private String accessToken;
    private UserProfileResponse user;

    public AuthPayload() {
    }

    public AuthPayload(String accessToken, UserProfileResponse user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UserProfileResponse getUser() {
        return user;
    }

    public void setUser(UserProfileResponse user) {
        this.user = user;
    }
}
