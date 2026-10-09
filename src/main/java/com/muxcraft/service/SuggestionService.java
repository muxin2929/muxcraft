package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.SuggestionCreateDTO;
import com.muxcraft.entity.Suggestion;
import com.muxcraft.vo.SuggestionAdminVO;

/**
 * 建议服务
 */
public interface SuggestionService {

    void create(Long userId, SuggestionCreateDTO dto);

    PageResult<SuggestionAdminVO> adminPage(int page, int size);

    void delete(Long id);

    PageResult<Suggestion> adminPageEntity(int page, int size);
}