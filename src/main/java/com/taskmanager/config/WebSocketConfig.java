package com.taskmanager.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Endpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Configuration and management for WebSocket connections
 */
public class WebSocketConfig {
    
    // Store active sessions by user ID
    private static final Map<Integer, Set<Session>> userSessions = new ConcurrentHashMap<>();
    
    // Store active sessions by project ID
    private static final Map<Integer, Set<Session>> projectSessions = new ConcurrentHashMap<>();
    
    /**
     * Create a ServerEndpointConfig for a WebSocket endpoint
     */
    public static ServerEndpointConfig createEndpointConfig(Class<? extends Endpoint> endpointClass, String path) {
        return ServerEndpointConfig.Builder
                .create(endpointClass, path)
                .configurator(new WebSocketConfigurator())
                .build();
    }
    
    /**
     * Register a user session
     */
    public static void addUserSession(Integer userId, Session session) {
        userSessions.computeIfAbsent(userId, k -> Collections.synchronizedSet(new HashSet<>()))
                    .add(session);
    }
    
    /**
     * Register a project session
     */
    public static void addProjectSession(Integer projectId, Session session) {
        projectSessions.computeIfAbsent(projectId, k -> Collections.synchronizedSet(new HashSet<>()))
                      .add(session);
    }
    
    /**
     * Remove a user session
     */
    public static void removeUserSession(Integer userId, Session session) {
        if (userSessions.containsKey(userId)) {
            userSessions.get(userId).remove(session);
            if (userSessions.get(userId).isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }
    
    /**
     * Remove a project session
     */
    public static void removeProjectSession(Integer projectId, Session session) {
        if (projectSessions.containsKey(projectId)) {
            projectSessions.get(projectId).remove(session);
            if (projectSessions.get(projectId).isEmpty()) {
                projectSessions.remove(projectId);
            }
        }
    }
    
    /**
     * Get all active sessions for a user
     */
    public static Set<Session> getUserSessions(Integer userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }
    
    /**
     * Get all active sessions for a project
     */
    public static Set<Session> getProjectSessions(Integer projectId) {
        return projectSessions.getOrDefault(projectId, Collections.emptySet());
    }
    
    /**
     * Send a message to all sessions in a project
     */
    public static void broadcastToProject(Integer projectId, String message) {
        Set<Session> sessions = getProjectSessions(projectId);
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                System.err.println("Error sending message to session: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send a message to all sessions of a user
     */
    public static void sendToUser(Integer userId, String message) {
        Set<Session> sessions = getUserSessions(userId);
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                System.err.println("Error sending message to user: " + e.getMessage());
            }
        }
    }
    
    /**
     * Custom configurator for WebSocket endpoints
     */
    public static class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            try {
                return endpointClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new InstantiationException("Could not instantiate websocket endpoint: " + e.getMessage());
            }
        }
    }
}