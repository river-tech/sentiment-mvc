package com.team.app.service;

import com.team.app.model.JobArticle;
import com.team.app.util.Logger;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SentimentService - Flask API based sentiment analysis.
 * Calls Python service http://127.0.0.1:9697/sentiment
 * Replaces old rule-based classifier.
 */
public class SentimentService {

    private static final String API_URL = "http://127.0.0.1:9697/sentiment";

    /**
     * Analyze a list of articles and return percentage distribution map.
     */
    public Map<String, Double> analyze(List<JobArticle> articles) {
        SentimentStats stats = analyzeInternal(articles);
        if (stats.getTotal() == 0) {
            return neutralOnlyResult();
        }

        Map<String, Double> result = new HashMap<>();
        result.put("positive", stats.getPositivePercentage());
        result.put("negative", stats.getNegativePercentage());
        result.put("neutral", stats.getNeutralPercentage());
        return result;
    }

    /**
     * Analyze sentiment for a list of articles and return computed statistics.
     */
    public SentimentStats analyzeArticles(List<JobArticle> articles) {
        return analyzeInternal(articles);
    }

    /**
     * Analyze a single article and return the sentiment label.
     */
    public String analyzeSentiment(JobArticle article) {
        return classifyArticle(article);
    }

    /**
     * Analyze articles currently stored for a given job and update their sentiment labels.
     * This helper can be used for manual reprocessing scenarios.
     */
    public SentimentStats batchAnalyzeSentiment(List<JobArticle> articles) {
        return analyzeInternal(articles);
    }

    private SentimentStats analyzeInternal(List<JobArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            Logger.warn("Sentiment analysis received empty article list");
            return new SentimentStats(0, 0, 0, 0);
        }

        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (JobArticle article : articles) {
            String sentiment = classifyArticle(article);
            article.setSentiment(sentiment);

            switch (sentiment) {
                case "positive" -> positive++;
                case "negative" -> negative++;
                default -> neutral++;
            }
        }

        SentimentStats stats = new SentimentStats(positive, negative, neutral, articles.size());
        Logger.info(String.format("Sentiment stats -> positive: %.2f%% negative: %.2f%% neutral: %.2f%%",
                stats.getPositivePercentage(), stats.getNegativePercentage(), stats.getNeutralPercentage()));
        return stats;
    }

    /**
     * Classify single article by calling Flask API
     */
    private String classifyArticle(JobArticle article) {
        if (article == null) return "neutral";

        StringBuilder builder = new StringBuilder();
        if (article.getTitle() != null) builder.append(article.getTitle()).append(' ');
        if (article.getDescription() != null) builder.append(article.getDescription());
        String text = builder.toString().trim();

        if (text.isEmpty()) return "neutral";

        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Properly escape JSON string
            String escapedText = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            String jsonBody = "{\"text\":\"" + escapedText + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject obj = new JSONObject(response.body());
                String label = obj.optString("label", "neutral").toLowerCase();
                
                // Normalize label to expected values
                if (label.equals("pos") || label.equals("positive")) {
                    return "positive";
                } else if (label.equals("neg") || label.equals("negative")) {
                    return "negative";
                } else {
                    return "neutral";
                }
            } else {
                Logger.warn("Flask API returned non-200 status: " + response.statusCode() + " for text: " + text.substring(0, Math.min(50, text.length())));
            }
        } catch (java.net.http.HttpTimeoutException e) {
            Logger.error("Sentiment API timeout: " + e.getMessage());
        } catch (java.io.IOException e) {
            Logger.error("Sentiment API IO error: " + e.getMessage());
        } catch (Exception e) {
            Logger.error("Sentiment API failed: " + e.getMessage(), e);
        }
        return "neutral";
    }

    private Map<String, Double> neutralOnlyResult() {
        Map<String, Double> result = new HashMap<>();
        result.put("positive", 0.0);
        result.put("negative", 0.0);
        result.put("neutral", 100.0);
        return result;
    }

    /**
     * Immutable statistics record.
     */
    public static class SentimentStats {
        private final int positiveCount;
        private final int negativeCount;
        private final int neutralCount;
        private final int total;
        private final double positivePercentage;
        private final double negativePercentage;
        private final double neutralPercentage;

        public SentimentStats(int positiveCount, int negativeCount, int neutralCount, int total) {
            this.positiveCount = positiveCount;
            this.negativeCount = negativeCount;
            this.neutralCount = neutralCount;
            this.total = total;

            if (total > 0) {
                this.positivePercentage = round(positiveCount * 100.0 / total);
                this.negativePercentage = round(negativeCount * 100.0 / total);
                this.neutralPercentage = round(neutralCount * 100.0 / total);
            } else {
                this.positivePercentage = 0.0;
                this.negativePercentage = 0.0;
                this.neutralPercentage = 100.0;
            }
        }

        private double round(double value) {
            return Math.round(value * 100.0) / 100.0;
        }

        public int getPositiveCount() {
            return positiveCount;
        }

        public int getNegativeCount() {
            return negativeCount;
        }

        public int getNeutralCount() {
            return neutralCount;
        }

        public int getTotal() {
            return total;
        }

        public double getPositivePercentage() {
            return positivePercentage;
        }

        public double getNegativePercentage() {
            return negativePercentage;
        }

        public double getNeutralPercentage() {
            return neutralPercentage;
        }
    }
}
