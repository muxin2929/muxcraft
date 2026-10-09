package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.ExperienceDTO;
import com.muxcraft.entity.Experience;
import com.muxcraft.vo.ExperienceVO;

/**
 * 经历服务
 */
public interface ExperienceService {

    PageResult<ExperienceVO> page(int page, int size);

    ExperienceVO create(ExperienceDTO dto);

    ExperienceVO update(Long id, ExperienceDTO dto);

    void delete(Long id);

    Experience getById(Long id);

    PageResult<Experience> adminPage(int page, int size);
}