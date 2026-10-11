package com.muxcraft.mapper;

import com.muxcraft.entity.ProjectSkill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目-技术栈关联 Mapper
 */
public interface ProjectSkillMapper {

    int insertBatch(@Param("list") List<ProjectSkill> list);

    int deleteByProjectId(@Param("projectId") Long projectId);

    List<Long> selectSkillIdsByProjectId(@Param("projectId") Long projectId);
}