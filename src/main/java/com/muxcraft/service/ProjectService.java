package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.ProjectDTO;
import com.muxcraft.entity.Project;
import com.muxcraft.vo.ProjectDetailVO;
import com.muxcraft.vo.ProjectVO;

/**
 * 项目服务
 */
public interface ProjectService {

    PageResult<ProjectVO> page(int page, int size, String keyword, Long skillId);

    ProjectDetailVO detail(Long id);

    ProjectVO create(ProjectDTO dto);

    ProjectVO update(Long id, ProjectDTO dto);

    void delete(Long id);

    Project getById(Long id);

    PageResult<Project> adminPage(int page, int size);
}