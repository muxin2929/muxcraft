package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.AchievementDTO;
import com.muxcraft.entity.Achievement;
import com.muxcraft.mapper.AchievementMapper;
import com.muxcraft.service.AchievementService;
import com.muxcraft.vo.AchievementVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成就服务实现
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;

    public AchievementServiceImpl(AchievementMapper achievementMapper) {
        this.achievementMapper = achievementMapper;
    }

    @Override
    public List<AchievementVO> list() {
        List<Achievement> list = achievementMapper.selectAll();
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public Achievement create(AchievementDTO dto) {
        Achievement achievement = new Achievement();
        BeanUtils.copyProperties(dto, achievement);
        if (achievement.getSortOrder() == null) {
            achievement.setSortOrder(0);
        }
        achievementMapper.insert(achievement);
        return achievement;
    }

    @Override
    public Achievement update(Long id, AchievementDTO dto) {
        Achievement existing = achievementMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "成就不存在");
        }
        Achievement achievement = new Achievement();
        BeanUtils.copyProperties(dto, achievement);
        achievement.setId(id);
        achievementMapper.update(achievement);
        return achievementMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        Achievement existing = achievementMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "成就不存在");
        }
        achievementMapper.deleteById(id);
    }

    @Override
    public Achievement getById(Long id) {
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement == null) {
            throw new BizException(ResultCode.NOT_FOUND, "成就不存在");
        }
        return achievement;
    }

    @Override
    public List<Achievement> adminList() {
        return achievementMapper.selectAll();
    }

    private AchievementVO toVO(Achievement a) {
        AchievementVO vo = new AchievementVO();
        vo.setId(a.getId());
        vo.setYear(a.getYear());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setSortOrder(a.getSortOrder());
        return vo;
    }
}