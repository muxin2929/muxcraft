package com.muxcraft.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxcraft.common.JsonUtils;
import com.muxcraft.vo.ChatMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话注册表，支持同一用户多端登录，所有连接都能收到推送
 */
@Component
public class WebSocketSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(userId);
            }
        }
    }

    public void sendMessage(Long userId, ChatMessageVO vo) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty()) {
            return;
        }
        try {
            ObjectMapper mapper = JsonUtils.getObjectMapper();
            String payload = mapper.writeValueAsString(Map.of(
                    "type", "message",
                    "fromUserId", vo.getFromUserId(),
                    "toUserId", vo.getToUserId(),
                    "content", vo.getContent(),
                    "createdAt", vo.getCreatedAt()
            ));
            TextMessage message = new TextMessage(payload);
            for (WebSocketSession session : set) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("Failed to send message to userId={}, session={}: {}", userId, session.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize message for userId={}", userId, e);
        }
    }
}