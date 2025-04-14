package com.taskmanager.socket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.taskmanager.model.User;
import com.taskmanager.service.NotificationService;

/**
 * WebSocket endpoint for notification functionality.
 * Enables real-time delivery of notifications to users.
 */
@ServerEndpoint(
    value = "/ws/notifications/{userId}",
    configurator = SocketConfigurator.class,
    decoders = SocketMessageDecoder.class,
    encoders = SocketMessageEncoder.class
)
public class NotificationEndpoint {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationEndpoint.class.getName());
    
    // Map of user IDs to their WebSocket sessions
    private static final Map<String, Session> userSessions = Collections.synchronizedMap(new HashMap<>());
    
    private NotificationService notificationService = new NotificationService();
    private User user;
    private String userId;
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("userId") String userId) {
        this.userId = userId;
        this.user = (User) config.getUserProperties().get("user");
        
        // Verify that the connected user has the correct ID
        if (user == null || !userId.equals(String.valueOf(user.getId()))) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, 
                                             "User authentication failed"));
                return;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing unauthorized WebSocket connection", e);
            }
            return;
        }
        
        // Add user session to the map
        userSessions.put(userId, session);
        
        // Send confirmation message
        SocketMessage confirmationMessage = new SocketMessage(
            "system", 
            "Connected to notification service.",
            "System"
        );
        
        try {
            // Send initial unread notification count
            int unreadCount = notificationService.getUnreadNotificationCount(user.getId());
            
            SocketMessage countMessage = new SocketMessage(
                "notification_count", 
                String.valueOf(unreadCount),
                "System"
            );
            
            session.getBasicRemote().sendObject(confirmationMessage);
            session.getBasicRemote().sendObject(countMessage);
        } catch (IOException | EncodeException e) {
            LOGGER.log(Level.SEVERE, "Error sending initial notification data", e);
        }
        
        LOGGER.info("User connected to notification service: " + userId);
    }
    
    @OnMessage
    public void onMessage(SocketMessage message, Session session) {
        LOGGER.info("Received notification message: " + message);
        
        // Most notification operations will be initiated from the server,
        // but we could handle client requests like "mark as read" here
        try {
            if (message.getType().equals("mark_read")) {
                // Handle marking notification as read
                Integer notificationId = message.getObjectId();
                if (notificationId != null) {
                    boolean success = notificationService.markAsRead(notificationId, Integer.parseInt(userId));
                    
                    SocketMessage responseMessage = new SocketMessage(
                        "notification_update",
                        success ? "Notification marked as read" : "Failed to update notification",
                        "System"
                    );
                    session.getBasicRemote().sendObject(responseMessage);
                    
                    // Send updated count
                    int unreadCount = notificationService.getUnreadNotificationCount(Integer.parseInt(userId));
                    SocketMessage countMessage = new SocketMessage(
                        "notification_count", 
                        String.valueOf(unreadCount),
                        "System"
                    );
                    session.getBasicRemote().sendObject(countMessage);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing notification message", e);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // Remove user from active sessions
        if (userId != null) {
            userSessions.remove(userId);
            LOGGER.info("User disconnected from notification service: " + userId + " - " + reason.getReasonPhrase());
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error in notification endpoint for user " + userId, throwable);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                                         "Server error occurred"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing WebSocket connection after error", e);
        }
    }
    
    /**
     * Send a notification to a specific user
     * This method can be called from other parts of the application
     */
    public static void sendNotification(String recipientId, String content, String type, Integer objectId) {
        Session recipientSession = userSessions.get(recipientId);
        
        if (recipientSession != null && recipientSession.isOpen()) {
            SocketMessage notificationMessage = new SocketMessage(
                type != null ? type : "notification",
                content,
                "System",
                recipientId,
                objectId
            );
            
            try {
                recipientSession.getBasicRemote().sendObject(notificationMessage);
            } catch (IOException | EncodeException e) {
                LOGGER.log(Level.SEVERE, "Error sending notification to user " + recipientId, e);
            }
        }
        // If user is not connected, the notification is already saved in the database
        // and will be shown when they log in
    }
    
    /**
     * Send updated notification count to a user
     * This can be called when new notifications are created for a user
     */
    public static void sendNotificationCount(String recipientId, int count) {
        Session recipientSession = userSessions.get(recipientId);
        
        if (recipientSession != null && recipientSession.isOpen()) {
            SocketMessage countMessage = new SocketMessage(
                "notification_count",
                String.valueOf(count),
                "System"
            );
            
            try {
                recipientSession.getBasicRemote().sendObject(countMessage);
            } catch (IOException | EncodeException e) {
                LOGGER.log(Level.SEVERE, "Error sending notification count to user " + recipientId, e);
            }
        }
    }
}