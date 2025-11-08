package com.team.app.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the application
 * Logs to both console and file
 */
public class Logger {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter fileWriter;
    private static String logFilePath;
    
    static {
        try {
            // Try multiple locations for log file
            String userHome = System.getProperty("user.home");
            String projectPath = System.getProperty("user.dir");
            
            // Priority: project logs > user home logs > current directory
            Path logDir = null;
            String logLocation = "unknown";
            
            // Check if running in project directory
            if (projectPath != null && projectPath.contains("sentiment-mvc")) {
                logDir = Paths.get(projectPath, "logs");
                logLocation = "project directory";
            } else if (userHome != null) {
                // Use absolute path in user home
                logDir = Paths.get(userHome, "sentiment-mvc-logs");
                logLocation = "user home";
            } else {
                // Fallback to current directory
                logDir = Paths.get("logs");
                logLocation = "current directory";
            }
            
            // Create logs directory if it doesn't exist
            Files.createDirectories(logDir);
            
            logFilePath = logDir.resolve("sentiment-mvc.log").toString();
            fileWriter = new PrintWriter(new FileWriter(logFilePath, true), true);
            
            // Always print to console where log file is
            String initMsg = String.format("[Logger] Initialized - Log file: %s (location: %s, projectPath: %s)", 
                logFilePath, logLocation, projectPath);
            System.out.println(initMsg);
            fileWriter.println(initMsg);
            fileWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Print to console
        System.out.println(logMessage);
        
        // Write to file with immediate flush
        if (fileWriter != null) {
            try {
                fileWriter.println(logMessage);
                fileWriter.flush();
            } catch (Exception e) {
                // If file write fails, at least print to console
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        } else {
            // If fileWriter is null, try to reinitialize
            System.err.println("WARNING: Logger fileWriter is null. Log message: " + logMessage);
        }
    }
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void error(String message, Throwable e) {
        log("ERROR", message + ": " + e.getMessage());
        if (e.getStackTrace() != null && e.getStackTrace().length > 0) {
            log("ERROR", "Stack trace: " + e.getStackTrace()[0].toString());
        }
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    /**
     * Get log file path
     */
    public static String getLogFilePath() {
        return logFilePath != null ? logFilePath : "logs/sentiment-mvc.log";
    }
}

