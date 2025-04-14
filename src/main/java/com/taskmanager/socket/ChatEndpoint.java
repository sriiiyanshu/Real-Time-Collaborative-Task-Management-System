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
import com.taskmanager.service.UserService;

/**
 * WebSocket endpoint for chat functionality.
 * Enables real-time messaging between users.
 */
@ServerEndpoint(
    value = "/ws/chat/{userId}",
    configurator = SocketConfigurator.class,
    decoders = SocketMessageDecoder.class,
    encoders = SocketMessageEncoder.class
)
public class ChatEndpoint {
    
    private static final Logger LOGGER = Logger.getLogger(ChatEndpoint.class.getName());
    
    // Map of user IDs to their WebSocket sessions
    private static final Map<String, Session> userSessions = Collections.synchronizedMap(new HashMap<>());
    
    private UserService userService = new UserService();
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
        
        // Send welcome message
        SocketMessage welcomeMessage = new SocketMessage(
            "system", 
            "Connected to chat. There are " + userSessions.size() + " users online.",
            "System"
        );
        try {
            session.getBasicRemote().sendObject(welcomeMessage);
        } catch (IOException | EncodeException e) {
            LOGGER.log(Level.SEVERE, "Error sending welcome message", e);
        }
        
        LOGGER.info("User connected to chat: " + userId);
    }
    
    @OnMessage
    public void onMessage(SocketMessage message, Session session) {
        LOGGER.info("Received message: " + message);
        
        try {
            if (message.getType().equals("chat")) {
                // Handle chat message
                if (message.getRecipient() != null) {
                    // Direct message to specific user
                    sendDirectMessage(message);
                } else {
                    // Broadcast to all connected users
                    broadcastMessage(message);
                }
            } else {
                // Handle other types of messages if needed
                session.getBasicRemote().sendObject(
                    new SocketMessage("error", "Unsupported message type", "System")
                );
            }
        } catch (IOException | EncodeException e) {
            LOGGER.log(Level.SEVERE, "Error handling message", e);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // Remove user from active sessions
        if (userId != null) {
            userSessions.remove(userId);
            LOGGER.info("User disconnected from chat: " + userId + " - " + reason.getReasonPhrase());
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error in chat endpoint for user " + userId, throwable);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                                         "Server error occurred"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing WebSocket connection after error", e);
        }
    }
    
    /**
     * Send a direct message to a specific user
     */
    private void sendDirectMessage(SocketMessage message) throws IOException, EncodeException {
        String recipientId = message.getRecipient();
        Session recipientSession = userSessions.get(recipientId);
        
        // Check if recipient is online
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.getBasicRemote().sendObject(message);
            
            // Also send a copy to the sender
            Session senderSession = userSessions.get(message.getSender());
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.getBasicRemote().sendObject(message);
            }
        } else {
            // Recipient not online, store message for later delivery or notify sender
            Session senderSession = userSessions.get(message.getSender());
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.getBasicRemote().sendObject(
                    new SocketMessage("system", "User is offline. Message will be delivered later.", "System")
                );
            }
            
            // Save message to database for offline delivery
            // Implementation depends on your persistence layer
        }
    }
    
    /**
     * Broadcast a message to all connected users
     */
    private void broadcastMessage(SocketMessage message) {
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    LOGGER.log(Level.SEVERE, "Error broadcasting message", e);
                }
            }
        });
    }
}