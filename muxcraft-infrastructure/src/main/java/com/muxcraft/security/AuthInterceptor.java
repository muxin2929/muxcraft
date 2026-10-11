package com.muxcraft.security;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JWT 认证拦截器，解析 Bearer Token 并注入 LoginUser
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER_ATTR = "loginUser";
    private static final ThreadLocal<LoginUser> LOGIN_USER_THREAD_LOCAL = new ThreadLocal<>();

    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/site",
            "/profile",
            "/experiences",
            "/skills",
            "/projects",
            "/projects/*",
            "/projects/*/comments",
            "/guestbook",
            "/articles",
            "/articles/*",
            "/achievements"
    );

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                LoginUser loginUser = jwtUtil.getLoginUser(token);
                LOGIN_USER_THREAD_LOCAL.set(loginUser);
                request.setAttribute(LOGIN_USER_ATTR, loginUser);
            } catch (JwtException | IllegalArgumentException e) {
                if (!isPublicGet(request)) {
                    throw new BizException(ResultCode.UNAUTHORIZED, "Token无效或已过期");
                }
            }
        }

        if (isPublicGet(request)) {
            return true;
        }

        if (header == null || !header.startsWith("Bearer ")) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }
        return true;
    }

    private boolean isPublicGet(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            uri = uri.substring(contextPath.length());
        }
        for (String pattern : PUBLIC_GET_PATHS) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LOGIN_USER_THREAD_LOCAL.remove();
    }

    public static LoginUser currentUser() {
        return LOGIN_USER_THREAD_LOCAL.get();
    }
}