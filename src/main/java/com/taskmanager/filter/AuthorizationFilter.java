package com.taskmanager.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;

/**
 * Filter to check if user has proper permissions
 * Protects resources that require specific authorization
 */
@WebFilter(urlPatterns = {
    "/projects/edit/*", 
    "/projects/delete/*",
    "/tasks/edit/*", 
    "/tasks/delete/*",
    "/admin/*"
})
public class AuthorizationFilter implements Filter {
    
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        projectDAO = new ProjectDAO();
        taskDAO = new TaskDAO();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Assume user is already authenticated (handled by AuthenticationFilter)
        User currentUser = (User) session.getAttribute("user");
        String requestURI = httpRequest.getRequestURI();
        
        try {
            boolean isAuthorized = false;
            
            // Admin area access
            if (requestURI.startsWith(httpRequest.getContextPath() + "/admin/")) {
                isAuthorized = "admin".equals(currentUser.getRole());
            }
            // Project edit/delete access
            else if (requestURI.startsWith(httpRequest.getContextPath() + "/projects/edit/") || 
                     requestURI.startsWith(httpRequest.getContextPath() + "/projects/delete/")) {
                
                // Extract project ID from URL
                String[] urlParts = requestURI.split("/");
                Integer projectId = Integer.parseInt(urlParts[urlParts.length - 1]);
                
                // Check if user is project owner or admin
                Project project = projectDAO.findById(projectId);
                isAuthorized = project != null && (
                    project.getOwnerId().equals(currentUser.getId()) || 
                    "admin".equals(currentUser.getRole())
                );
            }
            // Task edit/delete access
            else if (requestURI.startsWith(httpRequest.getContextPath() + "/tasks/edit/") || 
                     requestURI.startsWith(httpRequest.getContextPath() + "/tasks/delete/")) {
                
                // Extract task ID from URL
                String[] urlParts = requestURI.split("/");
                Integer taskId = Integer.parseInt(urlParts[urlParts.length - 1]);
                
                // Check if user is task creator, assignee, project owner, or admin
                Task task = taskDAO.findById(taskId);
                if (task != null) {
                    Project project = projectDAO.findById(task.getProjectId());
                    isAuthorized = task.getCreatorId().equals(currentUser.getId()) || 
                                   task.getAssigneeId().equals(currentUser.getId()) || 
                                   (project != null && project.getOwnerId().equals(currentUser.getId())) || 
                                   "admin".equals(currentUser.getRole());
                }
            }
            
            if (isAuthorized) {
                // User has proper permissions, proceed with the request
                chain.doFilter(request, response);
            } else {
                // User does not have proper permissions
                httpRequest.setAttribute("errorMessage", "You do not have permission to access this resource");
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard");
            }
        } catch (Exception e) {
            System.err.println("Error in AuthorizationFilter: " + e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}