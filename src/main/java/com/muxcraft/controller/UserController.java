package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.dto.PasswordUpdateDTO;
import com.muxcraft.dto.ProfileUpdateDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.UserService;
import com.muxcraft.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 接口：PUT /user/profile、PUT /user/password
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(userService.updateProfile(loginUser.getId(), dto));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        userService.updatePassword(loginUser.getId(), dto);
        return Result.success();
    }
}