package com.team.app.service;

import com.team.app.dao.JobDAO;
import com.team.app.dao.SimilarJob;
import com.team.app.model.Job;
import com.team.app.util.HttpClientUtil;
import com.team.app.util.Logger;
import java.io.IOException;

/**
 * KeywordService - Business logic for keyword processing with semantic matching
 * 
 * Handles:
 * - Getting embedding vectors from Flask API
 * - Finding similar jobs using embedding similarity
 * - Creating new jobs when no similar job found
 */
public class KeywordService {
    private static final double SIMILARITY_THRESHOLD = 0.85;
    
    private final JobDAO jobDAO;
    
    public KeywordService() {
        this.jobDAO = new JobDAO();
    }
    
    public KeywordService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }
    
    /**
     * Process keyword: get embedding, find similar job, or create new job
     * 
     * Flow:
     * 1. Call Flask API to get embedding vector for keyword
     * 2. Find most similar job using embedding similarity
     * 3. If similarity >= 0.85 → reuse existing job (don't crawl again)
     * 4. If no similar job found → create new job with embedding
     * 
     * @param keyword Keyword to process
     * @return Job object (either existing similar job or newly created job)
     * @throws IOException If Flask API call fails
     * @throws RuntimeException If job creation fails
     */
    public Job processKeyword(String keyword) throws IOException {
        Logger.info("  [KeywordService] Bắt đầu processKeyword");
        Logger.info("     - Keyword: " + keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        
        // Step 1: Get embedding vector from Flask API
        Logger.info("  [Step 1] Gọi Flask API để lấy embedding vector...");
        double[] embedding = HttpClientUtil.getEmbedding(keyword);
        Logger.info("     ✅ Nhận được embedding vector: " + embedding.length + " dimensions");
        Logger.debug("     Embedding sample (first 5): " + 
            String.format("[%.4f, %.4f, %.4f, %.4f, %.4f, ...]", 
                embedding[0], embedding[1], embedding[2], embedding[3], embedding[4]));
        
        // Step 2: Find most similar job
        Logger.info("  [Step 2] Tìm job tương tự nhất trong DB...");
        SimilarJob similarJob = jobDAO.findMostSimilarJob(embedding);
        
        if (similarJob != null) {
            Logger.info("     ✅ Tìm thấy job tương tự:");
            Logger.info("        - Job ID: " + similarJob.getJobId());
            Logger.info("        - Similarity: " + String.format("%.4f", similarJob.getSimilarity()));
            Logger.info("        - Threshold: " + SIMILARITY_THRESHOLD);
        } else {
            Logger.info("     ℹ️  Không tìm thấy job nào trong DB");
        }
        
        // Step 3: Check if similarity is high enough to reuse
        if (similarJob != null && similarJob.isSimilarEnough(SIMILARITY_THRESHOLD)) {
            Logger.info("  [Step 3] Similarity >= " + SIMILARITY_THRESHOLD + " → Tái sử dụng job cũ");
            // Reuse existing job (don't crawl again)
            Job existingJob = jobDAO.findById(similarJob.getJobId());
            if (existingJob != null) {
                Logger.info("     ✅ Trả về job cũ (ID: " + existingJob.getId() + ")");
                return existingJob;
            }
        }
        
        // Step 4: No similar job found or similarity too low → create new job
        Logger.info("  [Step 4] Tạo job mới (similarity < " + SIMILARITY_THRESHOLD + " hoặc không có job tương tự)");
        long jobId = jobDAO.create(keyword, embedding);
        
        if (jobId <= 0) {
            Logger.error("     ❌ Tạo job thất bại - jobId = " + jobId);
            throw new RuntimeException("Failed to create job");
        }
        
        Logger.info("     ✅ Job đã được tạo trong DB:");
        Logger.info("        - Job ID: " + jobId);
        Logger.info("        - Keyword: " + keyword);
        Logger.info("        - Embedding: " + embedding.length + " dimensions");
        
        Job newJob = jobDAO.findById(jobId);
        if (newJob == null) {
            Logger.error("     ❌ Job đã tạo nhưng không tìm thấy khi query lại");
            throw new RuntimeException("Job created but not found");
        }
        
        Logger.info("     ✅ Job được load từ DB thành công");
        return newJob;
    }
}

