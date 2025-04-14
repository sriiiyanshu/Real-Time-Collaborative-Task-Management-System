package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Team model class representing a group of users who collaborate together
 */
public class Team implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String name;
    private String description;
    private Date creationDate;
    private Integer creatorId;
    private String avatarPath;
    
    // Constructors
    
    public Team() {
        // Default constructor
    }
    
    public Team(String name, String description, Integer creatorId) {
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.creationDate = new Date();
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
    
    public Integer getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    
    /**
     * Get initials for display when avatar is not available
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return "TM"; // Default for "Team"
        }
        
        String[] words = name.split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        for (int i = 0; i < Math.min(2, words.length); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }
        
        // If only one word, use first two characters
        if (initials.length() == 1 && name.length() > 1) {
            initials.append(Character.toUpperCase(name.charAt(1)));
        }
        
        return initials.toString();
    }
    
    @Override
    public String toString() {
        return "Team [id=" + id + ", name=" + name + ", creatorId=" + creatorId + "]";
    }
}