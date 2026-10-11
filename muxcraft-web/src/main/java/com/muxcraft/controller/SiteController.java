package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.service.SiteService;
import com.muxcraft.vo.ProfileVO;
import com.muxcraft.vo.SiteVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点控制器
 * 接口：GET /site、GET /profile
 */
@RestController
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/site")
    public Result<SiteVO> site() {
        return Result.success(siteService.getSite());
    }

    @GetMapping("/profile")
    public Result<ProfileVO> profile() {
        return Result.success(siteService.getProfile());
    }
}