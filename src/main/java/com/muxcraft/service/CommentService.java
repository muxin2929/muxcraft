package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.vo.CommentAdminVO;
import com.muxcraft.vo.CommentVO;

import java.util.List;

/**
 * 评论服务
 */
public interface CommentService {

    PageResult<CommentVO> page(Long projectId, int page, int size);

    CommentVO create(Long projectId, Long userId, String content);

    void delete(Long id);

    void deleteByIds(List<Long> ids);

    PageResult<CommentAdminVO> adminPage(int page, int size);
}