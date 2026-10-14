package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求
 */
public class PasswordUpdateDTO {

    @NotBlank(message = "请输入原密码")
    @JsonAlias("old_password")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    @JsonAlias("new_password")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}