package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.service.ExperienceService;
import com.muxcraft.vo.ExperienceVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经历控制器
 * 接口：GET /experiences
 */
@RestController
@RequestMapping("/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping
    public Result<PageResult<ExperienceVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(experienceService.page(page, size));
    }
}