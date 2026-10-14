package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.LoginDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.JwtCookieProperties;
import com.muxcraft.security.JwtUtil;
import com.muxcraft.security.LoginUser;
import com.muxcraft.security.TurnstileService;
import com.muxcraft.service.AuthService;
import com.muxcraft.vo.LoginVO;
import com.muxcraft.vo.UserVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 认证控制器
 * 接口：POST /auth/register、POST /auth/login、GET /auth/me、POST /auth/refresh、POST /auth/logout
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieProperties cookieProperties;
    private final JwtUtil jwtUtil;
    private final TurnstileService turnstileService;

    public AuthController(AuthService authService, JwtCookieProperties cookieProperties,
                          JwtUtil jwtUtil, TurnstileService turnstileService) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
        this.jwtUtil = jwtUtil;
        this.turnstileService = turnstileService;
    }

    @PostMapping("/register")
    public Result<LoginVO> register(
            @RequestParam("userId") String userId,
            @RequestParam("nickname") String nickname,
            @RequestParam("password") String password,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            HttpServletResponse response) {
        LoginVO vo = authService.register(userId, nickname, password, avatar);
        setRefreshCookie(response, vo.getRefreshToken());
        return Result.success("registered", vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        LoginVO vo = authService.login(dto);
        setRefreshCookie(response, vo.getRefreshToken());
        return Result.success("login success", vo);
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readRefreshCookie(request);
        LoginVO vo = authService.refresh(refreshToken);
        setRefreshCookie(response, vo.getRefreshToken());
        return Result.success("refresh success", vo);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        authService.logout();
        clearRefreshCookie(response);
        return Result.<Void>success("logout success", null);
    }

    @PostMapping("/verify-turnstile")
    public Result<Void> verifyTurnstile(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String token = body.get("cf-turnstile-response");
        if (!turnstileService.verify(token)) {
            return Result.fail(ResultCode.BAD_REQUEST, "人机验证失败，请刷新后重试");
        }
        ResponseCookie cookie = ResponseCookie.from("cf_verified", "1")
                .httpOnly(true)
                .path("/")
                .maxAge(7200)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Result.<Void>success("verify success", null);
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(authService.me(loginUser.getId()));
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getName(), refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(cookieProperties.getPath())
                .maxAge(jwtUtil.getRefreshExpireSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getName(), "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(cookieProperties.getPath())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieProperties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}