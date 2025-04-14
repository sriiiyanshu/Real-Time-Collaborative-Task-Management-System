package com.taskmanager.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception is used when a task, project, user, file or other resource
 * cannot be located in the system.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Resource types that can be not found
     */
    public static final String RESOURCE_PROJECT = "project";
    public static final String RESOURCE_TASK = "task";
    public static final String RESOURCE_USER = "user";
    public static final String RESOURCE_FILE = "file";
    public static final String RESOURCE_COMMENT = "comment";
    public static final String RESOURCE_TEAM = "team";
    public static final String RESOURCE_NOTIFICATION = "notification";
    
    private String resourceType;
    private String resourceId;
    
    /**
     * Constructs a new resource not found exception with null as its detail message.
     */
    public ResourceNotFoundException() {
        super();
    }
    
    /**
     * Constructs a new resource not found exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new resource not found exception with the specified resource type and ID.
     * 
     * @param resourceType the type of resource that was not found
     * @param resourceId the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("Resource not found: " + resourceType + " with ID " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Constructs a new resource not found exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new resource not found exception with the specified cause.
     * 
     * @param cause the cause
     */
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Gets the type of resource that was not found.
     * 
     * @return the resource type
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Sets the type of resource that was not found.
     * 
     * @param resourceType the resource type to set
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    /**
     * Gets the ID of the resource that was not found.
     * 
     * @return the resource ID
     */
    public String getResourceId() {
        return resourceId;
    }
    
    /**
     * Sets the ID of the resource that was not found.
     * 
     * @param resourceId the resource ID to set
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}