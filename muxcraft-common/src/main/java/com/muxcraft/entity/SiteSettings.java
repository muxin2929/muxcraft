package com.muxcraft.entity;

/**
 * 站点设置表（单行）
 */
public class SiteSettings {

    private Integer id;
    private String siteTitle;
    private String siteDescription;
    private String profileName;
    private String profileTitle;
    private String shortIntro;
    private String fullIntro;
    private String location;
    private String photoUrl;
    private String githubUrl;
    private String giteeUrl;
    private String websiteUrl;
    private Integer registrationEnabled;
    private Integer guestbookEnabled;
    private Integer commentEnabled;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getShortIntro() {
        return shortIntro;
    }

    public void setShortIntro(String shortIntro) {
        this.shortIntro = shortIntro;
    }

    public String getFullIntro() {
        return fullIntro;
    }

    public void setFullIntro(String fullIntro) {
        this.fullIntro = fullIntro;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getGiteeUrl() {
        return giteeUrl;
    }

    public void setGiteeUrl(String giteeUrl) {
        this.giteeUrl = giteeUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Integer getRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(Integer registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public Integer getGuestbookEnabled() {
        return guestbookEnabled;
    }

    public void setGuestbookEnabled(Integer guestbookEnabled) {
        this.guestbookEnabled = guestbookEnabled;
    }

    public Integer getCommentEnabled() {
        return commentEnabled;
    }

    public void setCommentEnabled(Integer commentEnabled) {
        this.commentEnabled = commentEnabled;
    }
}