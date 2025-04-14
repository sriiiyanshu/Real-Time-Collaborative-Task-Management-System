package com.taskmanager.servlet;

import com.taskmanager.model.User;
import com.taskmanager.service.FileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;

/**
 * Servlet responsible for handling file upload operations.
 * Provides endpoints for uploading files associated with tasks and projects.
 */
@WebServlet("/api/files/upload/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class FileUploadServlet extends HttpServlet {
    
    private FileService fileService;
    
    @Override
    public void init() {
        fileService = new FileService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        // Determine if this is for a project or task
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid upload path");
            return;
        }
        
        try {
            // Get the file part
            Part filePart = request.getPart("file");
            
            // Get other parameters
            Integer projectId = null;
            Integer taskId = null;
            String description = request.getParameter("description");
            
            if (pathInfo.startsWith("/project/")) {
                projectId = Integer.parseInt(pathInfo.substring("/project/".length()));
            } else if (pathInfo.startsWith("/task/")) {
                taskId = Integer.parseInt(pathInfo.substring("/task/".length()));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid upload path");
                return;
            }
            
            com.taskmanager.model.File uploadedFile = null;
            if (projectId != null) {
                uploadedFile = fileService.uploadProjectFile(filePart, projectId, currentUser.getId());
            } else if (taskId != null) {
                uploadedFile = fileService.uploadTaskFile(filePart, taskId, currentUser.getId());
            }
            
            if (uploadedFile != null) {
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                JSONObject result = new JSONObject();
                result.put("id", uploadedFile.getId());
                result.put("fileName", uploadedFile.getFileName());
                result.put("fileSize", uploadedFile.getSize());
                result.put("uploadDate", uploadedFile.getUploadDate().toString());
                result.put("contentType", uploadedFile.getContentType());
                out.print(result.toString());
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File upload failed");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during file upload: " + e.getMessage());
        }
    }
}