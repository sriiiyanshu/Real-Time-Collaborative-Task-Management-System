package com.taskmanager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Task model class representing a task within a project
 */
public class Task implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String title;
    private String description;
    private Date creationDate;
    private Date dueDate;
    private Date completionDate;
    private Integer projectId;
    private Integer assigneeId;
    private Integer creatorId;
    private String priority;
    private String status;
    private List<String> tags;
    private Double estimatedHours;
    private List<Task> subtasks;
    private Double loggedHours;
    private Project project;
    private Integer completedSubtasks;
    
    // Constructors
    
    public Task() {
        // Default constructor
        this.tags = new ArrayList<>();
        this.subtasks = new ArrayList<>();
        this.estimatedHours = 0.0;
        this.loggedHours = 0.0;
        this.completedSubtasks = 0;
    }
    
    public Task(String title, String description, Integer projectId, Integer creatorId) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.creatorId = creatorId;
        this.creationDate = new Date();
        this.priority = "Medium";
        this.status = "To Do";
        this.tags = new ArrayList<>();
        this.subtasks = new ArrayList<>();
        this.estimatedHours = 0.0;
        this.loggedHours = 0.0;
        this.completedSubtasks = 0;
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public Date getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
    
    public Integer getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
    
    public Integer getAssigneeId() {
        return assigneeId;
    }
    
    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public Integer getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        
        // Set completion date automatically when status changes to Completed
        if ("Completed".equalsIgnoreCase(status) && this.completionDate == null) {
            this.completionDate = new Date();
        } else if (!"Completed".equalsIgnoreCase(status)) {
            this.completionDate = null;
        }
    }
    
    /**
     * Get the list of tags for this task
     * @return List of tags
     */
    public List<String> getTags() {
        return tags;
    }
    
    /**
     * Set the list of tags for this task
     * @param tags List of tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    /**
     * Add a tag to this task
     * @param tag Tag to add
     * @return true if the tag was added, false if it already existed
     */
    public boolean addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            return tags.add(tag.trim());
        }
        return false;
    }
    
    /**
     * Remove a tag from this task
     * @param tag Tag to remove
     * @return true if the tag was removed, false if it didn't exist
     */
    public boolean removeTag(String tag) {
        return tags.remove(tag);
    }
    
    /**
     * Check if this task has a specific tag
     * @param tag Tag to check for
     * @return true if the task has the tag, false otherwise
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
    
    /**
     * Check if task is completed
     */
    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }
    
    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || "Completed".equalsIgnoreCase(status)) {
            return false;
        }
        return dueDate.before(new Date());
    }
    
    /**
     * Get priority level as integer (for sorting)
     */
    public int getPriorityLevel() {
        if ("High".equalsIgnoreCase(priority)) {
            return 3;
        } else if ("Medium".equalsIgnoreCase(priority)) {
            return 2;
        } else if ("Low".equalsIgnoreCase(priority)) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Get the estimated hours for this task
     * @return Estimated hours
     */
    public Double getEstimatedHours() {
        return estimatedHours;
    }
    
    /**
     * Set the estimated hours for this task
     * @param estimatedHours Estimated hours
     */
    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
    
    public List<Task> getSubtasks() {
        return subtasks != null ? subtasks : new ArrayList<>();
    }
    
    public void setSubtasks(List<Task> subtasks) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
        
        // Update completed subtasks count
        this.completedSubtasks = 0;
        if (this.subtasks != null) {
            for (Task subtask : this.subtasks) {
                if (subtask.isCompleted()) {
                    this.completedSubtasks++;
                }
            }
        }
    }
    
    public void addSubtask(Task subtask) {
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }
        subtasks.add(subtask);
        if (subtask.isCompleted()) {
            completedSubtasks++;
        }
    }
    
    public Double getLoggedHours() {
        return loggedHours != null ? loggedHours : 0.0;
    }
    
    public void setLoggedHours(Double loggedHours) {
        this.loggedHours = loggedHours != null ? loggedHours : 0.0;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public Integer getCompletedSubtasks() {
        return completedSubtasks != null ? completedSubtasks : 0;
    }
    
    public void setCompletedSubtasks(Integer completedSubtasks) {
        this.completedSubtasks = completedSubtasks != null ? completedSubtasks : 0;
    }
    
    @Override
    public String toString() {
        return "Task [id=" + id + ", title=" + title + ", projectId=" + projectId + 
               ", status=" + status + ", priority=" + priority + 
               ", tags=" + (tags != null ? tags : "[]") + "]";
    }
}