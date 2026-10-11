package com.muxcraft.mapper;

import com.muxcraft.entity.Achievement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成就 Mapper
 */
public interface AchievementMapper {

    int insert(Achievement achievement);

    List<Achievement> selectAll();

    int update(Achievement achievement);

    int deleteById(@Param("id") Long id);

    Achievement selectById(@Param("id") Long id);
}