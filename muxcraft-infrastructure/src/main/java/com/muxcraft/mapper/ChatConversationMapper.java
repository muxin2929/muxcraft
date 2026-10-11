package com.muxcraft.mapper;

import com.muxcraft.entity.ChatConversation;
import com.muxcraft.vo.ConversationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatConversationMapper {

    int upsert(ChatConversation conversation);

    List<ConversationVO> selectConversations(@Param("userId") Long userId);

    ChatConversation selectByPair(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    int updateHidden(@Param("userId") Long userId,
                     @Param("otherUserId") Long otherUserId,
                     @Param("hidden") Integer hidden);

    int updatePinned(@Param("userId") Long userId,
                     @Param("otherUserId") Long otherUserId,
                     @Param("pinned") Integer pinned);
}