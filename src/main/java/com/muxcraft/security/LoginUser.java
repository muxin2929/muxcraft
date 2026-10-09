package com.muxcraft.security;

/**
 * 当前登录用户上下文，由 JWT 拦截器解析后放入 ThreadLocal
 */
public class LoginUser {

    private Long id;
    private String userId;
    private String nickname;
    private String avatarUrl;
    private String role;

    public LoginUser() {
    }

    public LoginUser(Long id, String userId, String nickname, String avatarUrl, String role) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}