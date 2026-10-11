package com.muxcraft.service;

import com.muxcraft.dto.AchievementDTO;
import com.muxcraft.entity.Achievement;
import com.muxcraft.vo.AchievementVO;

import java.util.List;

/**
 * 成就服务
 */
public interface AchievementService {

    List<AchievementVO> list();

    Achievement create(AchievementDTO dto);

    Achievement update(Long id, AchievementDTO dto);

    void delete(Long id);

    Achievement getById(Long id);

    List<Achievement> adminList();
}