package com.muxcraft.mapper;

import com.muxcraft.entity.Skill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 技术栈 Mapper
 */
public interface SkillMapper {

    int insert(Skill skill);

    Skill selectById(@Param("id") Long id);

    List<Skill> selectAllEnabled();

    List<Skill> selectAll();

    int update(Skill skill);

    int deleteById(@Param("id") Long id);

    long countEnabled();

    List<Skill> selectByProjectId(@Param("projectId") Long projectId);
}