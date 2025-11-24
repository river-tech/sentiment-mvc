package com.team.app.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

public class KeywordEmbedding {
    private Long id;
    private String keyword;
    private float[] embedding; 
    private Timestamp createdAt;

    public KeywordEmbedding() {}

    public KeywordEmbedding(String keyword, float[] embedding) {
        this.keyword = keyword;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeywordEmbedding)) return false;
        KeywordEmbedding that = (KeywordEmbedding) o;
        return Objects.equals(id, that.id) && Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keyword);
    }

    @Override
    public String toString() {
        return "KeywordEmbedding{" +
                "id=" + id +
                ", keyword='" + keyword + '\'' +
                ", embedding=" + Arrays.toString(
                    embedding != null ? Arrays.copyOf(embedding, Math.min(embedding.length, 5)) : new float[0]
                  ) + "..."+
                ", createdAt=" + createdAt +
                '}';
    }
}