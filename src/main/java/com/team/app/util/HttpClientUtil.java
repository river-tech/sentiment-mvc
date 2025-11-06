package com.team.app.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HttpClientUtil - Utility class for HTTP requests
 * 
 * TODO: Implement HTTP GET/POST methods
 * TODO: Handle HTTP errors
 * TODO: Support timeout configuration
 * TODO: Parse response content
 */
public class HttpClientUtil {
    
    /**
     * Send HTTP GET request
     * TODO: Create HttpURLConnection
     * TODO: Set request method and headers
     * TODO: Read response
     * TODO: Handle errors
     */
    public static String sendGet(String urlString) throws IOException {
        // TODO: Implement GET request
        // URL url = new URL(urlString);
        // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setRequestMethod("GET");
        // conn.setConnectTimeout(5000);
        // conn.setReadTimeout(5000);
        // TODO: Read response
        return null;
    }
    
    /**
     * Send HTTP POST request
     * TODO: Create HttpURLConnection
     * TODO: Set request method and headers
     * TODO: Write request body
     * TODO: Read response
     */
    public static String sendPost(String urlString, String body) throws IOException {
        // TODO: Implement POST request
        return null;
    }
}

