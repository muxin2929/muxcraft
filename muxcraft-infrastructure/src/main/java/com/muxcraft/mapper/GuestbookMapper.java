package com.muxcraft.mapper;

import com.muxcraft.entity.Guestbook;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 留言板 Mapper
 */
public interface GuestbookMapper {

    int insert(Guestbook guestbook);

    List<Guestbook> selectPage(@Param("offset") int offset, @Param("size") int size,
                               @Param("currentUserId") Long currentUserId);

    long countAll(@Param("currentUserId") Long currentUserId);

    int deleteById(@Param("id") Long id);

    int deleteByIds(@Param("ids") List<Long> ids);

    Guestbook selectById(@Param("id") Long id);

    List<Guestbook> selectAllAdmin();

    List<Guestbook> selectAdminPage(@Param("offset") int offset, @Param("size") int size);
}