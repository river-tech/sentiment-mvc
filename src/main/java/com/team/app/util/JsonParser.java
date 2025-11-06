package com.team.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JsonParser - Utility class for JSON parsing
 * 
 * TODO: Implement JSON serialization/deserialization
 * TODO: Use Gson library
 * TODO: Handle JSON parsing errors
 */
public class JsonParser {
    
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    /**
     * Convert object to JSON string
     * TODO: Use Gson.toJson()
     */
    public static String toJson(Object obj) {
        // TODO: Implement
        return gson.toJson(obj);
    }
    
    /**
     * Convert JSON string to object
     * TODO: Use Gson.fromJson()
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        // TODO: Implement
        return gson.fromJson(json, clazz);
    }
}

