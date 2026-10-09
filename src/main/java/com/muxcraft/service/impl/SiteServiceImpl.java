package com.muxcraft.service.impl;

import com.muxcraft.dto.AdminProfileDTO;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.mapper.ArticleMapper;
import com.muxcraft.mapper.ExperienceMapper;
import com.muxcraft.mapper.ProjectMapper;
import com.muxcraft.mapper.SiteSettingsMapper;
import com.muxcraft.mapper.SkillMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.SiteService;
import com.muxcraft.vo.ProfileVO;
import com.muxcraft.vo.SiteStatsVO;
import com.muxcraft.vo.SiteVO;
import org.springframework.stereotype.Service;

/**
 * 站点服务实现
 */
@Service
public class SiteServiceImpl implements SiteService {

    private final SiteSettingsMapper siteSettingsMapper;
    private final ProjectMapper projectMapper;
    private final ExperienceMapper experienceMapper;
    private final SkillMapper skillMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public SiteServiceImpl(SiteSettingsMapper siteSettingsMapper, ProjectMapper projectMapper,
                           ExperienceMapper experienceMapper, SkillMapper skillMapper,
                           ArticleMapper articleMapper, UserMapper userMapper) {
        this.siteSettingsMapper = siteSettingsMapper;
        this.projectMapper = projectMapper;
        this.experienceMapper = experienceMapper;
        this.skillMapper = skillMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public SiteVO getSite() {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        long projects = projectMapper.countEnabled();
        long experiences = experienceMapper.countAll();
        long skills = skillMapper.countEnabled();
        long articles = articleMapper.countPublished(null);
        SiteStatsVO stats = new SiteStatsVO(projects, experiences, skills, articles);
        SiteVO siteVO = new SiteVO();
        String siteTitle = (settings != null && settings.getSiteTitle() != null) ? settings.getSiteTitle() : "MuXCraft";
        String siteDesc = (settings != null && settings.getSiteDescription() != null) ? settings.getSiteDescription() : null;
        siteVO.setName(siteTitle);
        siteVO.setTitle(settings != null ? settings.getProfileTitle() : null);
        siteVO.setDescription(siteDesc);
        siteVO.setSiteTitle(siteTitle);
        siteVO.setSiteDescription(siteDesc);
        siteVO.setRegistrationEnabled(settings == null || settings.getRegistrationEnabled() == null || settings.getRegistrationEnabled() == 1);
        siteVO.setGuestbookEnabled(settings == null || settings.getGuestbookEnabled() == null || settings.getGuestbookEnabled() == 1);
        siteVO.setCommentEnabled(settings == null || settings.getCommentEnabled() == null || settings.getCommentEnabled() == 1);
        siteVO.setStats(stats);
        return siteVO;
    }

    @Override
    public ProfileVO getProfile() {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        ProfileVO vo = new ProfileVO();
        if (settings != null) {
            vo.setName(settings.getProfileName());
            vo.setTitle(settings.getProfileTitle());
            vo.setShortIntro(settings.getShortIntro());
            vo.setFullIntro(settings.getFullIntro());
            vo.setLocation(settings.getLocation());
            vo.setPhotoUrl(settings.getPhotoUrl());
            vo.setGithubUrl(settings.getGithubUrl());
            vo.setWebsiteUrl(settings.getWebsiteUrl());
        }
        return vo;
    }

    @Override
    public ProfileVO updateAdminProfile(AdminProfileDTO dto) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        boolean isNew = (settings == null);
        if (isNew) {
            settings = new SiteSettings();
            settings.setId(1);
        }
        settings.setProfileName(dto.getName());
        settings.setProfileTitle(dto.getTitle());
        settings.setShortIntro(dto.getShortIntro());
        settings.setFullIntro(dto.getFullIntro());
        settings.setLocation(dto.getLocation());
        settings.setPhotoUrl(dto.getPhotoUrl());
        settings.setGithubUrl(dto.getGithubUrl());
        settings.setWebsiteUrl(dto.getWebsiteUrl());
        if (isNew) {
            siteSettingsMapper.insert(settings);
        } else {
            siteSettingsMapper.update(settings);
        }
        if (dto.getPhotoUrl() != null && !dto.getPhotoUrl().isEmpty()) {
            userMapper.updateAllAdminsAvatar(dto.getPhotoUrl());
        }
        return getProfile();
    }
}