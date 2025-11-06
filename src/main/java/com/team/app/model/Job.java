package com.team.app.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job - Entity model representing a sentiment analysis job
 * 
 * TODO: Add getters and setters
 * TODO: Add equals() and hashCode() methods
 * TODO: Add toString() method
 * TODO: Add relationship with JobArticle (one-to-many)
 */
public class Job {
    private int id;
    private int userId;
    private String name;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<JobArticle> articles; // One-to-many relationship
    
    // TODO: Generate constructors
    // TODO: Generate getters and setters
    
    public Job() {
        // Default constructor
    }
    
    public Job(int userId, String name) {
        this.userId = userId;
        this.name = name;
        this.status = "PENDING";
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<JobArticle> getArticles() {
        return articles;
    }
    
    public void setArticles(List<JobArticle> articles) {
        this.articles = articles;
    }
}

