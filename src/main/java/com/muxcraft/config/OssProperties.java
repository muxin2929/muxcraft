package com.muxcraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 */
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
    private String bucketName;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isConfigured() {
        return accessKeyId != null && !accessKeyId.isEmpty()
                && accessKeySecret != null && !accessKeySecret.isEmpty()
                && bucketName != null && !bucketName.isEmpty();
    }
}