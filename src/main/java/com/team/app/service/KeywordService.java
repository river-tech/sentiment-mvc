package com.team.app.service;

import com.team.app.dao.JobDAO;
import com.team.app.dao.SimilarJob;
import com.team.app.model.Job;
import com.team.app.util.HttpClientUtil;
import com.team.app.util.Logger;
import java.io.IOException;

public class KeywordService {
    private static final double SIMILARITY_THRESHOLD = 0.85;
    
    private final JobDAO jobDAO;
    
    public KeywordService() {
        this.jobDAO = new JobDAO();
    }
    
    public KeywordService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public Job processKeyword(String keyword) throws IOException {
        Logger.info("  [KeywordService] Bắt đầu processKeyword");
        Logger.info("     - Keyword: " + keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        
        Logger.info("  [Step 1] Gọi Flask API để lấy embedding vector...");
        double[] embedding = HttpClientUtil.getEmbedding(keyword);
        Logger.info("     Nhận được embedding vector: " + embedding.length + " dimensions");
        Logger.debug("     Embedding sample (first 5): " + 
            String.format("[%.4f, %.4f, %.4f, %.4f, %.4f, ...]", 
                embedding[0], embedding[1], embedding[2], embedding[3], embedding[4]));

        Logger.info("  [Step 2] Tìm job tương tự nhất trong DB...");
        SimilarJob similarJob = jobDAO.findMostSimilarJob(embedding);
        
        if (similarJob != null) {
            Logger.info("     Tìm thấy job tương tự:");
            Logger.info("        - Job ID: " + similarJob.getJobId());
            Logger.info("        - Similarity: " + String.format("%.4f", similarJob.getSimilarity()));
            Logger.info("        - Threshold: " + SIMILARITY_THRESHOLD);
        } else {
            Logger.info("     Không tìm thấy job nào trong DB");
        }

        if (similarJob != null && similarJob.isSimilarEnough(SIMILARITY_THRESHOLD)) {
            Logger.info("  [Step 3] Similarity >= " + SIMILARITY_THRESHOLD + " → Tái sử dụng job cũ");
            Job existingJob = jobDAO.findById(similarJob.getJobId());
            if (existingJob != null) {
                Logger.info("      Trả về job cũ (ID: " + existingJob.getId() + ")");
                Logger.info("      Articles sẽ được lấy từ DB (không crawl lại)");
                return existingJob;
            }
        }
        
        Logger.info("  [Step 4] Tạo job mới (similarity < " + SIMILARITY_THRESHOLD + " hoặc không có job tương tự)");
        long jobId = jobDAO.create(keyword, embedding);
        
        if (jobId <= 0) {
            Logger.error("     Tạo job thất bại - jobId = " + jobId);
            throw new RuntimeException("Failed to create job");
        }
        
        Logger.info("      Job đã được tạo trong DB:");
        Logger.info("        - Job ID: " + jobId);
        Logger.info("        - Keyword: " + keyword);
        Logger.info("        - Embedding: " + embedding.length + " dimensions");
        
        Job newJob = jobDAO.findById(jobId);
        if (newJob == null) {
            Logger.error("      Job đã tạo nhưng không tìm thấy khi query lại");
            throw new RuntimeException("Job created but not found");
        }
        
        Logger.info("     Job được load từ DB thành công");
        return newJob;
    }
}

