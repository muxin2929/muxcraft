package com.muxcraft.service;

import com.muxcraft.dto.LoginDTO;
import com.muxcraft.vo.LoginVO;
import com.muxcraft.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 认证服务
 */
public interface AuthService {

    LoginVO register(String userId, String nickname, String password, MultipartFile avatar);

    LoginVO login(LoginDTO dto);

    UserVO me(Long userId);

    LoginVO refresh(String refreshToken);

    void logout();
}