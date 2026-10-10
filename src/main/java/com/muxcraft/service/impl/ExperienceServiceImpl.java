package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.ExperienceDTO;
import com.muxcraft.entity.Experience;
import com.muxcraft.mapper.ExperienceMapper;
import com.muxcraft.service.ExperienceService;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.ExperienceVO;
import com.muxcraft.vo.UploadVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 经历服务实现
 */
@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceMapper experienceMapper;
    private final FileService fileService;

    public ExperienceServiceImpl(ExperienceMapper experienceMapper, FileService fileService) {
        this.experienceMapper = experienceMapper;
        this.fileService = fileService;
    }

    @Override
    public PageResult<ExperienceVO> page(int page, int size) {
        int offset = (page - 1) * size;
        List<Experience> list = experienceMapper.selectPage(offset, size);
        long total = experienceMapper.countAll();
        List<ExperienceVO> records = list.stream().map(this::toVO).toList();
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public ExperienceVO create(ExperienceDTO dto, MultipartFile cover) {
        Experience experience = new Experience();
        BeanUtils.copyProperties(dto, experience);
        if (experience.getSortOrder() == null) {
            experience.setSortOrder(0);
        }
        experienceMapper.insert(experience);

        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            experience.setCoverUrl(vo.getUrl());
            experienceMapper.update(experience);
        }

        return toVO(experience);
    }

    @Override
    public ExperienceVO update(Long id, ExperienceDTO dto, MultipartFile cover) {
        Experience existing = experienceMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "经历不存在");
        }
        Experience experience = new Experience();
        BeanUtils.copyProperties(dto, experience);
        experience.setId(id);

        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            experience.setCoverUrl(vo.getUrl());
        }

        experienceMapper.update(experience);
        return toVO(experienceMapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        Experience existing = experienceMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "经历不存在");
        }
        experienceMapper.deleteById(id);
    }

    @Override
    public Experience getById(Long id) {
        Experience experience = experienceMapper.selectById(id);
        if (experience == null) {
            throw new BizException(ResultCode.NOT_FOUND, "经历不存在");
        }
        return experience;
    }

    @Override
    public PageResult<Experience> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Experience> list = experienceMapper.selectPage(offset, size);
        long total = experienceMapper.countAll();
        return new PageResult<>(list, page, size, total);
    }

    private ExperienceVO toVO(Experience e) {
        ExperienceVO vo = new ExperienceVO();
        vo.setId(e.getId());
        vo.setTitle(e.getTitle());
        vo.setOrganization(e.getOrganization());
        vo.setDescription(e.getDescription());
        vo.setStartDate(e.getStartDate());
        vo.setEndDate(e.getEndDate());
        vo.setCoverUrl(e.getCoverUrl());
        vo.setPageUrl(e.getPageUrl());
        return vo;
    }
}