package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * File model class representing an uploaded file in the system
 */
public class File implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String filename;
    private String storedFilename; // Added storedFilename field
    private String filePath;
    private long fileSize;
    private String fileType;
    private Date uploadDate;
    private Integer uploaderId;
    private Integer taskId;
    private Integer projectId;
    
    // Constructors
    
    public File() {
        // Default constructor
    }
    
    public File(String filename, String filePath, long fileSize, String fileType, Integer uploaderId) {
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploaderId = uploaderId;
        this.uploadDate = new Date();
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getStoredFilename() {
        return storedFilename;
    }
    
    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Date getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public Integer getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public Integer getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
    
    public Integer getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
    
    /**
     * Alias for setUploaderId for backward compatibility
     */
    public void setUserId(Integer userId) {
        this.uploaderId = userId;
    }
    
    /**
     * Alias for getUploaderId for backward compatibility
     */
    public Integer getUserId() {
        return uploaderId;
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedSize() {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double size = fileSize;
        
        while (size > 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * Get file extension
     */
    public String getExtension() {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    @Override
    public String toString() {
        return "File [id=" + id + ", filename=" + filename + ", fileSize=" + getFormattedSize() + 
               ", uploadDate=" + uploadDate + "]";
    }
}