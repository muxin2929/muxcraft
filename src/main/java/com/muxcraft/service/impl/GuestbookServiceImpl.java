package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.entity.Guestbook;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.entity.User;
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

    public GuestbookServiceImpl(GuestbookMapper guestbookMapper, UserMapper userMapper,
                                SiteSettingsMapper siteSettingsMapper) {
        this.guestbookMapper = guestbookMapper;
        this.userMapper = userMapper;
        this.siteSettingsMapper = siteSettingsMapper;
    }

    @Override
    public PageResult<GuestbookVO> page(int page, int size) {
        int offset = (page - 1) * size;
        List<Guestbook> list = guestbookMapper.selectPage(offset, size);
        long total = guestbookMapper.countAll();
        List<GuestbookVO> records = new ArrayList<>();
        for (Guestbook g : list) {
            records.add(toVO(g));
        }
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public GuestbookVO create(Long userId, String content) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        if (settings != null && settings.getGuestbookEnabled() != null && settings.getGuestbookEnabled() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "留言板已关闭");
        }
        Guestbook guestbook = new Guestbook();
        guestbook.setUserId(userId);
        guestbook.setContent(content);
        guestbookMapper.insert(guestbook);
        return toVO(guestbook);
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
        long total = guestbookMapper.countAll();
        List<GuestbookAdminVO> records = new ArrayList<>();
        for (Guestbook g : list) {
            records.add(toAdminVO(g));
        }
        return new PageResult<>(records, page, size, total);
    }

    private GuestbookVO toVO(Guestbook g) {
        GuestbookVO vo = new GuestbookVO();
        vo.setId(g.getId());
        vo.setContent(g.getContent());
        vo.setCreatedAt(g.getCreatedAt());
        User user = userMapper.selectById(g.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
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
        }
        return vo;
    }
}