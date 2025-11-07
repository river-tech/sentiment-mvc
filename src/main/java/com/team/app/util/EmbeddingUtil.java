package com.team.app.util;

/**
 * EmbeddingUtil - Utility class for embedding vector operations
 * 
 * Provides methods for:
 * - Computing cosine similarity between vectors
 * - Converting vectors to PostgreSQL vector format
 */
public class EmbeddingUtil {
    
    private EmbeddingUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Calculate cosine similarity between two embedding vectors
     * 
     * Formula: sim = dot(a, b) / (||a|| * ||b||)
     * 
     * @param a First vector
     * @param b Second vector
     * @return Cosine similarity value between 0.0 and 1.0 (1.0 = identical, 0.0 = orthogonal)
     */
    public static double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        // Avoid division by zero
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * Convert double array to PostgreSQL vector format string
     * 
     * PostgreSQL pgvector format: [0.1,0.2,0.3,...]
     * 
     * @param vector Embedding vector as double array
     * @return PostgreSQL vector format string
     */
    public static String arrayToPgVector(double[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Parse PostgreSQL vector string to double array
     * 
     * @param pgVector PostgreSQL vector format string: [0.1,0.2,0.3,...]
     * @return double array
     */
    public static double[] pgVectorToArray(String pgVector) {
        if (pgVector == null || pgVector.trim().isEmpty() || pgVector.equals("[]")) {
            return new double[0];
        }
        
        // Remove brackets and split by comma
        String cleaned = pgVector.trim().replaceAll("^\\[|\\]$", "");
        if (cleaned.isEmpty()) {
            return new double[0];
        }
        
        String[] parts = cleaned.split(",");
        double[] result = new double[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        
        return result;
    }
}

