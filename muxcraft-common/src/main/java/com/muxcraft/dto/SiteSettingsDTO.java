package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 站点设置请求
 */
public class SiteSettingsDTO {

    @JsonAlias("site_title")
    private String siteTitle;

    @JsonAlias("site_description")
    private String siteDescription;

    @JsonAlias("profile_name")
    private String profileName;

    @JsonAlias("profile_title")
    private String profileTitle;

    @JsonAlias("short_intro")
    private String shortIntro;

    @JsonAlias("full_intro")
    private String fullIntro;

    private String location;

    @JsonAlias("photo_url")
    private String photoUrl;

    @JsonAlias("github_url")
    private String githubUrl;

    @JsonAlias("website_url")
    private String websiteUrl;

    @JsonAlias("registration_enabled")
    private Boolean registrationEnabled;

    @JsonAlias("guestbook_enabled")
    private Boolean guestbookEnabled;

    @JsonAlias("comment_enabled")
    private Boolean commentEnabled;

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

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
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
}