package com.muxcraft.vo;

/**
 * 管理员仪表盘统计
 */
public class DashboardVO {

    private long users;
    private long projects;
    private long articles;
    private long comments;
    private long guestbook;
    private long suggestions;

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getProjects() {
        return projects;
    }

    public void setProjects(long projects) {
        this.projects = projects;
    }

    public long getArticles() {
        return articles;
    }

    public void setArticles(long articles) {
        this.articles = articles;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public long getGuestbook() {
        return guestbook;
    }

    public void setGuestbook(long guestbook) {
        this.guestbook = guestbook;
    }

    public long getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(long suggestions) {
        this.suggestions = suggestions;
    }
}