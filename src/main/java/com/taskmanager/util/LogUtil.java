package com.taskmanager.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Date;

/**
 * Utility class for application logging.
 */
public class LogUtil {
    
    private static final Logger LOGGER = Logger.getLogger("com.taskmanager");
    private static final String LOG_FOLDER = "logs";
    private static boolean initialized = false;
    
    static {
        initialize();
    }
    
    /**
     * Initializes the logging system.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_FOLDER);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Remove existing handlers
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
            
            // Add console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            LOGGER.addHandler(consoleHandler);
            
            // Add file handler with daily rotation
            FileHandler fileHandler = new FileHandler(LOG_FOLDER + "/taskmanager_%g_%u.log", 5242880, 10, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFormatter());
            LOGGER.addHandler(fileHandler);
            
            // Set global logging level
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);
            
            initialized = true;
        } catch (IOException e) {
            System.err.println("Error initializing logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Logs a debug message.
     * 
     * @param message The message to log
     */
    public static void debug(String message) {
        LOGGER.log(Level.FINE, message);
    }
    
    /**
     * Logs an info message.
     * 
     * @param message The message to log
     */
    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message The message to log
     * @param throwable The associated exception
     */
    public static void warn(String message, Throwable throwable) {
        LOGGER.log(Level.WARNING, message, throwable);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The message to log
     * @param throwable The associated exception
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Custom log formatter for better readability.
     */
    private static class LogFormatter extends SimpleFormatter {
        private static final String FORMAT = "[%1$tF %1$tT] [%2$s] [%3$s] %4$s %n";
        
        @Override
        public String format(LogRecord record) {
            String sourceClassName = record.getSourceClassName();
            if (sourceClassName.contains(".")) {
                sourceClassName = sourceClassName.substring(sourceClassName.lastIndexOf(".") + 1);
            }
            
            String threadName = Thread.currentThread().getName();
            
            return String.format(FORMAT,
                    new Date(record.getMillis()),
                    record.getLevel().getName(),
                    threadName,
                    formatMessage(record) + (record.getThrown() != null ? "\n" + getStackTrace(record.getThrown()) : ""));
        }
        
        private String getStackTrace(Throwable throwable) {
            StringBuilder sb = new StringBuilder();
            sb.append(throwable.toString()).append("\n");
            
            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            
            Throwable cause = throwable.getCause();
            if (cause != null) {
                sb.append("Caused by: ").append(getStackTrace(cause));
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Sets the global logging level.
     * 
     * @param level The logging level to set
     */
    public static void setLogLevel(Level level) {
        LOGGER.setLevel(level);
        
        for (Handler handler : LOGGER.getHandlers()) {
            handler.setLevel(level);
        }
    }
}
