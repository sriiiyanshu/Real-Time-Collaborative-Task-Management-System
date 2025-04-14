package com.taskmanager.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Part;

import com.taskmanager.config.AppConfig;
import com.taskmanager.dao.FileDAO;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;

/**
 * Service class for file management operations
 */
public class FileService {
    
    private FileDAO fileDAO;
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    
    // Define supported file types
    private static final String[] SUPPORTED_IMAGE_TYPES = {
        "image/jpeg", "image/png", "image/gif", "image/bmp", "image/tiff", "image/webp"
    };
    
    private static final String[] SUPPORTED_DOCUMENT_TYPES = {
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // pptx
        "text/plain", "text/csv", "text/html"
    };
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB max file size
    
    public FileService() {
        fileDAO = new FileDAO();
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();
        notificationService = new NotificationService();
        
        // Initialize file storage directory
        initStorageDirectory();
    }
    
    /**
     * Initialize the file storage directory if it doesn't exist
     */
    private void initStorageDirectory() {
        String uploadDir = AppConfig.getFileStoragePath();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Upload a file and associate it with a task
     * @param part The file part from multipart request
     * @param taskId The ID of the task to associate the file with
     * @param userId The ID of the user uploading the file
     * @return The file model object or null if upload failed
     */
    public com.taskmanager.model.File uploadTaskFile(Part part, Integer taskId, Integer userId) 
            throws SQLException, IOException {
        // Validate input
        if (part == null) {
            throw new IllegalArgumentException("File part cannot be null");
        }
        
        // Check file size
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size (10MB)");
        }
        
        // Check if task exists
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task does not exist");
        }
        
        // Validate file type
        String contentType = part.getContentType();
        if (!isFileTypeSupported(contentType)) {
            throw new IllegalArgumentException("File type not supported: " + contentType);
        }
        
        // Generate unique filename
        String originalFilename = getFileName(part);
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = generateUniqueFileName(fileExtension);
        
        // Save file to disk
        String uploadDir = AppConfig.getFileStoragePath();
        String filePath = uploadDir + File.separator + uniqueFileName;
        
        try (InputStream inputStream = part.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(filePath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        // Create file record
        com.taskmanager.model.File fileModel = new com.taskmanager.model.File();
        fileModel.setTaskId(taskId);
        fileModel.setUserId(userId);
        fileModel.setFilename(originalFilename);
        fileModel.setStoredFilename(uniqueFileName);
        fileModel.setFilePath(filePath);
        fileModel.setFileType(contentType);
        fileModel.setFileSize(part.getSize());
        fileModel.setUploadDate(new Date());
        
        // Save file record to database
        Integer fileId = fileDAO.insert(fileModel);
        if (fileId != null) {
            fileModel.setId(fileId);
            
            // Notify task assignee about the file upload if they're not the uploader
            if (task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
                notificationService.notifyFileUpload(
                    userId, taskId, task.getAssigneeId(), originalFilename
                );
            }
            
            return fileModel;
        }
        
        return null;
    }
    
    /**
     * Upload a file and associate it with a project
     */
    public com.taskmanager.model.File uploadProjectFile(Part part, Integer projectId, Integer userId) 
            throws SQLException, IOException {
        // Similar to uploadTaskFile but for projects
        if (part == null) {
            throw new IllegalArgumentException("File part cannot be null");
        }
        
        // Check file size
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size (10MB)");
        }
        
        // Check if project exists
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project does not exist");
        }
        
        // Validate file type
        String contentType = part.getContentType();
        if (!isFileTypeSupported(contentType)) {
            throw new IllegalArgumentException("File type not supported: " + contentType);
        }
        
        // Generate unique filename
        String originalFilename = getFileName(part);
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = generateUniqueFileName(fileExtension);
        
        // Save file to disk
        String uploadDir = AppConfig.getFileStoragePath();
        String filePath = uploadDir + File.separator + uniqueFileName;
        
        try (InputStream inputStream = part.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(filePath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        // Create file record
        com.taskmanager.model.File fileModel = new com.taskmanager.model.File();
        fileModel.setProjectId(projectId);
        fileModel.setUserId(userId);
        fileModel.setFilename(originalFilename);
        fileModel.setStoredFilename(uniqueFileName);
        fileModel.setFilePath(filePath);
        fileModel.setFileType(contentType);
        fileModel.setFileSize(part.getSize());
        fileModel.setUploadDate(new Date());
        
        // Save file record to database
        Integer fileId = fileDAO.insert(fileModel);
        if (fileId != null) {
            fileModel.setId(fileId);
            return fileModel;
        }
        
        return null;
    }
    
    /**
     * Get a file by ID
     */
    public com.taskmanager.model.File getFileById(Integer fileId) throws SQLException {
        return fileDAO.findById(fileId);
    }
    
    /**
     * Get files associated with a task
     */
    public List<com.taskmanager.model.File> getFilesByTaskId(Integer taskId) throws SQLException {
        return fileDAO.findByTaskId(taskId);
    }
    
    /**
     * Get files associated with a project
     */
    public List<com.taskmanager.model.File> getFilesByProjectId(Integer projectId) throws SQLException {
        return fileDAO.findByProjectId(projectId);
    }
    
    /**
     * Get files uploaded by a specific user
     */
    public List<com.taskmanager.model.File> getFilesByUserId(Integer userId) throws SQLException {
        return fileDAO.findByUserId(userId);
    }
    
    /**
     * Delete a file
     */
    public boolean deleteFile(Integer fileId, Integer userId) throws SQLException, IOException {
        com.taskmanager.model.File file = fileDAO.findById(fileId);
        if (file == null) {
            return false;
        }
        
        // Check if user is authorized to delete the file
        if (!file.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this file");
        }
        
        // Delete physical file
        Path path = Paths.get(file.getFilePath());
        Files.deleteIfExists(path);
        
        // Delete database record
        return fileDAO.delete(fileId);
    }
    
    /**
     * Extract the file name from the Part
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] elements = contentDisposition.split(";");
        
        for (String element : elements) {
            if (element.trim().startsWith("filename")) {
                return element.substring(element.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        
        return "unknown";
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * Generate a unique filename
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Check if the file type is supported
     */
    private boolean isFileTypeSupported(String contentType) {
        // Check for image types
        for (String type : SUPPORTED_IMAGE_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        
        // Check for document types
        for (String type : SUPPORTED_DOCUMENT_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get total storage used by all files
     */
    public long getTotalStorageUsed() throws SQLException {
        return fileDAO.getTotalStorageSize();
    }
    
    /**
     * Get storage used by a specific user
     */
    public long getUserStorageUsed(Integer userId) throws SQLException {
        return fileDAO.getUserStorageSize(userId);
    }
    
    /**
     * Count files by type
     */
    public int countFilesByType(String fileType) throws SQLException {
        return fileDAO.countByFileType(fileType);
    }

    /**
     * Check if a user is associated with a task
     * 
     * @param userId The user ID to check
     * @param taskId The task ID to check against
     * @return true if the user is associated with the task, false otherwise
     */
    public boolean isUserAssociatedWithTask(Integer userId, Integer taskId) throws SQLException {
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            return false;
        }
        
        // User is associated if they created the task
        if (userId.equals(task.getCreatorId())) {
            return true;
        }
        
        // User is associated if they are assigned to the task
        if (userId.equals(task.getAssigneeId())) {
            return true;
        }
        
        // Check if user is part of the project that contains this task
        if (task.getProjectId() != null) {
            return isUserAssociatedWithProject(userId, task.getProjectId());
        }
        
        return false;
    }
    
    /**
     * Check if a user is associated with a project
     * 
     * @param userId The user ID to check
     * @param projectId The project ID to check against
     * @return true if the user is associated with the project, false otherwise
     */
    public boolean isUserAssociatedWithProject(Integer userId, Integer projectId) throws SQLException {
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            return false;
        }
        
        // User is associated if they created the project
        if (userId.equals(project.getOwnerId())) {
            return true;
        }
        
        // Check if user is a team member for this project
        return projectDAO.isUserTeamMember(userId, projectId);
    }
}