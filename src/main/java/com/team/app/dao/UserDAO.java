package com.team.app.dao;

import com.team.app.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO - Data Access Object for User entity
 * 
 * TODO: Implement database operations for User
 * TODO: Use HikariCP connection pool
 * TODO: Implement CRUD methods
 */
public class UserDAO {
    
    // TODO: Inject DataSource from HikariCP
    
    /**
     * Find user by username
     * TODO: Execute SELECT query
     * TODO: Map ResultSet to User object
     */
    public User findByUsername(String username) {
        // TODO: Implement SQL query
        // SELECT * FROM users WHERE username = ?
        return null;
    }
    
    /**
     * Find user by ID
     * TODO: Execute SELECT query
     * TODO: Map ResultSet to User object
     */
    public User findById(int userId) {
        // TODO: Implement SQL query
        // SELECT * FROM users WHERE id = ?
        return null;
    }
    
    /**
     * Create new user
     * TODO: Execute INSERT query
     * TODO: Return generated user ID
     */
    public int createUser(User user) {
        // TODO: Implement SQL query
        // INSERT INTO users (username, password_hash, email, created_at) VALUES (?, ?, ?, NOW())
        return -1;
    }
    
    /**
     * Check if username exists
     * TODO: Execute COUNT query
     */
    public boolean usernameExists(String username) {
        // TODO: Implement SQL query
        // SELECT COUNT(*) FROM users WHERE username = ?
        return false;
    }
}

