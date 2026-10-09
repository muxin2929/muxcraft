package com.muxcraft.service;

import com.muxcraft.dto.SkillDTO;
import com.muxcraft.entity.Skill;
import com.muxcraft.vo.SkillCategoryVO;

import java.util.List;

/**
 * 技术栈服务
 */
public interface SkillService {

    List<SkillCategoryVO> listGroupedByCategory();

    Skill create(SkillDTO dto);

    Skill update(Long id, SkillDTO dto);

    void delete(Long id);

    Skill getById(Long id);

    List<Skill> adminList();
}