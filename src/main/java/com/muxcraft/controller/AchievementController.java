package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.service.AchievementService;
import com.muxcraft.vo.AchievementVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成就控制器
 * 接口：GET /achievements
 */
@RestController
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public Result<List<AchievementVO>> list() {
        return Result.success(achievementService.list());
    }
}