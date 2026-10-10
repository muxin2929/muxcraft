package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.ArticleDTO;
import com.muxcraft.entity.Article;
import com.muxcraft.vo.ArticleDetailVO;
import com.muxcraft.vo.ArticleVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章服务
 */
public interface ArticleService {

    PageResult<ArticleVO> page(int page, int size, String tag);

    ArticleDetailVO detail(Long id);

    ArticleVO create(ArticleDTO dto, MultipartFile cover);

    ArticleVO update(Long id, ArticleDTO dto, MultipartFile cover);

    void delete(Long id);

    Article getById(Long id);

    PageResult<Article> adminPage(int page, int size);
}