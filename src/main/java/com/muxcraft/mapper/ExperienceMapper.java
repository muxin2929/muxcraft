package com.muxcraft.mapper;

import com.muxcraft.entity.Experience;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 经历 Mapper
 */
public interface ExperienceMapper {

    int insert(Experience experience);

    Experience selectById(@Param("id") Long id);

    List<Experience> selectPage(@Param("offset") int offset, @Param("size") int size);

    long countAll();

    int update(Experience experience);

    int deleteById(@Param("id") Long id);

    List<Experience> selectAll();
}