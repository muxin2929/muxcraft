package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.SuggestionCreateDTO;
import com.muxcraft.entity.Suggestion;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.SuggestionMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.SuggestionService;
import com.muxcraft.vo.SuggestionAdminVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 建议服务实现
 */
@Service
public class SuggestionServiceImpl implements SuggestionService {

    private final SuggestionMapper suggestionMapper;
    private final UserMapper userMapper;

    public SuggestionServiceImpl(SuggestionMapper suggestionMapper, UserMapper userMapper) {
        this.suggestionMapper = suggestionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void create(Long userId, SuggestionCreateDTO dto) {
        Suggestion suggestion = new Suggestion();
        suggestion.setUserId(userId);
        suggestion.setTitle(dto.getTitle());
        suggestion.setContent(dto.getContent());
        suggestionMapper.insert(suggestion);
    }

    @Override
    public PageResult<SuggestionAdminVO> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Suggestion> list = suggestionMapper.selectPage(offset, size);
        long total = suggestionMapper.countAll();
        List<SuggestionAdminVO> records = new ArrayList<>();
        for (Suggestion s : list) {
            SuggestionAdminVO vo = new SuggestionAdminVO();
            vo.setId(s.getId());
            vo.setUserId(s.getUserId());
            vo.setTitle(s.getTitle());
            vo.setContent(s.getContent());
            vo.setStatus(s.getStatus());
            vo.setCreatedAt(s.getCreatedAt());
            User user = userMapper.selectById(s.getUserId());
            if (user != null) {
                vo.setAccount(user.getUserId());
                vo.setNickname(user.getNickname());
            }
            records.add(vo);
        }
        return new PageResult<>(records, page, size, total);
    }

    @Override
    public void delete(Long id) {
        Suggestion existing = suggestionMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "建议不存在");
        }
        suggestionMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        suggestionMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<Suggestion> adminPageEntity(int page, int size) {
        int offset = (page - 1) * size;
        List<Suggestion> list = suggestionMapper.selectPage(offset, size);
        long total = suggestionMapper.countAll();
        return new PageResult<>(list, page, size, total);
    }
}