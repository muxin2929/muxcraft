package com.muxcraft.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.muxcraft.common.JsonUtils;
import com.muxcraft.common.Result;
import com.muxcraft.common.ResultCode;
import com.muxcraft.config.TurnstileProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

public class TurnstileFilter implements Filter {

    private static final String VERIFIED_COOKIE = "cf_verified";
    private static final String TURNSTILE_FIELD = "cf-turnstile-response";

    private static final List<String> WRITE_METHODS = List.of("POST", "PUT", "DELETE", "PATCH");

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/auth/verify-turnstile",
            "/auth/refresh",
            "/auth/logout",
            "/error",
            "/uploads/**",
            "/ws/**"
    );

    private final TurnstileService turnstileService;
    private final TurnstileProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TurnstileFilter(TurnstileService turnstileService, TurnstileProperties properties) {
        this.turnstileService = turnstileService;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        if (!requiresVerification(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (isVerified(req)) {
            chain.doFilter(request, response);
            return;
        }

        String token;
        ServletRequest requestToUse = request;
        String contentType = req.getContentType();

        if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            token = req.getParameter(TURNSTILE_FIELD);
        } else if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            token = req.getParameter(TURNSTILE_FIELD);
        } else {
            CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(req);
            token = extractFromJson(wrapped.getCachedBodyAsString());
            requestToUse = wrapped;
        }

        if (!turnstileService.verify(token)) {
            writeError(res, ResultCode.FORBIDDEN, "请先完成安全验证");
            return;
        }

        chain.doFilter(requestToUse, response);
    }

    private boolean requiresVerification(HttpServletRequest req) {
        String method = req.getMethod().toUpperCase();
        if (!WRITE_METHODS.contains(method)) {
            return false;
        }
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            uri = uri.substring(contextPath.length());
        }
        for (String pattern : EXCLUDE_PATTERNS) {
            if (pathMatcher.match(pattern, uri)) {
                return false;
            }
        }
        return true;
    }

    private boolean isVerified(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (VERIFIED_COOKIE.equals(cookie.getName()) && "1".equals(cookie.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String extractFromJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = JsonUtils.getObjectMapper().readTree(body);
            return node.path(TURNSTILE_FIELD).asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeError(HttpServletResponse response, ResultCode code, String message) throws IOException {
        response.setStatus(code.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.fail(code, message);
        response.getWriter().write(JsonUtils.getObjectMapper().writeValueAsString(result));
    }
}