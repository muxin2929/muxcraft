package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.service.SkillService;
import com.muxcraft.vo.SkillVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技术栈控制器
 * 接口：GET /skills
 */
@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public Result<List<SkillVO>> list() {
        return Result.success(skillService.list());
    }
}