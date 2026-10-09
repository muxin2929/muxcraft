package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

/**
 * 修改个人资料请求
 */
public class ProfileUpdateDTO {

    @Size(max = 32, message = "昵称最长32位")
    private String nickname;

    @JsonAlias("avatar_url")
    private String avatarUrl;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}