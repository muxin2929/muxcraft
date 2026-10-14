package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.PasswordUpdateDTO;
import com.muxcraft.dto.ProfileUpdateDTO;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.service.FileService;
import com.muxcraft.service.UserService;
import com.muxcraft.vo.UploadVO;
import com.muxcraft.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FileService fileService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, FileService fileService) {
        this.userMapper = userMapper;
        this.fileService = fileService;
    }

    @Override
    public UserVO updateProfile(Long id, ProfileUpdateDTO dto, MultipartFile avatar) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }

        String oldAvatarUrl = user.getAvatarUrl();
        String avatarUrl = dto.getAvatarUrl();
        boolean replaced = false;
        if (avatar != null && !avatar.isEmpty()) {
            UploadVO vo = fileService.uploadAvatar(id, avatar);
            avatarUrl = vo.getUrl();
            replaced = true;
        }

        userMapper.updateProfile(id, dto.getNickname(), avatarUrl);
        if (replaced && oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            fileService.delete(oldAvatarUrl);
        }
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
            throw new BizException(ResultCode.BAD_REQUEST, "原密码不正确");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 8) {
            throw new BizException(ResultCode.BAD_REQUEST, "新密码至少 8 位");
        }
        userMapper.updatePassword(id, passwordEncoder.encode(dto.getNewPassword()));
    }
}