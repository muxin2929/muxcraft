package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.ArticleDTO;
import com.muxcraft.entity.Article;
import com.muxcraft.mapper.ArticleMapper;
import com.muxcraft.service.ArticleService;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.ArticleDetailVO;
import com.muxcraft.vo.ArticleVO;
import com.muxcraft.vo.UploadVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 文章服务实现
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Set<String> VALID_STATUSES = Set.of("DRAFT", "PUBLISHED");

    private final ArticleMapper articleMapper;
    private final FileService fileService;

    public ArticleServiceImpl(ArticleMapper articleMapper, FileService fileService) {
        this.articleMapper = articleMapper;
        this.fileService = fileService;
    }

    @Override
    public PageResult<ArticleVO> page(int page, int size, String tag) {
        // 公共列表取消分页：一次返回全部已发布文章
        List<Article> list = articleMapper.selectPagePublished(0, 100000, tag);
        long total = articleMapper.countPublished(tag);
        List<ArticleVO> records = list.stream().map(this::toListVO).toList();
        return new PageResult<>(records, 1, records.size(), total);
    }

    @Override
    public ArticleDetailVO detail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || !"PUBLISHED".equals(article.getStatus())) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    @Override
    public ArticleVO create(ArticleDTO dto, MultipartFile cover) {
        validateStatus(dto.getStatus());
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        if (article.getStatus() == null) {
            article.setStatus("DRAFT");
        } else {
            article.setStatus(article.getStatus().toUpperCase());
        }
        articleMapper.insert(article);

        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            article.setCoverUrl(vo.getUrl());
            articleMapper.update(article);
        }

        return toListVO(article);
    }

    @Override
    public ArticleVO update(Long id, ArticleDTO dto, MultipartFile cover) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        validateStatus(dto.getStatus());
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setId(id);

        String oldCoverUrl = existing.getCoverUrl();
        if (cover != null && !cover.isEmpty()) {
            UploadVO vo = fileService.upload(cover, "covers");
            article.setCoverUrl(vo.getUrl());
        }

        if (article.getStatus() != null) {
            article.setStatus(article.getStatus().toUpperCase());
        }
        articleMapper.update(article);
        if (cover != null && !cover.isEmpty() && oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
            fileService.delete(oldCoverUrl);
        }
        return toListVO(articleMapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        String coverUrl = existing.getCoverUrl();
        articleMapper.deleteById(id);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            fileService.delete(coverUrl);
        }
    }

    @Override
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        return article;
    }

    @Override
    public PageResult<Article> adminPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Article> list = articleMapper.selectAdminPage(offset, size);
        long total = articleMapper.countAll();
        return new PageResult<>(list, page, size, total);
    }

    private ArticleVO toListVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSlug(article.getSlug());
        vo.setSummary(article.getSummary());
        vo.setCoverUrl(article.getCoverUrl());
        vo.setPublishedAt(article.getPublishedAt());
        return vo;
    }

    private void validateStatus(String status) {
        if (status != null && !status.isEmpty() && !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new BizException(ResultCode.BAD_REQUEST, "文章状态只能是 draft 或 published");
        }
    }
}