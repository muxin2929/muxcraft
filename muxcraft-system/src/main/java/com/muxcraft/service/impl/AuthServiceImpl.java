package com.muxcraft.service.impl;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.dto.LoginDTO;
import com.muxcraft.entity.SiteSettings;
import com.muxcraft.entity.User;
import com.muxcraft.mapper.SiteSettingsMapper;
import com.muxcraft.mapper.UserMapper;
import com.muxcraft.security.JwtUtil;
import com.muxcraft.service.AuthService;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.LoginVO;
import com.muxcraft.vo.UploadVO;
import com.muxcraft.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final SiteSettingsMapper siteSettingsMapper;
    private final JwtUtil jwtUtil;
    private final FileService fileService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper, SiteSettingsMapper siteSettingsMapper,
                           JwtUtil jwtUtil, FileService fileService) {
        this.userMapper = userMapper;
        this.siteSettingsMapper = siteSettingsMapper;
        this.jwtUtil = jwtUtil;
        this.fileService = fileService;
    }

    @Override
    public LoginVO register(String userId, String nickname, String password, MultipartFile avatar) {
        SiteSettings settings = siteSettingsMapper.selectById(1);
        if (settings != null && settings.getRegistrationEnabled() != null && settings.getRegistrationEnabled() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "站点已关闭注册");
        }
        if (userId == null || userId.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号不能为空");
        }
        if (userId.length() < 3 || userId.length() > 32) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号长度3-32位");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "昵称不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "密码不能为空");
        }
        if (password.length() < 8) {
            throw new BizException(ResultCode.BAD_REQUEST, "密码至少8位");
        }
        User existing = userMapper.selectByUserId(userId);
        if (existing != null) {
            throw new BizException(ResultCode.CONFLICT, "账号已存在");
        }
        User user = new User();
        user.setUserId(userId);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        if (avatar != null && !avatar.isEmpty()) {
            UploadVO vo = fileService.uploadAvatar(user.getId(), avatar);
            userMapper.updateProfile(user.getId(), null, vo.getUrl());
            user.setAvatarUrl(vo.getUrl());
        }

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByUserId(dto.getUserId());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已禁用");
        }
        fillAdminAvatar(user);
        return buildLoginVO(user);
    }

    @Override
    public UserVO me(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        fillAdminAvatar(user);
        return toUserVO(user);
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }
        Long userId;
        try {
            userId = jwtUtil.parseRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }
        fillAdminAvatar(user);
        return buildLoginVO(user);
    }

    @Override
    public void logout() {
    }

    private LoginVO buildLoginVO(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserId(), user.getNickname(),
                user.getAvatarUrl(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        return new LoginVO(accessToken, jwtUtil.getExpireSeconds(), toUserVO(user), refreshToken);
    }

    private void fillAdminAvatar(User user) {
        if ("ADMIN".equals(user.getRole()) && (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty())) {
            SiteSettings settings = siteSettingsMapper.selectById(1);
            if (settings != null && settings.getPhotoUrl() != null && !settings.getPhotoUrl().isEmpty()) {
                user.setAvatarUrl(settings.getPhotoUrl());
            }
        }
    }

    private UserVO toUserVO(User user) {
        return new UserVO(user.getId(), user.getUserId(), user.getNickname(), user.getAvatarUrl(), user.getRole());
    }
}