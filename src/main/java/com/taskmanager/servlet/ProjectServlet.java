package com.taskmanager.servlet;

import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.service.ProjectService;
import com.taskmanager.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class ProjectServlet
 * Handles project management functionality
 */
@WebServlet("/projects/*")
public class ProjectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProjectService projectService;
    private JsonUtil jsonUtil;
    
    public void init() {
        projectService = new ProjectService();
        jsonUtil = new JsonUtil();
    }
    
    /**
     * Handle GET requests for projects
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        // Get current user from session
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        response.setContentType("application/json");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // List all projects for the user
            List<Project> projects = projectService.getUserProjects(currentUser.getId());
            jsonUtil.writeJsonResponse(response, projects);
        } else {
            try {
                // Get specific project
                int projectId = Integer.parseInt(pathInfo.substring(1));
                Project project = projectService.getProject(projectId);
                
                if (project == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }
                
                if (!projectService.hasAccess(currentUser.getId(), projectId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this project");
                    return;
                }
                
                jsonUtil.writeJsonResponse(response, project);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
            }
        }
    }
    
    /**
     * Handle POST requests for creating new projects
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        Project project = jsonUtil.parseJsonRequest(request, Project.class);
        project.setOwnerId(currentUser.getId());
        
        Project createdProject = projectService.createProject(project);
        response.setContentType("application/json");
        jsonUtil.writeJsonResponse(response, createdProject);
    }
    
    /**
     * Handle PUT requests for updating projects
     */
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        try {
            int projectId = Integer.parseInt(pathInfo.substring(1));
            
            if (!projectService.hasAccess(currentUser.getId(), projectId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this project");
                return;
            }
            
            Project project = jsonUtil.parseJsonRequest(request, Project.class);
            project.setId(projectId);
            
            boolean updated = projectService.updateProject(project);
            
            if (updated) {
                Project updatedProject = projectService.getProject(projectId);
                response.setContentType("application/json");
                jsonUtil.writeJsonResponse(response, updatedProject);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        }
    }
    
    /**
     * Handle DELETE requests for removing projects
     */
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        try {
            int projectId = Integer.parseInt(pathInfo.substring(1));
            
            if (!projectService.isOwner(currentUser.getId(), projectId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only project owner can delete the project");
                return;
            }
            
            boolean deleted = projectService.deleteProject(projectId);
            
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        }
    }
}