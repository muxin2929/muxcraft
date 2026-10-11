package com.muxcraft.entity;

import java.time.LocalDateTime;

public class ChatConversation {

    private Long userId;
    private Long otherUserId;
    private Integer hidden;
    private Integer pinned;
    private Long lastMessageId;
    private LocalDateTime lastAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public Integer getHidden() {
        return hidden;
    }

    public void setHidden(Integer hidden) {
        this.hidden = hidden;
    }

    public Integer getPinned() {
        return pinned;
    }

    public void setPinned(Integer pinned) {
        this.pinned = pinned;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public LocalDateTime getLastAt() {
        return lastAt;
    }

    public void setLastAt(LocalDateTime lastAt) {
        this.lastAt = lastAt;
    }
}