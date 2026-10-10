package com.muxcraft.service;

import com.muxcraft.dto.PasswordUpdateDTO;
import com.muxcraft.dto.ProfileUpdateDTO;
import com.muxcraft.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService {

    UserVO updateProfile(Long id, ProfileUpdateDTO dto, MultipartFile avatar);

    void updatePassword(Long id, PasswordUpdateDTO dto);
}