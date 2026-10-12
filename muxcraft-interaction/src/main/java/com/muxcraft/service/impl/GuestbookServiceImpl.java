package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.entity.Guestbook;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.ChatBlockMapper;
import com.muxcraft.mapper.GuestbookMapper;
import com.muxcraft.mapper.SiteSettingsMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.GuestbookService;
import com.muxcraft.vo.GuestbookAdminVO;
import com.muxcraft.vo.GuestbookVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言板服务实现
 */
@Service
public class GuestbookServiceImpl implements GuestbookService {

    private final GuestbookMapper guestbookMapper;
    private final UserMapper userMapper;
    private final SiteSettingsMapper siteSettingsMapper;
    private final ChatBlockMapper chatBlockMapper;

    public GuestbookServiceImpl(GuestbookMapper guestbookMapper, UserMapper userMapper,
                                SiteSettingsMapper siteSettingsMapper, ChatBlockMapper chatBlockMapper) {
        this.guestbookMapper = guestbookMapper;
        this.userMapper = userMapper;
        this.siteSettingsMapper = siteSettingsMapper;
        this.chatBlockMapper = chatBlockMapper;
    }

    @Override
    public PageResult<GuestbookVO> page(int page, int size, Long currentUserId) {
        int offset = (page - 1) * size;
        List<Guestbook> list = guestbookMapper.selectPage(offset, size, currentUserId);
        long total = guestbookMapper.countAll(currentUserId);
        List<GuestbookVO> records = new ArrayList<>();
        for (Guestbook g : list) {
            records.add(toVO(g));
        }
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public GuestbookVO create(Long userId, String content, Long replyToId) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        if (settings != null && settings.getGuestbookEnabled() != null && settings.getGuestbookEnabled() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "留言板已关闭");
        }
        if (replyToId != null) {
            Guestbook parent = guestbookMapper.selectById(replyToId);
            if (parent == null) {
                throw new BizException(ResultCode.NOT_FOUND, "被回复的留言不存在");
            }
            if (!parent.getUserId().equals(userId) && chatBlockMapper.existsAnyDirection(userId, parent.getUserId())) {
                throw new BizException(ResultCode.FORBIDDEN, "对方已屏蔽你，无法回复");
            }
        }
        Guestbook guestbook = new Guestbook();
        guestbook.setUserId(userId);
        guestbook.setContent(content);
        guestbook.setReplyToId(replyToId);
        guestbookMapper.insert(guestbook);
        Guestbook saved = guestbookMapper.selectById(guestbook.getId());
        return toVO(saved);
    }

    @Override
    public void delete(Long id) {
        Guestbook existing = guestbookMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "留言不存在");
        }
        guestbookMapper.deleteById(id);
    }

    @Override
    public void deleteByUser(Long id, Long userId) {
        Guestbook existing = guestbookMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "留言不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "只能删除自己的留言");
        }
        guestbookMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        guestbookMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<GuestbookAdminVO> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Guestbook> list = guestbookMapper.selectAdminPage(offset, size);
        long total = guestbookMapper.countAll(null);
        List<GuestbookAdminVO> records = new ArrayList<>();
        for (Guestbook g : list) {
            records.add(toAdminVO(g));
        }
        return new PageResult<>(records, page, size, total);
    }

    private GuestbookVO toVO(Guestbook g) {
        GuestbookVO vo = new GuestbookVO();
        vo.setId(g.getId());
        vo.setUserId(g.getUserId());
        vo.setContent(g.getContent());
        vo.setCreatedAt(g.getCreatedAt());
        vo.setReplyToId(g.getReplyToId());
        vo.setBlockedByMe(g.getBlockedByMe());
        User user = userMapper.selectById(g.getUserId());
        if (user != null) {
            vo.setAccount(user.getUserId());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setRole(user.getRole());
        }
        if (g.getReplyToId() != null) {
            Guestbook parent = guestbookMapper.selectById(g.getReplyToId());
            if (parent != null) {
                User parentUser = userMapper.selectById(parent.getUserId());
                if (parentUser != null) {
                    vo.setReplyToNickname(parentUser.getNickname());
                }
            }
        }
        return vo;
    }

    private GuestbookAdminVO toAdminVO(Guestbook g) {
        GuestbookAdminVO vo = new GuestbookAdminVO();
        vo.setId(g.getId());
        vo.setUserId(g.getUserId());
        vo.setContent(g.getContent());
        vo.setCreatedAt(g.getCreatedAt());
        User user = userMapper.selectById(g.getUserId());
        if (user != null) {
            vo.setAccount(user.getUserId());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setRole(user.getRole());
        }
        return vo;
    }
}