package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 成就请求
 */
public class AchievementDTO {

    @NotBlank(message = "年份不能为空")
    @Size(max = 8)
    private String year;

    @NotBlank(message = "标题不能为空")
    @Size(max = 255)
    private String title;

    @Size(max = 500)
    private String description;

    @JsonAlias("sort_order")
    private Integer sortOrder;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}