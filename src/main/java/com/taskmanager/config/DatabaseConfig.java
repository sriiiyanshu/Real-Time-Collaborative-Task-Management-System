package com.taskmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration and connection management
 */
public class DatabaseConfig {
    
    private static final Properties properties = new Properties();
    private static boolean propertiesLoaded = false;
    
    /**
     * Load database properties from file
     */
    private static void loadProperties() {
        if (!propertiesLoaded) {
            try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (input == null) {
                    System.err.println("Unable to find database.properties");
                    throw new RuntimeException("Unable to find database.properties");
                }
                
                properties.load(input);
                propertiesLoaded = true;
            } catch (IOException ex) {
                System.err.println("Error loading database properties: " + ex.getMessage());
                throw new RuntimeException("Error loading database properties", ex);
            }
        }
    }
    
    /**
     * Get a property value, resolving environment variables if present
     * @param key The property key
     * @return The resolved property value
     */
    private static String resolveProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }
        
        // Check if the value contains an environment variable reference
        if (value.startsWith("${") && value.contains("}")) {
            String envVar = value.substring(2, value.indexOf("}"));
            String defaultValue = null;
            
            // Check if there's a default value after a colon
            if (envVar.contains(":")) {
                String[] parts = envVar.split(":", 2);
                envVar = parts[0];
                defaultValue = parts[1];
            }
            
            // Get the environment variable value or use default
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isEmpty()) {
                return envValue;
            } else if (defaultValue != null) {
                return defaultValue;
            } else {
                System.err.println("Environment variable " + envVar + " not set and no default provided");
                throw new RuntimeException("Required environment variable " + envVar + " is not defined");
            }
        }
        
        return value;
    }
    
    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        loadProperties();
        
        String url = properties.getProperty("db.url");
        String user = resolveProperty("db.username");
        String password = resolveProperty("db.password");
        
        try {
            Class.forName(properties.getProperty("db.driver"));
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    /**
     * Close database resources safely
     */
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get a database property
     */
    public static String getProperty(String key) {
        loadProperties();
        return resolveProperty(key);
    }
    
    /**
     * Check database connectivity
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
