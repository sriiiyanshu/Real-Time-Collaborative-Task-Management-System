package com.taskmanager.socket;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import com.taskmanager.model.User;

/**
 * Configurator for WebSocket endpoints.
 * Handles authentication and session management for WebSocket connections.
 */
public class SocketConfigurator extends ServerEndpointConfig.Configurator {
    
    private static final String USER_PROPERTY = "user";
    private static final String USER_ID_PROPERTY = "userId";
    private static final String SESSION_PROPERTY = "httpSession";
    
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        
        if (httpSession != null) {
            User user = (User) httpSession.getAttribute("user");
            
            if (user != null) {
                // Store user and session information in endpoint config
                config.getUserProperties().put(USER_PROPERTY, user);
                config.getUserProperties().put(USER_ID_PROPERTY, user.getId());
                config.getUserProperties().put(SESSION_PROPERTY, httpSession);
            } else {
                // User not authenticated, you might want to reject connection
                // We'll allow connection but without user info for now
                config.getUserProperties().put(USER_PROPERTY, null);
            }
        } else {
            // No HTTP session available, likely not authenticated
            config.getUserProperties().put(USER_PROPERTY, null);
        }
        
        super.modifyHandshake(config, request, response);
    }
    
    /**
     * Helper method to extract User from endpoint config
     */
    public static User getUser(ServerEndpointConfig config) {
        return (User) config.getUserProperties().get(USER_PROPERTY);
    }
    
    /**
     * Helper method to extract User ID from endpoint config
     */
    public static Integer getUserId(ServerEndpointConfig config) {
        return (Integer) config.getUserProperties().get(USER_ID_PROPERTY);
    }
    
    /**
     * Helper method to extract HTTP session from endpoint config
     */
    public static HttpSession getHttpSession(ServerEndpointConfig config) {
        return (HttpSession) config.getUserProperties().get(SESSION_PROPERTY);
    }
}