package com.example.getoffer.dto.user;

public class UpdateProfileRequest {

    private String nickname;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
