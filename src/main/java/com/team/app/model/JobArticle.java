package com.team.app.model;

import java.sql.Timestamp;
import java.util.Objects;

public class JobArticle {
    private Long id;
    private Long jobId;
    private String title;
    private String description;
    private String url;
    private String sentiment; 
    private Timestamp createdAt;

    public JobArticle() {}

    public JobArticle(Long jobId, String title, String description, String url, String sentiment) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.sentiment = sentiment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobArticle)) return false;
        JobArticle that = (JobArticle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "JobArticle{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", title='" + title + '\'' +
                ", sentiment='" + sentiment + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}