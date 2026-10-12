package com.muxcraft.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.muxcraft.common.BizException;
import com.muxcraft.common.JsonUtils;
import com.muxcraft.security.JwtUtil;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final WebSocketSessionRegistry sessionRegistry;

    public ChatWebSocketHandler(JwtUtil jwtUtil, ChatService chatService, WebSocketSessionRegistry sessionRegistry) {
        this.jwtUtil = jwtUtil;
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LoginUser loginUser = resolveUser(session);
        if (loginUser == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        session.getAttributes().put("loginUser", loginUser);
        sessionRegistry.addSession(loginUser.getId(), session);
        log.info("WebSocket connected: userId={}, session={}", loginUser.getId(), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LoginUser loginUser = (LoginUser) session.getAttributes().get("loginUser");
        if (loginUser == null) {
            sendError(session, "未登录");
            return;
        }
        try {
            JsonNode node = JsonUtils.getObjectMapper().readTree(message.getPayload());
            String type = node.path("type").asText("");
            if (!"message".equals(type)) {
                sendError(session, "不支持的消息类型");
                return;
            }
            Long toUserId = node.path("toUserId").asLong();
            String content = node.path("content").asText("");
            if (toUserId == 0 || content.isBlank()) {
                sendError(session, "参数错误");
                return;
            }
            chatService.sendMessage(loginUser.getId(), toUserId, content);
        } catch (BizException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            log.error("WebSocket handle error", e);
            sendError(session, "消息处理失败");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
        log.warn("WebSocket transport error: {}", exception.getMessage());
    }

    private LoginUser resolveUser(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return jwtUtil.getLoginUser(token);
        } catch (Exception e) {
            log.warn("WebSocket invalid token: {}", e.getMessage());
            return null;
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            String payload = JsonUtils.getObjectMapper().writeValueAsString(Map.of("type", "error", "message", message));
            session.sendMessage(new TextMessage(payload));
        }
    }

    private void removeSession(WebSocketSession session) {
        LoginUser loginUser = (LoginUser) session.getAttributes().get("loginUser");
        if (loginUser != null) {
            sessionRegistry.removeSession(loginUser.getId(), session);
            log.info("WebSocket disconnected: userId={}, session={}", loginUser.getId(), session.getId());
        }
    }
}