package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.dto.LoginDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.AuthService;
import com.muxcraft.vo.LoginVO;
import com.muxcraft.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 认证控制器
 * 接口：POST /auth/register、POST /auth/login、GET /auth/me
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<LoginVO> register(
            @RequestParam("userId") String userId,
            @RequestParam("nickname") String nickname,
            @RequestParam("password") String password,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        return Result.success("registered", authService.register(userId, nickname, password, avatar));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("login success", authService.login(dto));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(authService.me(loginUser.getId()));
    }
}