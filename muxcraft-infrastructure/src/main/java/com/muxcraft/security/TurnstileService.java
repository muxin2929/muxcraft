package com.muxcraft.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.muxcraft.common.JsonUtils;
import com.muxcraft.config.TurnstileProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class TurnstileService {

    private static final Logger log = LoggerFactory.getLogger(TurnstileService.class);

    private final TurnstileProperties properties;
    private final HttpClient httpClient;

    public TurnstileService(TurnstileProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean verify(String responseToken) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (responseToken == null || responseToken.isBlank()) {
            return false;
        }
        try {
            String body = "secret=" + URLEncoder.encode(properties.getSecretKey(), StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(responseToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getVerifyUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("Turnstile verify failed, status={}", response.statusCode());
                return false;
            }
            JsonNode node = JsonUtils.getObjectMapper().readTree(response.body());
            boolean success = node.path("success").asBoolean(false);
            if (!success) {
                log.warn("Turnstile verify failed: {}", response.body());
            }
            return success;
        } catch (Exception e) {
            log.error("Turnstile verify error", e);
            return false;
        }
    }
}