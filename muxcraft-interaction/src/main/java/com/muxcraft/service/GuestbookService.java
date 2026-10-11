package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.vo.GuestbookAdminVO;
import com.muxcraft.vo.GuestbookVO;

import java.util.List;

/**
 * 留言板服务
 */
public interface GuestbookService {

    PageResult<GuestbookVO> page(int page, int size, Long currentUserId);

    GuestbookVO create(Long userId, String content, Long replyToId);

    void delete(Long id);

    void deleteByUser(Long id, Long userId);

    void deleteByIds(List<Long> ids);

    PageResult<GuestbookAdminVO> adminPage(int page, int size);
}