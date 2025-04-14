package com.taskmanager.listener;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Session lifecycle listener
 * Monitors session creation and destruction
 */
@WebListener
public class SessionListener implements HttpSessionListener {
    
    // Store active sessions (can be used for user activity monitoring)
    private static final ConcurrentHashMap<String, HttpSession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Called when a session is created
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        
        // Set session timeout from configuration
        int timeoutInMinutes = 30; // Default timeout
        try {
            timeoutInMinutes = com.taskmanager.config.AppConfig.getSessionTimeout();
        } catch (Exception e) {
            System.err.println("Error getting session timeout: " + e.getMessage());
        }
        session.setMaxInactiveInterval(timeoutInMinutes * 60); // Convert to seconds
        
        // Log session creation
        System.out.println("Session created: " + session.getId() + " at " + new Date());
        
        // Add to active sessions map
        activeSessions.put(session.getId(), session);
    }
    
    /**
     * Called when a session is invalidated or times out
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        
        // Get user information for logging
        String username = (String) session.getAttribute("username");
        username = (username != null) ? username : "anonymous";
        
        // Log session destruction
        System.out.println("Session destroyed: " + session.getId() + 
                " for user " + username + " at " + new Date());
        
        // Remove from active sessions map
        activeSessions.remove(session.getId());
    }
    
    /**
     * Get count of active sessions
     */
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Get all active sessions
     */
    public static ConcurrentHashMap<String, HttpSession> getActiveSessions() {
        return activeSessions;
    }
}