package com.team.app.dao;

import com.team.app.config.DatabaseConfig;
import com.team.app.model.Job;
import com.team.app.util.EmbeddingUtil;
import com.team.app.util.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * JobDAO - Data Access Object for Job entity
 * 
 * Handles database operations for jobs including embedding vector similarity search
 */
public class JobDAO {
    private DataSource dataSource;
    
    public JobDAO() {
        this.dataSource = DatabaseConfig.getDataSource();
    }
    
    public JobDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataSource resolveDataSource() {
        if (dataSource == null) {
            dataSource = DatabaseConfig.getDataSource();
        }
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not initialized. Ensure DatabaseConfig is loaded.");
        }
        return dataSource;
    }
    
    /**
     * Find the most similar job by embedding vector using PostgreSQL pgvector
     * 
     * Uses cosine distance operator (<=>) which is faster than manual cosine calculation
     * Similarity = 1 - distance (distance ranges from 0 to 2, similarity from 1 to -1)
     * 
     * SQL: SELECT id, keyword, 1 - (embedding <=> ?) AS similarity
     *      FROM jobs
     *      WHERE embedding IS NOT NULL
     *      ORDER BY similarity DESC
     *      LIMIT 1;
     * 
     * Note: If using PostgreSQL pgvector extension, prefer embedding <=> ?::vector 
     * because it's faster than manual cosine similarity calculation
     * 
     * @param embedding Embedding vector to compare (float[] or double[])
     * @return SimilarJob with jobId and similarity score, or null if no jobs found
     */
    public SimilarJob findMostSimilarJob(double[] embedding) {
        Logger.info("    [JobDAO] Tìm job tương tự nhất");
        Logger.info("       - Vector dimensions: " + (embedding != null ? embedding.length : 0));
        
        if (embedding == null || embedding.length == 0) {
            Logger.warn("       ⚠️  Vector null hoặc rỗng");
            return null;
        }
        
        // Convert vector to PostgreSQL format
        String pgVector = EmbeddingUtil.arrayToPgVector(embedding);
        
        // Use pgvector cosine distance operator (<=>)
        // 1 - distance = similarity (distance 0 = identical, distance 2 = opposite)
        // ORDER BY similarity DESC to get highest similarity first
        String sql = "SELECT id, keyword, 1 - (embedding <=> ?::vector) AS similarity " +
                     "FROM jobs " +
                     "WHERE embedding IS NOT NULL " +
                     "ORDER BY embedding <=> ?::vector " +
                     "LIMIT 1";
        
        Logger.debug("       - SQL: SELECT id, keyword, 1 - (embedding <=> ?::vector) AS similarity ... LIMIT 1");
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pgVector);
            ps.setString(2, pgVector);
            
            Logger.info("       - Đang query DB...");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long jobId = rs.getLong("id");
                    double similarity = rs.getDouble("similarity");
                    String keyword = rs.getString("keyword");
                    Logger.info("       ✅ Tìm thấy job tương tự:");
                    Logger.info("          - Job ID: " + jobId);
                    Logger.info("          - Keyword: " + keyword);
                    Logger.info("          - Similarity: " + String.format("%.4f", similarity));
                    return new SimilarJob(jobId, similarity);
                } else {
                    Logger.info("       ℹ️  Không tìm thấy job nào có embedding trong DB");
                }
            }
        } catch (SQLException e) {
            Logger.error("       ❌ Lỗi SQL khi tìm similar job", e);
            throw new RuntimeException("findMostSimilarJob failed", e);
        }
        
        return null;
    }
    
    /**
     * Create a new job with embedding vector
     * 
     * @param keyword Job keyword
     * @param embedding Embedding vector (384 dimensions)
     * @return Generated job ID, or -1 if failed
     */
    public long create(String keyword, double[] embedding) {
        Logger.info("    [JobDAO] Bắt đầu tạo job trong DB");
        Logger.info("       - Keyword: " + keyword);
        Logger.info("       - Embedding: " + (embedding != null ? embedding.length + " dimensions" : "null"));
        
        String pgVector = embedding != null ? EmbeddingUtil.arrayToPgVector(embedding) : null;
        
        String sql = "INSERT INTO jobs (keyword, status, progress, embedding, created_at, updated_at) " +
                     "VALUES (?, 'QUEUED'::job_status, 0, ?::vector, NOW(), NOW()) RETURNING id";
        
        Logger.debug("       - SQL: INSERT INTO jobs (keyword, status, progress, embedding, ...) RETURNING id");
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            if (pgVector != null) {
                ps.setString(2, pgVector);
                Logger.debug("       - Embedding vector length: " + pgVector.length() + " chars");
            } else {
                ps.setNull(2, java.sql.Types.OTHER);
                Logger.warn("       - Embedding vector là null");
            }
            
            Logger.info("       - Đang execute INSERT...");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long jobId = rs.getLong("id");
                    Logger.info("       ✅ Job đã được INSERT vào DB thành công!");
                    Logger.info("       ✅ Job ID được tạo: " + jobId);
                    Logger.info("       ✅ Status: QUEUED");
                    return jobId;
                } else {
                    Logger.error("       ❌ INSERT thành công nhưng không có RETURNING id");
                }
            }
        } catch (SQLException e) {
            Logger.error("       ❌ Lỗi SQL khi tạo job", e);
            Logger.error("       ❌ SQL Error: " + e.getMessage());
            throw new RuntimeException("create job failed", e);
        }
        
        Logger.error("       ❌ Tạo job thất bại - không có jobId được trả về");
        return -1;
    }

    /**
     * Create a new job with default status QUEUED (no embedding provided).
     */
    public long create(String keyword) {
        String sql = "INSERT INTO jobs (keyword, status, progress, created_at, updated_at) " +
                "VALUES (?, 'QUEUED'::job_status, 0, NOW(), NOW()) RETURNING id";
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long jobId = rs.getLong("id");
                    Logger.info("       ✅ Job đã được tạo (QUEUED) - ID: " + jobId);
                    return jobId;
                }
            }
        } catch (SQLException e) {
            Logger.error("       ❌ Lỗi SQL khi tạo job (no embedding)", e);
            throw new RuntimeException("create job failed", e);
        }
        return -1;
    }
    
    /**
     * Find job by ID
     */
    public Job findById(long jobId) {
        String sql = "SELECT id, keyword, status, progress, positive, negative, neutral, " +
                     "message, embedding, created_at, updated_at " +
                     "FROM jobs WHERE id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.error("[JobDAO] findById failed for jobId=" + jobId, e);
            throw new RuntimeException("findById failed", e);
        }
        
        return null;
    }
    
    /**
     * Find all jobs (system-wide, no user filtering)
     * 
     * @return List of all jobs ordered by creation date (newest first)
     */
    public List<Job> findAll() {
        String sql = "SELECT id, keyword, status, progress, positive, negative, neutral, " +
                     "message, embedding, created_at, updated_at " +
                     "FROM jobs ORDER BY created_at DESC";
        
        List<Job> jobs = new ArrayList<>();
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            Logger.error("[JobDAO] findAll failed", e);
            throw new RuntimeException("findAll failed", e);
        }
        
        return jobs;
    }
    
    /**
     * Update job status
     */
    public boolean updateStatus(long jobId, String status) {
        String sql = "UPDATE jobs SET status = ?::job_status, updated_at = NOW() WHERE id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, jobId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("[JobDAO] updateStatus failed (jobId=" + jobId + ", status=" + status + ")", e);
            throw new RuntimeException("updateStatus failed", e);
        }
    }

    /**
     * Update job status and progress in a single operation.
     */
    public boolean updateStatus(long jobId, String status, int progress) {
        String sql = "UPDATE jobs SET status = ?::job_status, progress = ?, updated_at = NOW() WHERE id = ?";

        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, progress);
            ps.setLong(3, jobId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("[JobDAO] updateStatus with progress failed (jobId=" + jobId + ", status=" + status
                    + ", progress=" + progress + ")", e);
            throw new RuntimeException("updateStatus with progress failed", e);
        }
    }
    
    /**
     * Update job sentiment results
     */
    public boolean updateSentiment(long jobId, double positive, double negative, double neutral) {
        String sql = "UPDATE jobs SET positive = ?, negative = ?, neutral = ?, " +
                     "status = 'DONE'::job_status, updated_at = NOW() WHERE id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, positive);
            ps.setDouble(2, negative);
            ps.setDouble(3, neutral);
            ps.setLong(4, jobId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("updateSentiment failed", e);
        }
    }

    /**
     * Update job sentiment from map results.
     */
    public boolean updateSentiment(long jobId, java.util.Map<String, Double> sentiment) {
        if (sentiment == null || sentiment.isEmpty()) {
            return updateSentiment(jobId, 0.0, 0.0, 100.0);
        }
        double positive = sentiment.getOrDefault("positive", 0.0);
        double negative = sentiment.getOrDefault("negative", 0.0);
        double neutral = sentiment.getOrDefault("neutral", 0.0);
        return updateSentiment(jobId, positive, negative, neutral);
    }
    
    /**
     * Update job embedding vector
     * 
     * @param jobId Job ID
     * @param embedding Embedding vector (384 dimensions)
     * @return true if update successful
     */
    public boolean updateEmbedding(long jobId, double[] embedding) {
        Logger.info("    [JobDAO] Cập nhật embedding cho job ID: " + jobId);
        
        String pgVector = embedding != null ? EmbeddingUtil.arrayToPgVector(embedding) : null;
        
        String sql = "UPDATE jobs SET embedding = ?::vector, updated_at = NOW() WHERE id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (pgVector != null) {
                ps.setString(1, pgVector);
            } else {
                ps.setNull(1, java.sql.Types.OTHER);
            }
            ps.setLong(2, jobId);
            
            int rowsUpdated = ps.executeUpdate();
            Logger.info("       ✅ Cập nhật embedding: " + (rowsUpdated > 0 ? "thành công" : "không có job nào được cập nhật"));
            return rowsUpdated > 0;
        } catch (SQLException e) {
            Logger.error("       ❌ Lỗi SQL khi cập nhật embedding", e);
            throw new RuntimeException("updateEmbedding failed", e);
        }
    }
    
    /**
     * Delete job
     */
    public boolean deleteJob(long jobId) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("[JobDAO] deleteJob failed (jobId=" + jobId + ")", e);
            throw new RuntimeException("deleteJob failed", e);
        }
    }

    /**
     * Mark job as FAILED with a message.
     */
    public void markFailed(long jobId, String reason) {
        String sql = "UPDATE jobs SET status = 'FAILED'::job_status, message = ?, updated_at = NOW() WHERE id = ?";
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setLong(2, jobId);
            ps.executeUpdate();
            Logger.warn("[JobDAO] Job " + jobId + " marked as FAILED: " + reason);
        } catch (SQLException e) {
            Logger.error("[JobDAO] markFailed failed for jobId=" + jobId, e);
            throw new RuntimeException("markFailed failed", e);
        }
    }
    
    /**
     * Map ResultSet row to Job object
     */
    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getLong("id"));
        job.setKeyword(rs.getString("keyword"));
        job.setStatus(rs.getString("status"));
        job.setProgress(rs.getInt("progress"));
        job.setPositive(rs.getDouble("positive"));
        job.setNegative(rs.getDouble("negative"));
        job.setNeutral(rs.getDouble("neutral"));
        job.setMessage(rs.getString("message"));
        
        // Handle embedding vector
        try {
            String embeddingStr = rs.getString("embedding");
            if (embeddingStr != null && !embeddingStr.trim().isEmpty()) {
                job.setEmbedding(EmbeddingUtil.pgVectorToArray(embeddingStr));
            }
        } catch (SQLException e) {
            // Embedding column might not exist or be null - ignore
            Logger.debug("       - Embedding không có hoặc null");
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            job.setCreatedAt(createdAt);
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            job.setUpdatedAt(updatedAt);
    }
        
        return job;
    }
}
