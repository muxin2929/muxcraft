package com.muxcraft.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

/**
 * 管理员个人资料请求
 */
public class AdminProfileDTO {

    @Size(max = 64)
    @JsonAlias("name")
    private String name;

    @Size(max = 128)
    @JsonAlias("title")
    private String title;

    @JsonAlias("short_intro")
    private String shortIntro;

    @JsonAlias("full_intro")
    private String fullIntro;

    @Size(max = 128)
    @JsonAlias("location")
    private String location;

    @JsonAlias("photo_url")
    private String photoUrl;

    @JsonAlias("github_url")
    private String githubUrl;

    @JsonAlias("website_url")
    private String websiteUrl;

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
}