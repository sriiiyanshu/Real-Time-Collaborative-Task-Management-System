package com.taskmanager.servlet;

import com.taskmanager.service.FileService;
import com.taskmanager.model.User;
import com.taskmanager.model.File;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Servlet responsible for handling file download operations.
 * Provides endpoints for downloading files associated with tasks and projects.
 */
@WebServlet("/api/files/download/*")
public class FileDownloadServlet extends HttpServlet {
    
    private FileService fileService;
    
    @Override
    public void init() {
        fileService = new FileService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Extract file ID from path
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required");
            return;
        }
        
        long fileId;
        try {
            fileId = Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID format");
            return;
        }
        
        // Get current user from session
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }
        
        try {
            // Fetch the file using service
            File file = fileService.getFileById(Integer.valueOf((int) fileId));
            if (file == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                return;
            }
            
            // Verify user has access to this file
            if (!hasAccessToFile(currentUser.getId(), file)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
            
            // Set response headers for file download
            response.setContentType(file.getFileType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");
            response.setContentLength((int) file.getFileSize());
            
            // Stream the file content
            try (OutputStream out = response.getOutputStream()) {
                writeFileToOutput(file, out);
            }
            
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error downloading file: " + e.getMessage());
        }
    }
    
    /**
     * Check if user has access to the file
     * @param userId The user ID
     * @param file The file to check
     * @return true if the user has access, false otherwise
     */
    private boolean hasAccessToFile(Integer userId, File file) throws SQLException {
        // If user is the uploader, they have access
        if (userId.equals(file.getUploaderId())) {
            return true;
        }
        
        // If file is associated with a task, check if user is assigned to that task
        if (file.getTaskId() != null) {
            return fileService.isUserAssociatedWithTask(userId, file.getTaskId());
        }
        
        // If file is associated with a project, check if user is a member of that project
        if (file.getProjectId() != null) {
            return fileService.isUserAssociatedWithProject(userId, file.getProjectId());
        }
        
        // Default: no access
        return false;
    }
    
    /**
     * Write file content to output stream
     * @param file The file to write
     * @param out The output stream to write to
     */
    private void writeFileToOutput(File file, OutputStream out) throws IOException {
        java.nio.file.Path path = Paths.get(file.getFilePath());
        Files.copy(path, out);
    }
}