package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Comment model class representing a comment on a task
 */
public class Comment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String content;
    private Date creationDate;
    private Date lastModified;
    private Integer userId;
    private Integer taskId;
    private Integer parentCommentId;
    
    // Constructors
    
    public Comment() {
        // Default constructor
    }
    
    public Comment(String content, Integer userId, Integer taskId) {
        this.content = content;
        this.userId = userId;
        this.taskId = taskId;
        this.creationDate = new Date();
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
    
    public Integer getParentCommentId() {
        return parentCommentId;
    }
    
    public void setParentCommentId(Integer parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
    
    /**
     * Check if this comment is a reply to another comment
     */
    public boolean isReply() {
        return parentCommentId != null;
    }
    
    @Override
    public String toString() {
        return "Comment [id=" + id + ", userId=" + userId + ", taskId=" + taskId + 
               ", creationDate=" + creationDate + "]";
    }
}
