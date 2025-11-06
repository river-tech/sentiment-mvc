package com.team.app.dao;

import com.team.app.model.Job;
import java.util.List;

/**
 * JobDAO - Data Access Object for Job entity
 * 
 * TODO: Implement database operations for Job
 * TODO: Use HikariCP connection pool
 * TODO: Implement CRUD methods
 */
public class JobDAO {
    
    // TODO: Inject DataSource from HikariCP
    
    /**
     * Create a new job
     * TODO: Execute INSERT query
     * TODO: Return generated job ID
     */
    public int createJob(Job job) {
        // TODO: Implement SQL query
        // INSERT INTO jobs (user_id, name, status, created_at) VALUES (?, ?, 'PENDING', NOW())
        return -1;
    }
    
    /**
     * Find job by ID
     * TODO: Execute SELECT query
     * TODO: Map ResultSet to Job object
     */
    public Job findById(int jobId) {
        // TODO: Implement SQL query
        // SELECT * FROM jobs WHERE id = ?
        return null;
    }
    
    /**
     * Find all jobs by user ID
     * TODO: Execute SELECT query
     * TODO: Map ResultSet to List<Job>
     */
    public List<Job> findAllByUserId(int userId) {
        // TODO: Implement SQL query
        // SELECT * FROM jobs WHERE user_id = ? ORDER BY created_at DESC
        return null;
    }
    
    /**
     * Update job status
     * TODO: Execute UPDATE query
     */
    public boolean updateStatus(int jobId, String status) {
        // TODO: Implement SQL query
        // UPDATE jobs SET status = ? WHERE id = ?
        return false;
    }
    
    /**
     * Delete job
     * TODO: Execute DELETE query (or soft delete)
     */
    public boolean deleteJob(int jobId) {
        // TODO: Implement SQL query
        // DELETE FROM jobs WHERE id = ?
        return false;
    }
}

