package com.muxcraft.service;

import com.muxcraft.common.PageResult;
import com.muxcraft.dto.SiteSettingsDTO;
import com.muxcraft.dto.UserStatusDTO;
import com.muxcraft.vo.DashboardVO;
import com.muxcraft.vo.SiteSettingsVO;
import com.muxcraft.vo.UserAdminVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 管理员服务
 */
public interface AdminService {

    DashboardVO dashboard();

    PageResult<UserAdminVO> userPage(int page, int size, String keyword, LocalDateTime startTime, LocalDateTime endTime);

    void updateUserStatus(Long id, UserStatusDTO dto);

    void deleteUser(Long id);

    SiteSettingsVO getSettings();

    SiteSettingsVO updateSettings(SiteSettingsDTO dto, MultipartFile photo);
}