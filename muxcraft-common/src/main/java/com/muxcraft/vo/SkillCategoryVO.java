package com.muxcraft.vo;

import java.util.List;

/**
 * 按分类分组的技术栈出参
 */
public class SkillCategoryVO {

    private String category;
    private List<SkillVO> skills;

    public SkillCategoryVO() {
    }

    public SkillCategoryVO(String category, List<SkillVO> skills) {
        this.category = category;
        this.skills = skills;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<SkillVO> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillVO> skills) {
        this.skills = skills;
    }
}