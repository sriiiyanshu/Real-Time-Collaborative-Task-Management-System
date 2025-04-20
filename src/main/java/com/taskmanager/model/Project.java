package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Project model class representing a project in the system
 */
public class Project implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String name;
    private String description;
    private Date creationDate;
    private Date dueDate;
    private Integer ownerId;
    private String status;
    
    // Constructors
    
    public Project() {
        // Default constructor
    }
    
    public Project(Integer id, String name, String description, Integer ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = new Date();
        this.ownerId = ownerId;
        this.status = "Active";
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Check if project is completed
     */
    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }
    
    /**
     * Check if project is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || "Completed".equalsIgnoreCase(status)) {
            return false;
        }
        return dueDate.before(new Date());
    }
    
    /**
     * Get project completion percentage. This is a placeholder that returns 0 by default.
     * In a real implementation, this would calculate the percentage based on completed tasks.
     */
    public int getCompletionPercentage() {
        // Placeholder implementation
        // In a real system, this would typically calculate based on the number of
        // completed tasks vs total tasks in the project
        if (isCompleted()) {
            return 100;
        }
        return 0; // Default value when tasks are not implemented
    }
    
    @Override
    public String toString() {
        return "Project [id=" + id + ", name=" + name + ", ownerId=" + ownerId + ", status=" + status + "]";
    }
}