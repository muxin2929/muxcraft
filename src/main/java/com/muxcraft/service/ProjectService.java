package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.ProjectDTO;
import com.muxcraft.entity.Project;
import com.muxcraft.vo.ProjectDetailVO;
import com.muxcraft.vo.ProjectVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 项目服务
 */
public interface ProjectService {

    PageResult<ProjectVO> page(int page, int size, String keyword, Long skillId);

    ProjectDetailVO detail(Long id);

    ProjectVO create(ProjectDTO dto, MultipartFile icon, MultipartFile cover);

    ProjectVO update(Long id, ProjectDTO dto, MultipartFile icon, MultipartFile cover);

    void delete(Long id);

    Project getById(Long id);

    PageResult<Project> adminPage(int page, int size);
}