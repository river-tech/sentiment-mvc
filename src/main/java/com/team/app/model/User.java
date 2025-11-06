package com.team.app.model;

import java.time.LocalDateTime;

/**
 * User - Entity model representing a user in the system
 * 
 * TODO: Add getters and setters
 * TODO: Add equals() and hashCode() methods
 * TODO: Add toString() method
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private LocalDateTime createdAt;
    
    // TODO: Generate constructors
    // TODO: Generate getters and setters
    
    public User() {
        // Default constructor
    }
    
    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

