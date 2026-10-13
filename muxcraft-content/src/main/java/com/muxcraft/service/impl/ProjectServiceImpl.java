package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.ProjectDTO;
import com.muxcraft.entity.Project;
import com.muxcraft.entity.ProjectSkill;
import com.muxcraft.entity.Skill;
import com.muxcraft.mapper.CommentMapper;
import com.muxcraft.mapper.ProjectMapper;
import com.muxcraft.mapper.ProjectSkillMapper;
import com.muxcraft.mapper.SkillMapper;
import com.muxcraft.service.FileService;
import com.muxcraft.service.ProjectService;
import com.muxcraft.vo.ProjectDetailVO;
import com.muxcraft.vo.ProjectVO;
import com.muxcraft.vo.SkillSimpleVO;
import com.muxcraft.vo.UploadVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目服务实现
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectSkillMapper projectSkillMapper;
    private final SkillMapper skillMapper;
    private final CommentMapper commentMapper;
    private final FileService fileService;

    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectSkillMapper projectSkillMapper,
                              SkillMapper skillMapper, CommentMapper commentMapper, FileService fileService) {
        this.projectMapper = projectMapper;
        this.projectSkillMapper = projectSkillMapper;
        this.skillMapper = skillMapper;
        this.commentMapper = commentMapper;
        this.fileService = fileService;
    }

    @Override
    public PageResult<ProjectVO> page(int page, int size, String keyword, Long skillId) {
        int offset = (page - 1) * size;
        List<Project> list = projectMapper.selectPage(offset, size, keyword, skillId);
        long total = projectMapper.countPage(keyword, skillId);
        List<ProjectVO> records = list.stream().map(this::toListVO).toList();
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public ProjectDetailVO detail(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ResultCode.NOT_FOUND, "项目不存在");
        }
        ProjectDetailVO vo = new ProjectDetailVO();
        BeanUtils.copyProperties(project, vo);
        vo.setSkills(getSkillSimples(id));
        return vo;
    }

    @Override
    @Transactional
    public ProjectVO create(ProjectDTO dto, MultipartFile icon, MultipartFile cover) {
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        if (project.getStatus() == null) {
            project.setStatus(1);
        }
        if (project.getSortOrder() == null) {
            project.setSortOrder(0);
        }
        projectMapper.insert(project);

        boolean hasFile = false;
        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            project.setCoverUrl(vo.getUrl());
            hasFile = true;
        }
        if (hasFile) {
            projectMapper.update(project);
        }

        saveProjectSkills(project.getId(), dto.getSkillIds());
        return toListVO(project);
    }

    @Override
    @Transactional
    public ProjectVO update(Long id, ProjectDTO dto, MultipartFile icon, MultipartFile cover) {
        Project existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "项目不存在");
        }
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setId(id);

        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            project.setCoverUrl(vo.getUrl());
        }

        projectMapper.update(project);
        if (dto.getSkillIds() != null) {
            projectSkillMapper.deleteByProjectId(id);
            saveProjectSkills(id, dto.getSkillIds());
        }
        return toListVO(projectMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "项目不存在");
        }
        commentMapper.deleteByProjectId(id);
        projectSkillMapper.deleteByProjectId(id);
        projectMapper.deleteById(id);
    }

    @Override
    public Project getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(ResultCode.NOT_FOUND, "项目不存在");
        }
        project.setSkillIds(projectSkillMapper.selectSkillIdsByProjectId(id));
        return project;
    }

    @Override
    public PageResult<Project> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Project> list = projectMapper.selectAdminPage(offset, size);
        long total = projectMapper.countAll();
        return new PageResult<>(list, page, size, total);
    }

    private void saveProjectSkills(Long projectId, List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return;
        }
        List<ProjectSkill> list = new ArrayList<>();
        for (Long skillId : skillIds) {
            ProjectSkill ps = new ProjectSkill();
            ps.setProjectId(projectId);
            ps.setSkillId(skillId);
            list.add(ps);
        }
        projectSkillMapper.insertBatch(list);
    }

    private List<SkillSimpleVO> getSkillSimples(Long projectId) {
        List<Skill> skills = skillMapper.selectByProjectId(projectId);
        return skills.stream()
                .map(s -> new SkillSimpleVO(s.getId(), s.getName(), s.getIconUrl()))
                .toList();
    }

    private ProjectVO toListVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setSlug(project.getSlug());
        vo.setCoverUrl(project.getCoverUrl());
        vo.setSummary(project.getSummary());
        vo.setSkills(getSkillSimples(project.getId()));
        return vo;
    }
}