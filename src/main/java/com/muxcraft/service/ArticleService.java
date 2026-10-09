package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.ArticleDTO;
import com.muxcraft.entity.Article;
import com.muxcraft.vo.ArticleDetailVO;
import com.muxcraft.vo.ArticleVO;

/**
 * 文章服务
 */
public interface ArticleService {

    PageResult<ArticleVO> page(int page, int size, String tag);

    ArticleDetailVO detail(String slug);

    ArticleVO create(ArticleDTO dto);

    ArticleVO update(Long id, ArticleDTO dto);

    void delete(Long id);

    Article getById(Long id);

    PageResult<Article> adminPage(int page, int size);
}