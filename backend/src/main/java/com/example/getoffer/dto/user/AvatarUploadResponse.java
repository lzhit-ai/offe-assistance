package com.example.getoffer.dto.user;

public class AvatarUploadResponse {

    private String avatar;

    public AvatarUploadResponse(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
