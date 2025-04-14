package com.taskmanager.socket;

/**
 * Represents a message exchanged through WebSockets.
 * Used for real-time communication between server and clients.
 */
public class SocketMessage {
    private String type;       // Message type (e.g., "chat", "notification", "task_update")
    private String content;    // Message content
    private String sender;     // User ID or name of the sender
    private String recipient;  // User ID or name of the recipient (optional, null for broadcasts)
    private Integer objectId;  // ID of the related object (e.g., task ID, project ID)
    private long timestamp;    // Message timestamp
    
    public SocketMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public SocketMessage(String type, String content, String sender) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }
    
    public SocketMessage(String type, String content, String sender, String recipient, Integer objectId) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.objectId = objectId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public Integer getObjectId() {
        return objectId;
    }
    
    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "SocketMessage [type=" + type + ", sender=" + sender + 
               ", recipient=" + recipient + ", objectId=" + objectId + 
               ", timestamp=" + timestamp + "]";
    }
}