package com.muxcraft.vo;

/**
 * 文件上传出参
 */
public class UploadVO {

    private String url;
    private String objectKey;

    public UploadVO() {
    }

    public UploadVO(String url, String objectKey) {
        this.url = url;
        this.objectKey = objectKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}