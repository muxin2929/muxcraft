package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.SkillDTO;
import com.muxcraft.entity.Skill;
import com.muxcraft.mapper.SkillMapper;
import com.muxcraft.service.FileService;
import com.muxcraft.service.SkillService;
import com.muxcraft.vo.SkillVO;
import com.muxcraft.vo.UploadVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 技术栈服务实现
 */
@Service
public class SkillServiceImpl implements SkillService {

    private static final Set<String> VALID_CATEGORIES = Set.of("frontend", "backend", "tool");

    private final SkillMapper skillMapper;
    private final FileService fileService;

    public SkillServiceImpl(SkillMapper skillMapper, FileService fileService) {
        this.skillMapper = skillMapper;
        this.fileService = fileService;
    }

    @Override
    public List<SkillVO> list() {
        List<Skill> skills = skillMapper.selectAllEnabled();
        List<SkillVO> result = new ArrayList<>();
        for (Skill skill : skills) {
            SkillVO vo = new SkillVO();
            vo.setId(skill.getId());
            vo.setName(skill.getName());
            vo.setIconUrl(skill.getIconUrl());
            vo.setRating(skill.getRating());
            vo.setDescription(skill.getDescription());
            result.add(vo);
        }
        return result;
    }

    @Override
    public Skill create(SkillDTO dto, MultipartFile icon) {
        validateCategory(dto.getCategory());
        Skill skill = new Skill();
        BeanUtils.copyProperties(dto, skill);
        if (skill.getStatus() == null) {
            skill.setStatus(1);
        }
        if (skill.getRating() == null) {
            skill.setRating(3);
        }
        if (skill.getSortOrder() == null) {
            skill.setSortOrder(0);
        }
        skillMapper.insert(skill);

        if (icon != null && !icon.isEmpty()) {
            UploadVO vo = fileService.upload(icon, "icons");
            skill.setIconUrl(vo.getUrl());
            skillMapper.update(skill);
        }

        return skill;
    }

    @Override
    public Skill update(Long id, SkillDTO dto, MultipartFile icon) {
        Skill existing = skillMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "技术栈不存在");
        }
        validateCategory(dto.getCategory());
        Skill skill = new Skill();
        BeanUtils.copyProperties(dto, skill);
        skill.setId(id);

        String oldIconUrl = existing.getIconUrl();
        if (icon != null && !icon.isEmpty()) {
            UploadVO vo = fileService.upload(icon, "icons");
            skill.setIconUrl(vo.getUrl());
        }

        skillMapper.update(skill);
        if (icon != null && !icon.isEmpty() && oldIconUrl != null && !oldIconUrl.isEmpty()) {
            fileService.delete(oldIconUrl);
        }
        return skillMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        Skill existing = skillMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "技术栈不存在");
        }
        String iconUrl = existing.getIconUrl();
        skillMapper.deleteById(id);
        if (iconUrl != null && !iconUrl.isEmpty()) {
            fileService.delete(iconUrl);
        }
    }

    @Override
    public Skill getById(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BizException(ResultCode.NOT_FOUND, "技术栈不存在");
        }
        return skill;
    }

    @Override
    public List<Skill> adminList() {
        return skillMapper.selectAll();
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank() || !VALID_CATEGORIES.contains(category)) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类只能是 frontend、backend、tool 之一");
        }
    }
}