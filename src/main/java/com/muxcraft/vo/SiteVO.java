package com.muxcraft.vo;

/**
 * 首页站点信息
 */
public class SiteVO {

    private String name;
    private String title;
    private String description;
    private String siteTitle;
    private String siteDescription;
    private Boolean registrationEnabled;
    private Boolean guestbookEnabled;
    private Boolean commentEnabled;
    private SiteStatsVO stats;

    public SiteVO() {
    }

    public SiteVO(String name, String title, String description, SiteStatsVO stats) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSiteTitle() {
        return siteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public void setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
    }

    public Boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public Boolean getGuestbookEnabled() {
        return guestbookEnabled;
    }

    public void setGuestbookEnabled(Boolean guestbookEnabled) {
        this.guestbookEnabled = guestbookEnabled;
    }

    public Boolean getCommentEnabled() {
        return commentEnabled;
    }

    public void setCommentEnabled(Boolean commentEnabled) {
        this.commentEnabled = commentEnabled;
    }

    public SiteStatsVO getStats() {
        return stats;
    }

    public void setStats(SiteStatsVO stats) {
        this.stats = stats;
    }
}