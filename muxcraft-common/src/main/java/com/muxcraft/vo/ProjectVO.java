package com.muxcraft.vo;

import java.util.List;

/**
 * 项目列表出参
 */
public class ProjectVO {

    private Long id;
    private String name;
    private String slug;
    private String coverUrl;
    private String summary;
    private List<SkillSimpleVO> skills;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<SkillSimpleVO> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillSimpleVO> skills) {
        this.skills = skills;
    }
}