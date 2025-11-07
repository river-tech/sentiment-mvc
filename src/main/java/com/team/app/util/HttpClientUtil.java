package com.team.app.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HttpClientUtil - Utility class for HTTP requests
 * 
 * Provides methods for:
 * - Sending HTTP GET/POST requests
 * - Getting embedding vectors from Flask API
 */
public class HttpClientUtil {
    
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 30000; // 30 seconds
    
    /**
     * Send HTTP GET request
     */
    public static String sendGet(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP GET failed with response code: " + responseCode);
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Send HTTP POST request
     */
    public static String sendPost(String urlString, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            
            // Write request body
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(body);
                writer.flush();
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP POST failed with response code: " + responseCode);
            }
            
            // Read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Get embedding vector from Flask API
     * 
     * Sends POST request to http://127.0.0.1:9696/embed with JSON body:
     * { "keyword": "..." }
     * 
     * Expects JSON response:
     * { "embedding": [0.12, 0.85, ...] }
     * 
     * @param keyword Keyword to get embedding for
     * @return Embedding vector as double array (384 dimensions)
     * @throws IOException If HTTP request fails or response is invalid
     */
    public static double[] getEmbedding(String keyword) throws IOException {
        Logger.info("    [HttpClientUtil] Gọi embedding API");
        Logger.info("       - URL: http://127.0.0.1:9696/embed");
        Logger.info("       - Keyword: " + keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        
        String apiUrl = "http://127.0.0.1:9696/embed";
        
        // Build JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("keyword", keyword);
        
        Logger.debug("       - Request body: " + requestBody.toString());
        
        // Send POST request
        long startTime = System.currentTimeMillis();
        String responseJson = sendPost(apiUrl, requestBody.toString());
        long duration = System.currentTimeMillis() - startTime;
        
        Logger.info("       ✅ Nhận response từ API (thời gian: " + duration + "ms)");
        Logger.debug("       - Response length: " + responseJson.length() + " chars");
        
        // Parse JSON response
        JsonObject response = JsonParser.parseString(responseJson).getAsJsonObject();
        
        if (!response.has("embedding")) {
            Logger.error("       ❌ Response không có field 'embedding'");
            throw new IOException("Invalid response: missing 'embedding' field");
        }
        
        JsonArray embeddingArray = response.getAsJsonArray("embedding");
        double[] embedding = new double[embeddingArray.size()];
        
        for (int i = 0; i < embeddingArray.size(); i++) {
            embedding[i] = embeddingArray.get(i).getAsDouble();
        }
        
        Logger.info("       ✅ Parse embedding thành công: " + embedding.length + " dimensions");
        
        return embedding;
    }
}

