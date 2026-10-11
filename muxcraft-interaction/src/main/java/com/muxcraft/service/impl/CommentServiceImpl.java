package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.entity.Comment;
import com.muxcraft.entity.Project;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.ChatBlockMapper;
import com.muxcraft.mapper.CommentMapper;
import com.muxcraft.mapper.ProjectMapper;
import com.muxcraft.mapper.SiteSettingsMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.CommentService;
import com.muxcraft.vo.CommentAdminVO;
import com.muxcraft.vo.CommentVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论服务实现
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final SiteSettingsMapper siteSettingsMapper;
    private final ChatBlockMapper chatBlockMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper, ProjectMapper projectMapper,
                              SiteSettingsMapper siteSettingsMapper, ChatBlockMapper chatBlockMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.siteSettingsMapper = siteSettingsMapper;
        this.chatBlockMapper = chatBlockMapper;
    }

    @Override
    public PageResult<CommentVO> page(Long projectId, int page, int size, Long currentUserId) {
        int offset = (page - 1) * size;
        List<Comment> list = commentMapper.selectPageByProjectId(projectId, offset, size, currentUserId);
        long total = commentMapper.countByProjectId(projectId, currentUserId);
        List<CommentVO> records = new ArrayList<>();
        for (Comment comment : list) {
            records.add(toVO(comment));
        }
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public CommentVO create(Long projectId, Long userId, String content, Long replyToId) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        if (settings != null && settings.getCommentEnabled() != null && settings.getCommentEnabled() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "评论已关闭");
        }
        if (projectMapper.selectById(projectId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "项目不存在");
        }
        if (replyToId != null) {
            Comment parent = commentMapper.selectById(replyToId);
            if (parent == null) {
                throw new BizException(ResultCode.NOT_FOUND, "被回复的评论不存在");
            }
            if (!parent.getUserId().equals(userId) && chatBlockMapper.existsAnyDirection(userId, parent.getUserId())) {
                throw new BizException(ResultCode.FORBIDDEN, "对方已屏蔽你，无法回复");
            }
        }
        Comment comment = new Comment();
        comment.setProjectId(projectId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setReplyToId(replyToId);
        commentMapper.insert(comment);
        Comment saved = commentMapper.selectById(comment.getId());
        return toVO(saved);
    }

    @Override
    public void delete(Long id) {
        Comment existing = commentMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评论不存在");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public void deleteByUser(Long id, Long userId) {
        Comment existing = commentMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评论不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        commentMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<CommentAdminVO> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> list = commentMapper.selectAdminPage(offset, size);
        long total = commentMapper.countAll();
        List<CommentAdminVO> records = new ArrayList<>();
        for (Comment comment : list) {
            records.add(toAdminVO(comment));
        }
        return new PageResult<>(records, page, size, total);
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyToId(comment.getReplyToId());
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setAccount(user.getUserId());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setRole(user.getRole());
        }
        if (comment.getReplyToId() != null) {
            Comment parent = commentMapper.selectById(comment.getReplyToId());
            if (parent != null) {
                User parentUser = userMapper.selectById(parent.getUserId());
                if (parentUser != null) {
                    vo.setReplyToNickname(parentUser.getNickname());
                }
            }
        }
        return vo;
    }

    private CommentAdminVO toAdminVO(Comment comment) {
        CommentAdminVO vo = new CommentAdminVO();
        vo.setId(comment.getId());
        vo.setProjectId(comment.getProjectId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        if (comment.getProjectId() != null) {
            Project project = projectMapper.selectById(comment.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setAccount(user.getUserId());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setRole(user.getRole());
        }
        return vo;
    }
}