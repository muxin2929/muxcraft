package com.muxcraft.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，负责签发与解析 Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-seconds}")
    private long expireSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long id, String userId, String nickname, String avatarUrl, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("avatarUrl", avatarUrl)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public LoginUser getLoginUser(String token) {
        Claims claims = parseToken(token);
        LoginUser loginUser = new LoginUser();
        loginUser.setId(Long.valueOf(claims.getSubject()));
        loginUser.setUserId(claims.get("userId", String.class));
        loginUser.setNickname(claims.get("nickname", String.class));
        loginUser.setAvatarUrl(claims.get("avatarUrl", String.class));
        loginUser.setRole(claims.get("role", String.class));
        return loginUser;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}