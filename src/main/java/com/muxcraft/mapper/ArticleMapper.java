package com.muxcraft.mapper;

import com.muxcraft.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 博客文章 Mapper
 */
public interface ArticleMapper {

    int insert(Article article);

    Article selectById(@Param("id") Long id);

    Article selectBySlug(@Param("slug") String slug);

    List<Article> selectPagePublished(@Param("offset") int offset, @Param("size") int size,
                                      @Param("tag") String tag);

    long countPublished(@Param("tag") String tag);

    int update(Article article);

    int deleteById(@Param("id") Long id);

    long countAll();

    List<Article> selectAllAdmin();

    List<Article> selectAdminPage(@Param("offset") int offset, @Param("size") int size);
}