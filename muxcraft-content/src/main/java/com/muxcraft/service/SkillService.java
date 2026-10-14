package com.muxcraft.service;

import com.muxcraft.dto.SkillDTO;
import com.muxcraft.entity.Skill;
import com.muxcraft.vo.SkillVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 技术栈服务
 */
public interface SkillService {

    List<SkillVO> list();

    Skill create(SkillDTO dto, MultipartFile icon);

    Skill update(Long id, SkillDTO dto, MultipartFile icon);

    void delete(Long id);

    Skill getById(Long id);

    List<Skill> adminList();
}