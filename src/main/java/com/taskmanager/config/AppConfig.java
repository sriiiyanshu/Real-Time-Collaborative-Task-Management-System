package com.taskmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application-wide configuration settings
 */
public class AppConfig {
    
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    // Default configuration values
    private static final String DEFAULT_FILE_UPLOAD_PATH = "/uploads";
    private static final int DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int DEFAULT_SESSION_TIMEOUT = 30; // 30 minutes
    private static final boolean DEFAULT_ENABLE_WEBSOCKET = true;
    private static final String DEFAULT_FILE_STORAGE_PATH = "/storage"; // Default file storage path
    
    /**
     * Initialize configuration settings
     */
    public static void init() {
        if (!initialized) {
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                } else {
                    System.out.println("Warning: application.properties not found, using default settings");
                }
                initialized = true;
            } catch (IOException e) {
                System.err.println("Error loading application properties: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the configured file upload path
     */
    public static String getFileUploadPath() {
        init();
        return properties.getProperty("app.upload.path", DEFAULT_FILE_UPLOAD_PATH);
    }
    
    /**
     * Get the configured file storage path
     */
    public static String getFileStoragePath() {
        init();
        return properties.getProperty("app.storage.path", DEFAULT_FILE_STORAGE_PATH);
    }
    
    /**
     * Get maximum allowed file size for uploads (in bytes)
     */
    public static int getMaxFileSize() {
        init();
        try {
            return Integer.parseInt(properties.getProperty("app.upload.max_size", String.valueOf(DEFAULT_MAX_FILE_SIZE)));
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_FILE_SIZE;
        }
    }
    
    /**
     * Get HTTP session timeout (in minutes)
     */
    public static int getSessionTimeout() {
        init();
        try {
            return Integer.parseInt(properties.getProperty("app.session.timeout", String.valueOf(DEFAULT_SESSION_TIMEOUT)));
        } catch (NumberFormatException e) {
            return DEFAULT_SESSION_TIMEOUT;
        }
    }
    
    /**
     * Check if WebSocket functionality is enabled
     */
    public static boolean isWebSocketEnabled() {
        init();
        return Boolean.parseBoolean(properties.getProperty("app.websocket.enabled", String.valueOf(DEFAULT_ENABLE_WEBSOCKET)));
    }
    
    /**
     * Get application base URL (for emails, etc.)
     */
    public static String getAppBaseUrl() {
        init();
        return properties.getProperty("app.base_url", "http://localhost:8080/taskmanager");
    }
    
    /**
     * Get a custom property value
     */
    public static String getProperty(String key, String defaultValue) {
        init();
        return properties.getProperty(key, defaultValue);
    }
}
