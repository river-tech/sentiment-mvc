package com.team.app.dao;

/**
 * SimilarJob - Result class for similarity search
 * Contains job ID and similarity score
 */
public class SimilarJob {
    private final Long jobId;
    private final double similarity;
    
    public SimilarJob(Long jobId, double similarity) {
        this.jobId = jobId;
        this.similarity = similarity;
    }
    
    public Long getJobId() {
        return jobId;
    }
    
    public double getSimilarity() {
        return similarity;
    }
    
    public boolean isSimilarEnough(double threshold) {
        return similarity >= threshold;
    }
}

