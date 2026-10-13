package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.entity.ChatBlock;
import com.muxcraft.entity.ChatConversation;
import com.muxcraft.entity.ChatMessage;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.ChatBlockMapper;
import com.muxcraft.mapper.ChatConversationMapper;
import com.muxcraft.mapper.ChatMessageDeletedMapper;
import com.muxcraft.mapper.ChatMessageMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.ChatService;
import com.muxcraft.vo.ChatMessageVO;
import com.muxcraft.vo.ConversationVO;
import com.muxcraft.websocket.WebSocketSessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatBlockMapper chatBlockMapper;
    private final ChatConversationMapper chatConversationMapper;
    private final ChatMessageDeletedMapper chatMessageDeletedMapper;
    private final UserMapper userMapper;
    private final WebSocketSessionRegistry sessionRegistry;

    public ChatServiceImpl(ChatMessageMapper chatMessageMapper, ChatBlockMapper chatBlockMapper,
                           ChatConversationMapper chatConversationMapper,
                           ChatMessageDeletedMapper chatMessageDeletedMapper, UserMapper userMapper,
                           WebSocketSessionRegistry sessionRegistry) {
        this.chatMessageMapper = chatMessageMapper;
        this.chatBlockMapper = chatBlockMapper;
        this.chatConversationMapper = chatConversationMapper;
        this.chatMessageDeletedMapper = chatMessageDeletedMapper;
        this.userMapper = userMapper;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public List<ConversationVO> conversations(Long currentUserId) {
        return chatConversationMapper.selectConversations(currentUserId);
    }

    @Override
    public PageResult<ChatMessageVO> messages(Long currentUserId, Long otherUserId, int page, int size) {
        User other = userMapper.selectById(otherUserId);
        if (other == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        int offset = (page - 1) * size;
        List<ChatMessage> list = chatMessageMapper.selectByPair(currentUserId, otherUserId, currentUserId, offset, size);
        long total = chatMessageMapper.countByPair(currentUserId, otherUserId, currentUserId);
        List<ChatMessageVO> records = list.stream().map(this::toVO).toList();
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public ChatMessageVO sendMessage(Long fromUserId, Long toUserId, String content) {
        if (fromUserId.equals(toUserId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能给自己发消息");
        }
        User toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BizException(ResultCode.NOT_FOUND, "接收用户不存在");
        }
        boolean blockedByMe = chatBlockMapper.selectByPair(fromUserId, toUserId) != null;
        boolean blockedByOther = chatBlockMapper.selectByPair(toUserId, fromUserId) != null;
        if (blockedByMe && blockedByOther) {
            throw new BizException(ResultCode.FORBIDDEN, "你们已互相屏蔽，无法聊天");
        }
        if (blockedByMe) {
            throw new BizException(ResultCode.FORBIDDEN, "你已屏蔽对方，无法聊天");
        }
        if (blockedByOther) {
            throw new BizException(ResultCode.FORBIDDEN, "对方已屏蔽你，无法聊天");
        }
        LocalDateTime now = LocalDateTime.now();
        ChatMessage message = new ChatMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setCreatedAt(now);
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        upsertConversation(fromUserId, toUserId, message.getId(), now);
        upsertConversation(toUserId, fromUserId, message.getId(), now);

        ChatMessageVO vo = toVO(message);
        sessionRegistry.sendMessage(toUserId, vo);
        return vo;
    }

    @Override
    public boolean blockUser(Long userId, Long blockedId) {
        if (userId.equals(blockedId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能屏蔽自己");
        }
        User self = userMapper.selectById(userId);
        if (self == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(self.getRole())) {
            throw new BizException(ResultCode.FORBIDDEN, "管理员不能屏蔽别人");
        }
        User target = userMapper.selectById(blockedId);
        if (target == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(target.getRole())) {
            throw new BizException(ResultCode.FORBIDDEN, "管理员不能被屏蔽");
        }
        ChatBlock block = new ChatBlock();
        block.setBlockerId(userId);
        block.setBlockedId(blockedId);
        chatBlockMapper.insert(block);
        return true;
    }

    @Override
    public boolean unblockUser(Long userId, Long blockedId) {
        if (userId.equals(blockedId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "参数错误");
        }
        chatBlockMapper.deleteByPair(userId, blockedId);
        return true;
    }

    @Override
    public List<Long> getBlockedIds(Long userId) {
        return chatBlockMapper.selectBlockedIds(userId);
    }

    @Override
    @Transactional
    public void deleteConversation(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "参数错误");
        }
        User other = userMapper.selectById(otherUserId);
        if (other == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        ChatConversation existing = chatConversationMapper.selectByPair(currentUserId, otherUserId);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "会话不存在");
        }
        List<Long> allIds = chatMessageMapper.selectAllIdsByPair(currentUserId, otherUserId);
        if (!allIds.isEmpty()) {
            softDeleteMessages(allIds, currentUserId);
        }
        chatConversationMapper.updateHidden(currentUserId, otherUserId, 1);
    }

    @Override
    public void pinConversation(Long currentUserId, Long otherUserId, boolean pinned) {
        if (currentUserId.equals(otherUserId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "参数错误");
        }
        User other = userMapper.selectById(otherUserId);
        if (other == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        ChatConversation existing = chatConversationMapper.selectByPair(currentUserId, otherUserId);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "会话不存在");
        }
        chatConversationMapper.updatePinned(currentUserId, otherUserId, pinned ? 1 : 0);
    }

    @Override
    @Transactional
    public void deleteMessages(Long currentUserId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "ids 不能为空");
        }
        Set<Long> idSet = new HashSet<>(ids);
        List<Long> uniqueIds = new ArrayList<>(idSet);
        softDeleteMessages(uniqueIds, currentUserId);
        refreshConversationLastMessage(currentUserId, idSet);
    }

    @Override
    public boolean markAsRead(Long currentUserId, Long fromUserId) {
        if (currentUserId.equals(fromUserId)) {
            return true;
        }
        chatMessageMapper.markAsRead(fromUserId, currentUserId);
        return true;
    }

    private void softDeleteMessages(List<Long> messageIds, Long currentUserId) {
        chatMessageDeletedMapper.insertBatch(messageIds, currentUserId);
        List<Long> bothDeleted = chatMessageDeletedMapper.selectBothDeletedMessageIds(messageIds);
        if (!bothDeleted.isEmpty()) {
            chatMessageMapper.deleteByIds(bothDeleted);
            chatMessageDeletedMapper.deleteByMessageIds(bothDeleted);
        }
    }

    private void refreshConversationLastMessage(Long currentUserId, Set<Long> deletedIds) {
        List<ConversationVO> conversations = chatConversationMapper.selectConversations(currentUserId);
        for (ConversationVO conv : conversations) {
            Long otherUserId = conv.getUserId();
            ChatConversation cc = chatConversationMapper.selectByPair(currentUserId, otherUserId);
            if (cc == null || cc.getLastMessageId() == null) {
                continue;
            }
            if (!deletedIds.contains(cc.getLastMessageId())) {
                continue;
            }
            ChatMessage latest = chatMessageMapper.selectLastVisibleByPair(currentUserId, otherUserId, currentUserId);
            if (latest != null) {
                cc.setLastMessageId(latest.getId());
                cc.setLastAt(latest.getCreatedAt());
            } else {
                cc.setLastMessageId(null);
                cc.setLastAt(null);
            }
            chatConversationMapper.upsert(cc);
        }
    }

    private void upsertConversation(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastAt) {
        ChatConversation conv = new ChatConversation();
        conv.setUserId(userId);
        conv.setOtherUserId(otherUserId);
        conv.setHidden(0);
        conv.setPinned(0);
        conv.setLastMessageId(lastMessageId);
        conv.setLastAt(lastAt);
        chatConversationMapper.upsert(conv);
    }

    private ChatMessageVO toVO(ChatMessage m) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(m.getId());
        vo.setFromUserId(m.getFromUserId());
        vo.setToUserId(m.getToUserId());
        vo.setContent(m.getContent());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }
}