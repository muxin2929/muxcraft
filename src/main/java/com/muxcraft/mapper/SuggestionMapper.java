package com.muxcraft.mapper;

import com.muxcraft.entity.Suggestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户建议 Mapper
 */
public interface SuggestionMapper {

    int insert(Suggestion suggestion);

    List<Suggestion> selectPage(@Param("offset") int offset, @Param("size") int size);

    long countAll();

    int deleteById(@Param("id") Long id);

    Suggestion selectById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}