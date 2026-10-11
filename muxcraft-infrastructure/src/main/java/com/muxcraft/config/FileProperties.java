package com.muxcraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 上传方式：oss / local
     */
    private String uploadType = "oss";

    /**
     * 本地存储目录（绝对路径）
     */
    private String localPath = "/www/wwwroot/muxcraft/uploads";

    /**
     * 本地存储访问 URL 前缀（含协议，不含末尾斜杠，如 https://muxcraft.cfd）
     */
    private String localUrlBase = "https://muxcraft.cfd";

    /**
     * OSS 访问域名（含协议，如 https://oss.example.com）
     */
    private String ossDomain;

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalUrlBase() {
        return localUrlBase;
    }

    public void setLocalUrlBase(String localUrlBase) {
        this.localUrlBase = localUrlBase;
    }

    public String getOssDomain() {
        return ossDomain;
    }

    public void setOssDomain(String ossDomain) {
        this.ossDomain = ossDomain;
    }
}