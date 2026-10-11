package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.vo.ChatMessageVO;
import com.muxcraft.vo.ConversationVO;

import java.util.List;

public interface ChatService {

    List<ConversationVO> conversations(Long currentUserId);

    PageResult<ChatMessageVO> messages(Long currentUserId, Long otherUserId, int page, int size);

    ChatMessageVO sendMessage(Long fromUserId, Long toUserId, String content);

    boolean blockUser(Long userId, Long blockedId);

    boolean unblockUser(Long userId, Long blockedId);

    List<Long> getBlockedIds(Long userId);

    void deleteConversation(Long currentUserId, Long otherUserId);

    void pinConversation(Long currentUserId, Long otherUserId, boolean pinned);

    void deleteMessages(Long currentUserId, List<Long> ids);
}