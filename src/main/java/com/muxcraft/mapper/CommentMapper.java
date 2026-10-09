package com.muxcraft.mapper;

import com.muxcraft.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目评论 Mapper
 */
public interface CommentMapper {

    int insert(Comment comment);

    List<Comment> selectPageByProjectId(@Param("projectId") Long projectId,
                                        @Param("offset") int offset, @Param("size") int size);

    long countByProjectId(@Param("projectId") Long projectId);

    int deleteById(@Param("id") Long id);

    Comment selectById(@Param("id") Long id);

    List<Comment> selectAllAdmin();

    List<Comment> selectAdminPage(@Param("offset") int offset, @Param("size") int size);

    long countAll();
}