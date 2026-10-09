package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求
 */
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    @JsonAlias("user_id")
    private String userId;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}