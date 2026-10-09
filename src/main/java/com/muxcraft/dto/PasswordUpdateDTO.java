package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求
 */
public class PasswordUpdateDTO {

    @NotBlank(message = "旧密码不能为空")
    @JsonAlias("old_password")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, message = "新密码至少8位")
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