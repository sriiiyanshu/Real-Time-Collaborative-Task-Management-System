package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Notification model class representing user notifications
 */
public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String message;
    private String type;
    private Date creationDate;
    private boolean isRead;
    private Integer userId;
    private String link;
    private Integer relatedId;
    
    // Constructors
    
    public Notification() {
        // Default constructor
    }
    
    public Notification(String message, String type, Integer userId) {
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.creationDate = new Date();
        this.isRead = false;
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public Integer getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Check if notification is recent (less than 24 hours old)
     */
    public boolean isRecent() {
        long millisPerDay = 24 * 60 * 60 * 1000;
        return (new Date().getTime() - creationDate.getTime()) < millisPerDay;
    }
    
    @Override
    public String toString() {
        return "Notification [id=" + id + ", type=" + type + ", userId=" + userId + 
               ", isRead=" + isRead + "]";
    }
}