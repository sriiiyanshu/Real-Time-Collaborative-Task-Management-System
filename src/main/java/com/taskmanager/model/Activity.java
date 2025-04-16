package com.taskmanager.model;

import java.util.Date;

/**
 * Represents a user activity record
 */
public class Activity {
    
    private Integer id;
    private Integer userId;
    private String type;
    private String description;
    private Date timestamp;
    private String entityType;
    private Integer entityId;
    
    // Activity types
    public static final String TASK_CREATED = "TASK_CREATED";
    public static final String TASK_COMPLETED = "TASK_COMPLETED";
    public static final String COMMENT_ADDED = "COMMENT_ADDED";
    public static final String PROJECT_CREATED = "PROJECT_CREATED";
    public static final String FILE_UPLOADED = "FILE_UPLOADED";
    
    public Activity() {
        // Default constructor
    }
    
    public Activity(Integer userId, String type, String description, String entityType, Integer entityId) {
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
    
    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                '}';
    }
}