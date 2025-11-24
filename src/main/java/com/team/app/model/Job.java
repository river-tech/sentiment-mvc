package com.team.app.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class Job {
    private Long id;
    private String keyword;   
    private String status;    
    private int progress;
    private double positive;
    private double negative;
    private double neutral;
    private String message;
    private double[] embedding;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<JobArticle> articles; 

    public Job() {}

    public Job(String keyword) {
        this.keyword = keyword;
        this.status = "QUEUED";
        this.progress = 0;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public double getPositive() { return positive; }
    public void setPositive(double positive) { this.positive = positive; }

    public double getNegative() { return negative; }
    public void setNegative(double negative) { this.negative = negative; }

    public double getNeutral() { return neutral; }
    public void setNeutral(double neutral) { this.neutral = neutral; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<JobArticle> getArticles() { return articles; }
    public void setArticles(List<JobArticle> articles) { this.articles = articles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", keyword='" + keyword + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                ", positive=" + positive +
                ", negative=" + negative +
                ", neutral=" + neutral +
                ", embedding=" + (embedding != null ? embedding.length + " dimensions" : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}