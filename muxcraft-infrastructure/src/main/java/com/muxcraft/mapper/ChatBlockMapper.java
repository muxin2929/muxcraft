package com.muxcraft.mapper;

import com.muxcraft.entity.ChatBlock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatBlockMapper {

    int insert(ChatBlock block);

    int deleteByPair(@Param("userId") Long userId, @Param("blockedId") Long blockedId);

    ChatBlock selectByPair(@Param("userId") Long userId, @Param("blockedId") Long blockedId);

    List<Long> selectBlockedIds(@Param("userId") Long userId);

    boolean existsAnyDirection(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}