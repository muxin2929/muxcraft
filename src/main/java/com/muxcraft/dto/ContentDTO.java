package com.muxcraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 评论/留言/建议等内容提交的通用请求
 */
public class ContentDTO {

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容不超过1000字")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}