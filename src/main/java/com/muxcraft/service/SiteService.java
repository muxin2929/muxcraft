package com.muxcraft.service;

import com.muxcraft.dto.AdminProfileDTO;
import com.muxcraft.vo.ProfileVO;
import com.muxcraft.vo.SiteVO;

/**
 * 站点服务
 */
public interface SiteService {

    SiteVO getSite();

    ProfileVO getProfile();

    ProfileVO updateAdminProfile(AdminProfileDTO dto);
}