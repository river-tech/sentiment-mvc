package com.team.app.model;

import java.time.LocalDateTime;

/**
 * JobArticle - Entity model representing an article within a job
 * 
 * TODO: Add getters and setters
 * TODO: Add equals() and hashCode() methods
 * TODO: Add toString() method
 */
public class JobArticle {
    private int id;
    private int jobId;
    private String title;
    private String content;
    private String url;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private LocalDateTime createdAt;
    
    // TODO: Generate constructors
    // TODO: Generate getters and setters
    
    public JobArticle() {
        // Default constructor
    }
    
    public JobArticle(int jobId, String title, String content, String url) {
        this.jobId = jobId;
        this.title = title;
        this.content = content;
        this.url = url;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getJobId() {
        return jobId;
    }
    
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSentiment() {
        return sentiment;
    }
    
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

