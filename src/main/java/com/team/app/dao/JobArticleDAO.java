package com.team.app.dao;

import com.team.app.config.DatabaseConfig;
import com.team.app.model.JobArticle;
import com.team.app.util.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JobArticleDAO {
    private DataSource dataSource;
    
    public JobArticleDAO() {
        this.dataSource = DatabaseConfig.getDataSource();
    }
    
    public JobArticleDAO(DataSource dataSource) {
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

    public List<JobArticle> findByJobId(int jobId) {
        Logger.info("[JobArticleDAO] Đang lấy articles từ DB cho job ID: " + jobId);
        String sql = "SELECT id, job_id, title, url, description, sentiment, created_at " +
                     "FROM job_articles WHERE job_id = ? ORDER BY created_at DESC";
        
        List<JobArticle> articles = new ArrayList<>();
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobArticle article = mapRow(rs);
                    articles.add(article);
                    Logger.info("[JobArticleDAO] └ Article #" + articles.size() + " | id=" + article.getId() 
                            + " | title=\"" + (article.getTitle() != null ? article.getTitle().substring(0, Math.min(50, article.getTitle().length())) : "") + "...\""
                            + " | sentiment=" + article.getSentiment());
                }
            }
        } catch (SQLException e) {
            Logger.error("[JobArticleDAO] findByJobId failed for jobId=" + jobId, e);
            throw new RuntimeException("findByJobId failed", e);
        }
        
        Logger.info("[JobArticleDAO] Đã lấy được " + articles.size() + " articles từ DB cho job ID: " + jobId);
        return articles;
    }

    public void insert(long jobId, JobArticle article) {
        if (article == null) return;
        boolean hasDescription = columnExists("job_articles", "description");

        String sql;
        if (hasDescription) {
            sql = "INSERT INTO job_articles (job_id, title, url, description, sentiment, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, NOW())";
        } else {
            sql = "INSERT INTO job_articles (job_id, title, url, sentiment, created_at) " +
                  "VALUES (?, ?, ?, ?, NOW())";
        }
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        
            ps.setLong(1, jobId);
            ps.setString(2, article.getTitle());
            ps.setString(3, article.getUrl());
        
            int paramIndex = 4;
            if (hasDescription) {
            ps.setString(4, article.getDescription());
                paramIndex = 5;
            }
        
            String sentiment = article.getSentiment();
if (sentiment == null || sentiment.isBlank()) {
    sentiment = "NEUTRAL";
} else {
    sentiment = sentiment.toUpperCase(java.util.Locale.ROOT);
}

ps.setObject(paramIndex, sentiment, java.sql.Types.OTHER);
        
            ps.executeUpdate();
        } catch (SQLException e) {
            com.team.app.util.Logger.error("[JobArticleDAO] Failed to insert article: jobId=" + jobId
                    + ", title=" + (article != null ? article.getTitle() : null)
                    + ", url=" + (article != null ? article.getUrl() : null), e);
            throw new RuntimeException("Failed to insert article", e);
        }
    }

    public void deleteByJobId(long jobId) {
        String sql = "DELETE FROM job_articles WHERE job_id = ?";
        
        try (Connection con = resolveDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByJobId failed", e);
        }
    }
    
    private JobArticle mapRow(ResultSet rs) throws SQLException {
        JobArticle article = new JobArticle();
        article.setId(rs.getLong("id"));
        article.setJobId(rs.getLong("job_id"));
        article.setTitle(rs.getString("title"));
        article.setUrl(rs.getString("url"));
        try {
        article.setDescription(rs.getString("description"));
        } catch (SQLException ignore) {
            article.setDescription(null);
        }
        article.setSentiment(rs.getString("sentiment"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            article.setCreatedAt(createdAt);
        }
        
        return article;
    }

    private boolean columnExists(String tableName, String columnName) {
        try (Connection con = resolveDataSource().getConnection();
             ResultSet rs = con.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}