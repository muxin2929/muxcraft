package com.muxcraft.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 登录响应
 */
public class LoginVO {

    private String token;
    private long expiresIn;
    private UserVO user;

    @JsonIgnore
    private String refreshToken;

    public LoginVO() {
    }

    public LoginVO(String token, long expiresIn, UserVO user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public LoginVO(String token, long expiresIn, UserVO user, String refreshToken) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}