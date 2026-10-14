package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.SiteSettingsDTO;
import com.muxcraft.dto.UserStatusDTO;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.ArticleMapper;
import com.muxcraft.mapper.CommentMapper;
import com.muxcraft.mapper.GuestbookMapper;
import com.muxcraft.mapper.ProjectMapper;
import com.muxcraft.mapper.SiteSettingsMapper;
import com.muxcraft.mapper.SuggestionMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.service.AdminService;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.DashboardVO;
import com.muxcraft.vo.SiteSettingsVO;
import com.muxcraft.vo.UploadVO;
import com.muxcraft.vo.UserAdminVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务实现
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final GuestbookMapper guestbookMapper;
    private final SuggestionMapper suggestionMapper;
    private final SiteSettingsMapper siteSettingsMapper;
    private final FileService fileService;

    public AdminServiceImpl(UserMapper userMapper, ProjectMapper projectMapper, ArticleMapper articleMapper,
                            CommentMapper commentMapper, GuestbookMapper guestbookMapper,
                            SuggestionMapper suggestionMapper, SiteSettingsMapper siteSettingsMapper,
                            FileService fileService) {
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.guestbookMapper = guestbookMapper;
        this.suggestionMapper = suggestionMapper;
        this.siteSettingsMapper = siteSettingsMapper;
        this.fileService = fileService;
    }

    @Override
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setUsers(userMapper.countAll());
        vo.setProjects(projectMapper.countEnabled());
        vo.setArticles(articleMapper.countPublished(null));
        vo.setComments(commentMapper.countAll());
        vo.setGuestbook(guestbookMapper.countAll(null));
        vo.setSuggestions(suggestionMapper.countAll());
        return vo;
    }

    @Override
    public PageResult<UserAdminVO> userPage(int page, int size, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        int offset = (page - 1) * size;
        List<User> list = userMapper.selectPage(offset, size, keyword, startTime, endTime);
        long total = userMapper.countPage(keyword, startTime, endTime);
        List<UserAdminVO> records = list.stream().map(this::toAdminVO).toList();
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public void updateUserStatus(Long id, UserStatusDTO dto) {
        Long currentId = currentUserId();
        if (currentId != null && currentId.equals(id)) {
            throw new BizException(ResultCode.FORBIDDEN, "不能修改自己的状态");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        userMapper.updateStatus(id, dto.getStatus());
    }

    @Override
    public void deleteUser(Long id) {
        Long currentId = currentUserId();
        if (currentId != null && currentId.equals(id)) {
            throw new BizException(ResultCode.FORBIDDEN, "不能删除自己");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            long adminCount = userMapper.countAdmin();
            if (adminCount <= 1) {
                throw new BizException(ResultCode.FORBIDDEN, "禁止删除最后一个管理员");
            }
        }
        String avatarUrl = user.getAvatarUrl();
        userMapper.deleteById(id);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            fileService.delete(avatarUrl);
        }
    }

    @Override
    public SiteSettingsVO getSettings() {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        return toSettingsVO(settings);
    }

    @Override
    public SiteSettingsVO updateSettings(SiteSettingsDTO dto, MultipartFile photo) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        boolean isNew = (settings == null);
        if (isNew) {
            settings = new SiteSettings();
            settings.setId(1);
        }
        String oldPhotoUrl = settings.getPhotoUrl();
        settings.setSiteTitle(dto.getSiteTitle());
        settings.setSiteDescription(dto.getSiteDescription());
        settings.setProfileName(dto.getProfileName());
        settings.setProfileTitle(dto.getProfileTitle());
        settings.setShortIntro(dto.getShortIntro());
        settings.setFullIntro(dto.getFullIntro());
        settings.setLocation(dto.getLocation());
        settings.setPhotoUrl(dto.getPhotoUrl());
        settings.setGithubUrl(dto.getGithubUrl());
        settings.setWebsiteUrl(dto.getWebsiteUrl());
        settings.setRegistrationEnabled(dto.getRegistrationEnabled() != null && dto.getRegistrationEnabled() ? 1 : 0);
        settings.setGuestbookEnabled(dto.getGuestbookEnabled() != null && dto.getGuestbookEnabled() ? 1 : 0);
        settings.setCommentEnabled(dto.getCommentEnabled() != null && dto.getCommentEnabled() ? 1 : 0);
        if (isNew) {
            siteSettingsMapper.insert(settings);
        } else {
            siteSettingsMapper.update(settings);
        }

        if (photo != null && !photo.isEmpty()) {
            UploadVO vo = fileService.upload(photo, "avatars");
            settings.setPhotoUrl(vo.getUrl());
            siteSettingsMapper.update(settings);
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                fileService.delete(oldPhotoUrl);
            }
        }

        return toSettingsVO(siteSettingsMapper.selectById(1));
    }

    private Long currentUserId() {
        com.muxcraft.security.LoginUser lu = AuthInterceptor.currentUser();
        return lu != null ? lu.getId() : null;
    }

    private UserAdminVO toAdminVO(User user) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    private SiteSettingsVO toSettingsVO(SiteSettings s) {
        SiteSettingsVO vo = new SiteSettingsVO();
        if (s != null) {
            vo.setSiteTitle(s.getSiteTitle());
            vo.setSiteDescription(s.getSiteDescription());
            vo.setProfileName(s.getProfileName());
            vo.setProfileTitle(s.getProfileTitle());
            vo.setShortIntro(s.getShortIntro());
            vo.setFullIntro(s.getFullIntro());
            vo.setLocation(s.getLocation());
            vo.setPhotoUrl(s.getPhotoUrl());
            vo.setGithubUrl(s.getGithubUrl());
            vo.setWebsiteUrl(s.getWebsiteUrl());
            vo.setRegistrationEnabled(s.getRegistrationEnabled() != null && s.getRegistrationEnabled() == 1);
            vo.setGuestbookEnabled(s.getGuestbookEnabled() != null && s.getGuestbookEnabled() == 1);
            vo.setCommentEnabled(s.getCommentEnabled() != null && s.getCommentEnabled() == 1);
        }
        return vo;
    }
}