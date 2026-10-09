package com.muxcraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 建议请求
 */
public class SuggestionCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不超过128字")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不超过2000字")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}