package com.taskmanager.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.taskmanager.config.AppConfig;
import com.taskmanager.config.DatabaseConfig;

/**
 * Application lifecycle listener
 * Handles application startup and shutdown events
 */
@WebListener
public class ApplicationListener implements ServletContextListener {
    
    /**
     * Called when the application is starting up
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Task Management System is starting up...");
        
        // Initialize application configuration
        AppConfig.init();
        
        // Test database connectivity
        try {
            boolean dbConnectionSuccessful = DatabaseConfig.testConnection();
            if (dbConnectionSuccessful) {
                System.out.println("Database connection test successful");
            } else {
                System.err.println("WARNING: Database connection test failed");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Database initialization failed: " + e.getMessage());
        }
        
        // Set application-wide attributes
        sce.getServletContext().setAttribute("appName", "Task Management System");
        sce.getServletContext().setAttribute("appVersion", "1.0.0");
        
        // Create upload directory if it doesn't exist
        java.io.File uploadDir = new java.io.File(AppConfig.getFileUploadPath());
        if (!uploadDir.exists()) {
            if (uploadDir.mkdirs()) {
                System.out.println("Upload directory created at: " + uploadDir.getAbsolutePath());
            } else {
                System.err.println("WARNING: Failed to create upload directory");
            }
        }
        
        // Create log directories for log4j2
        String homeDir = System.getProperty("user.home");
        java.io.File logDir = new java.io.File(homeDir + "/logs/taskmanager");
        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                System.out.println("Log directory created at: " + logDir.getAbsolutePath());
            } else {
                System.err.println("WARNING: Failed to create log directory at: " + logDir.getAbsolutePath());
            }
        }
        
        System.out.println("Task Management System startup completed");
    }
    
    /**
     * Called when the application is shutting down
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Task Management System is shutting down...");
        
        // Perform cleanup operations
        
        System.out.println("Task Management System shutdown completed");
    }
}