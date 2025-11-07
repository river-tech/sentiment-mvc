package com.team.app.service;

import com.team.app.model.JobArticle;
import com.team.app.util.Logger;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * SentimentService - Simple rule-based sentiment analysis using word lists.
 */
public class SentimentService {

    private static final Set<String> POSITIVE_KEYWORDS;
    private static final Set<String> NEGATIVE_KEYWORDS;

    static {
        Set<String> positive = new HashSet<>();
        positive.addAll(Arrays.asList(
            "good", "great", "excellent", "positive", "gain", "success",
            "improve", "benefit", "growth", "strong", "optimistic", "up",
            "win", "secure", "stability", "profit", "favorable", "bullish",
            "record", "surge", "rally", "amazing", "happy", "love",
            "tang", "tang truong", "tang trưởng", "tangtruong", "tăng trưởng",
            "tich cuc", "tichcuc", "tích cực", "trien vong", "trienvong",
            "thuong loi", "thuongloi", "ky vong", "kyvong", "ben cung",
            "bencung", "ky ket", "kyket", "ky ket thanh cong", "kyketthanhcong"
        ));

        Set<String> negative = new HashSet<>();
        negative.addAll(Arrays.asList(
            "bad", "poor", "negative", "loss", "decline", "drop",
            "weak", "down", "risk", "warn", "warning", "crash", "fail",
            "collapse", "bearish", "cut", "fall", "uncertain", "lawsuit",
            "fraud", "crisis", "fear", "slowdown", "terrible", "hate",
            "sad", "angry", "giam", "giảm", "suy giam", "suygiam",
            "tieu cuc", "tieucuc", "tiêu cực", "khung hoang", "khunghoang",
            "thiet thoi", "thietthoi", "thua", "thua lo", "thua lỗ",
            "sut giam", "sutgiam", "kho han", "khohan", "dich benh", "dichbenh"
        ));

        POSITIVE_KEYWORDS = Collections.unmodifiableSet(positive);
        NEGATIVE_KEYWORDS = Collections.unmodifiableSet(negative);
    }

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
        Logger.info("Sentiment stats -> positive: " + stats.getPositivePercentage()
                + "% negative: " + stats.getNegativePercentage()
                + "% neutral: " + stats.getNeutralPercentage());
        return stats;
    }

    private String classifyArticle(JobArticle article) {
        if (article == null) {
            return "neutral";
        }
        StringBuilder builder = new StringBuilder();
        if (article.getTitle() != null) {
            builder.append(article.getTitle()).append(' ');
        }
        if (article.getDescription() != null) {
            builder.append(article.getDescription());
        }
        return classifyText(builder.toString());
    }

    private String classifyText(String text) {
        if (text == null || text.isBlank()) {
            return "neutral";
        }

        String lower = text.toLowerCase(Locale.ROOT);
        String normalized = normalize(text);

        int score = 0;
        for (String keyword : POSITIVE_KEYWORDS) {
            if (containsKeyword(lower, normalized, keyword)) {
                score++;
            }
        }
        for (String keyword : NEGATIVE_KEYWORDS) {
            if (containsKeyword(lower, normalized, keyword)) {
                score--;
            }
        }

        if (score > 0) {
            return "positive";
        }
        if (score < 0) {
            return "negative";
        }
        return "neutral";
    }

    private boolean containsKeyword(String lower, String normalizedText, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        String keyLower = keyword.toLowerCase(Locale.ROOT);
        if (lower.contains(keyLower)) {
            return true;
        }
        String normalizedKey = normalize(keyword);
        if (!Objects.equals(normalizedKey, keyLower) && normalizedText.contains(normalizedKey)) {
            return true;
        }
        return false;
    }

    /**
     * Normalize text by lower-casing and removing diacritics/non-letter characters.
     */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String lowerCase = noAccents.toLowerCase(Locale.ROOT);
        return lowerCase.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
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

