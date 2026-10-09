package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.service.ProjectService;
import com.muxcraft.vo.ProjectDetailVO;
import com.muxcraft.vo.ProjectVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目控制器
 * 接口：GET /projects、GET /projects/{id}
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public Result<PageResult<ProjectVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long skillId) {
        size = Math.min(size, 50);
        return Result.success(projectService.page(page, size, keyword, skillId));
    }

    @GetMapping("/{id}")
    public Result<ProjectDetailVO> detail(@PathVariable Long id) {
        return Result.success(projectService.detail(id));
    }
}