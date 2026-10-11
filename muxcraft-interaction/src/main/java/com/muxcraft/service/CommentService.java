package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.vo.CommentAdminVO;
import com.muxcraft.vo.CommentVO;

import java.util.List;

/**
 * 评论服务
 */
public interface CommentService {

    PageResult<CommentVO> page(Long projectId, int page, int size, Long currentUserId);

    CommentVO create(Long projectId, Long userId, String content, Long replyToId);

    void delete(Long id);

    void deleteByUser(Long id, Long userId);

    void deleteByIds(List<Long> ids);

    PageResult<CommentAdminVO> adminPage(int page, int size);
}