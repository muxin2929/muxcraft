package com.muxcraft.mapper;

import com.muxcraft.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {

    int insert(ChatMessage message);

    List<ChatMessage> selectByPair(@Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2,
                                   @Param("currentUserId") Long currentUserId,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    long countByPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2,
                     @Param("currentUserId") Long currentUserId);

    ChatMessage selectLastByPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    ChatMessage selectLastVisibleByPair(@Param("userId1") Long userId1,
                                        @Param("userId2") Long userId2,
                                        @Param("currentUserId") Long currentUserId);

    List<Long> selectAllIdsByPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    int deleteByIds(@Param("ids") List<Long> ids);
}