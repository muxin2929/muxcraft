package com.muxcraft.vo;

/**
 * 站点统计信息
 */
public class SiteStatsVO {

    private long projects;
    private long experiences;
    private long skills;
    private long articles;

    public SiteStatsVO() {
    }

    public SiteStatsVO(long projects, long experiences, long skills, long articles) {
        this.projects = projects;
        this.experiences = experiences;
        this.skills = skills;
        this.articles = articles;
    }

    public long getProjects() {
        return projects;
    }

    public void setProjects(long projects) {
        this.projects = projects;
    }

    public long getExperiences() {
        return experiences;
    }

    public void setExperiences(long experiences) {
        this.experiences = experiences;
    }

    public long getSkills() {
        return skills;
    }

    public void setSkills(long skills) {
        this.skills = skills;
    }

    public long getArticles() {
        return articles;
    }

    public void setArticles(long articles) {
        this.articles = articles;
    }
}