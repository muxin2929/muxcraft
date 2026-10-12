package com.muxcraft.entity;

import java.time.LocalDateTime;

/**
 * 留言板表
 */
public class Guestbook {

    private Long id;
    private Long userId;
    private Long replyToId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean blockedByMe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyToId) {
        this.replyToId = replyToId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(Boolean blockedByMe) {
        this.blockedByMe = blockedByMe;
    }
}