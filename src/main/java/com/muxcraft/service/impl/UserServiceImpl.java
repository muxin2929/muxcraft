package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.PasswordUpdateDTO;
import com.muxcraft.dto.ProfileUpdateDTO;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.UserService;
import com.muxcraft.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserVO updateProfile(Long id, ProfileUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        userMapper.updateProfile(id, dto.getNickname(), dto.getAvatarUrl());
        User updated = userMapper.selectById(id);
        return new UserVO(updated.getId(), updated.getUserId(), updated.getNickname(),
                updated.getAvatarUrl(), updated.getRole());
    }

    @Override
    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNPROCESSABLE, "旧密码错误");
        }
        userMapper.updatePassword(id, passwordEncoder.encode(dto.getNewPassword()));
    }
}