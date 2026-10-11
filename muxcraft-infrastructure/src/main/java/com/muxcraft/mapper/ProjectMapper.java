package com.muxcraft.mapper;

import com.muxcraft.entity.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目 Mapper
 */
public interface ProjectMapper {

    int insert(Project project);

    Project selectById(@Param("id") Long id);

    List<Project> selectPage(@Param("offset") int offset, @Param("size") int size,
                             @Param("keyword") String keyword, @Param("skillId") Long skillId);

    long countPage(@Param("keyword") String keyword, @Param("skillId") Long skillId);

    int update(Project project);

    int deleteById(@Param("id") Long id);

    long countEnabled();

    long countAll();

    List<Project> selectAllAdmin();

    List<Project> selectAdminPage(@Param("offset") int offset, @Param("size") int size);
}