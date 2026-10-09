package com.muxcraft.vo;

/**
 * 技术栈简版出参（用于项目展示）
 */
public class SkillSimpleVO {

    private Long id;
    private String name;
    private String iconUrl;

    public SkillSimpleVO() {
    }

    public SkillSimpleVO(Long id, String name, String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}