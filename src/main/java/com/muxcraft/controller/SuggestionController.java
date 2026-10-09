package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.dto.SuggestionCreateDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 建议控制器
 * 接口：POST /suggestions
 */
@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody SuggestionCreateDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        suggestionService.create(loginUser.getId(), dto);
        return Result.success();
    }
}