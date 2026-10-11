package com.muxcraft.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageDeletedMapper {

    int insertBatch(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);

    List<Long> selectDeletedIds(@Param("userId") Long userId);

    List<Long> selectBothDeletedMessageIds(@Param("messageIds") List<Long> messageIds);

    int deleteByMessageIds(@Param("messageIds") List<Long> messageIds);
}