package com.muxcraft.mapper;

import com.muxcraft.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper
 */
public interface UserMapper {

    int insert(User user);

    User selectById(@Param("id") Long id);

    User selectByUserId(@Param("userId") String userId);

    int updateProfile(@Param("id") Long id, @Param("nickname") String nickname, @Param("avatarUrl") String avatarUrl);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<User> selectPage(@Param("offset") int offset, @Param("size") int size);

    long countAll();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateAllAdminsAvatar(@Param("avatarUrl") String avatarUrl);

    int deleteById(@Param("id") Long id);

    long countAdmin();
}