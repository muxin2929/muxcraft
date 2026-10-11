package com.muxcraft.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 客户端工厂
 * 懒加载单例，未配置 OSS 时返回 null
 */
@Component
public class OssClientFactory {

    private static final Logger log = LoggerFactory.getLogger(OssClientFactory.class);

    private final OssProperties ossProperties;
    private final FileProperties fileProperties;
    private volatile OSS ossClient;

    public OssClientFactory(OssProperties ossProperties, FileProperties fileProperties) {
        this.ossProperties = ossProperties;
        this.fileProperties = fileProperties;
    }

    public OSS getClient() {
        if (!ossProperties.isConfigured()) {
            return null;
        }
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    try {
                        ossClient = new OSSClientBuilder().build(
                                ossProperties.getEndpoint(),
                                ossProperties.getAccessKeyId(),
                                ossProperties.getAccessKeySecret()
                        );
                        log.info("OSS 客户端初始化成功, endpoint={}, bucket={}",
                                ossProperties.getEndpoint(), ossProperties.getBucketName());
                    } catch (Exception e) {
                        log.error("OSS 客户端初始化失败", e);
                        return null;
                    }
                }
            }
        }
        return ossClient;
    }

    public String getBucketName() {
        return ossProperties.getBucketName();
    }

    public String getEndpoint() {
        return ossProperties.getEndpoint();
    }

    public String getOssDomain() {
        return fileProperties.getOssDomain();
    }
}