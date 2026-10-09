package com.muxcraft.mapper;

import com.muxcraft.entity.SiteSettings;

/**
 * 站点设置 Mapper
 */
public interface SiteSettingsMapper {

    SiteSettings selectById(Integer id);

    int update(SiteSettings siteSettings);

    int insert(SiteSettings siteSettings);
}