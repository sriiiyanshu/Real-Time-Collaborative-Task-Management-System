package com.taskmanager.servlet;

import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.UserDAO;
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
import java.sql.SQLException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Servlet implementation class ProjectServlet
 * Handles project management functionality
 */
@WebServlet("/projects/*")
public class ProjectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProjectService projectService;
    private UserDAO userDAO;
    private JsonUtil jsonUtil;
    
    public void init() {
        projectService = new ProjectService();
        userDAO = new UserDAO();
        jsonUtil = new JsonUtil();
    }
    
    /**
     * Handle GET requests for projects
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        
        // Get current user from session
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        try {
            // Handle action parameter for form display
            if ("new".equals(action)) {
                // Show new project form
                // Add available users for team member selection
                List<User> availableUsers = userDAO.findAll();
                request.setAttribute("availableUsers", availableUsers);
                request.setAttribute("now", new Date()); // For default start date
                
                // Forward to project.jsp
                request.getRequestDispatcher("/project.jsp").forward(request, response);
                return;
            } else if ("edit".equals(action)) {
                // Get project for editing
                int projectId = Integer.parseInt(request.getParameter("id"));
                Project project = projectService.getProjectById(projectId);
                
                if (project == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }
                
                // Check if user has access to this project
                if (!projectService.hasAccess(currentUser.getId(), projectId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this project");
                    return;
                }
                
                // Add project and team members to request
                request.setAttribute("project", project);
                List<User> projectMembers = projectService.getProjectMembers(projectId);
                request.setAttribute("projectMembers", projectMembers);
                
                // Add available users for team member selection
                List<User> availableUsers = userDAO.findAll();
                request.setAttribute("availableUsers", availableUsers);
                
                // Forward to project.jsp
                request.getRequestDispatcher("/project.jsp").forward(request, response);
                return;
            } else if (request.getParameter("id") != null) {
                // Show specific project details
                int projectId = Integer.parseInt(request.getParameter("id"));
                Project project = projectService.getProjectById(projectId);
                
                if (project == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                    return;
                }
                
                // Check if user has access to this project
                if (!projectService.hasAccess(currentUser.getId(), projectId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this project");
                    return;
                }
                
                // Add project and related data to request
                request.setAttribute("project", project);
                
                // Get project members
                List<User> projectMembers = projectService.getProjectMembers(projectId);
                request.setAttribute("projectMembers", projectMembers);
                
                // Get project tasks
                request.setAttribute("projectTasks", projectService.getProjectTasks(projectId));
                
                // Get project statistics
                request.setAttribute("projectStats", projectService.getProjectStatistics(projectId));
                
                // Forward to project.jsp
                request.getRequestDispatcher("/project.jsp").forward(request, response);
                return;
            } else {
                // Default: List all user's projects
                List<Project> userProjects = projectService.getUserProjects(currentUser.getId());
                request.setAttribute("userProjects", userProjects);
                
                // Forward to project.jsp
                request.getRequestDispatcher("/project.jsp").forward(request, response);
                return;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        }
    }
    
    /**
     * Handle POST requests for creating new projects
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        // Get action parameter
        String action = request.getParameter("action");
        
        try {
            if ("edit".equals(action)) {
                // Update existing project
                int projectId = Integer.parseInt(request.getParameter("id"));
                Project project = projectService.getProjectById(projectId);
                
                if (project == null) {
                    request.setAttribute("errorMessage", "Project not found");
                    request.getRequestDispatcher("/project.jsp").forward(request, response);
                    return;
                }
                
                // Check if user is project owner
                if (!project.getOwnerId().equals(currentUser.getId())) {
                    request.setAttribute("errorMessage", "Only project owner can edit project details");
                    request.getRequestDispatcher("/project.jsp").forward(request, response);
                    return;
                }
                
                // Update project fields
                updateProjectFromRequest(project, request);
                
                // Update project in database
                boolean updated = projectService.updateProject(project);
                
                if (updated) {
                    // Handle team members update
                    String[] teamMembers = request.getParameterValues("teamMembers");
                    if (teamMembers != null) {
                        // Update team members (implementation would handle adding/removing members)
                        projectService.updateProjectMembers(projectId, teamMembers);
                    }
                    
                    // Redirect to project details
                    response.sendRedirect(request.getContextPath() + "/projects?id=" + projectId);
                } else {
                    request.setAttribute("errorMessage", "Failed to update project");
                    request.setAttribute("project", project);
                    request.getRequestDispatcher("/project.jsp").forward(request, response);
                }
            } else {
                // Default: Create new project
                // Create project object from form data
                Project project = new Project();
                project.setOwnerId(currentUser.getId());
                updateProjectFromRequest(project, request);
                
                // Create project in database
                Project createdProject = projectService.createProject(
                    project.getName(),
                    project.getDescription(),
                    project.getOwnerId(),
                    project.getDueDate()
                );
                
                if (createdProject != null) {
                    // Handle team members
                    String[] teamMembers = request.getParameterValues("teamMembers");
                    if (teamMembers != null) {
                        for (String memberId : teamMembers) {
                            try {
                                int userId = Integer.parseInt(memberId);
                                projectService.addProjectMember(createdProject.getId(), userId, "Member");
                            } catch (NumberFormatException e) {
                                // Skip invalid IDs
                            }
                        }
                    }
                    
                    // Redirect to project details
                    response.sendRedirect(request.getContextPath() + "/projects?id=" + createdProject.getId());
                } else {
                    request.setAttribute("errorMessage", "Failed to create project");
                    request.setAttribute("project", project); // Return to form with entered data
                    request.getRequestDispatcher("/project.jsp?action=new").forward(request, response);
                }
            }
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/project.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error processing request: " + e.getMessage());
            request.getRequestDispatcher("/project.jsp").forward(request, response);
        }
    }
    
    /**
     * Update project object with data from request
     */
    private void updateProjectFromRequest(Project project, HttpServletRequest request) {
        // Set basic project data
        project.setName(request.getParameter("name"));
        project.setDescription(request.getParameter("description"));
        project.setStatus(request.getParameter("status"));
        
        // Parse dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String startDateStr = request.getParameter("startDate");
            if (startDateStr != null && !startDateStr.isEmpty()) {
                project.setCreationDate(dateFormat.parse(startDateStr));
            }
            
            String endDateStr = request.getParameter("endDate");
            if (endDateStr != null && !endDateStr.isEmpty()) {
                project.setDueDate(dateFormat.parse(endDateStr));
            }
        } catch (ParseException e) {
            // Use current date if parse fails
            if (project.getCreationDate() == null) {
                project.setCreationDate(new Date());
            }
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
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        try {
            int projectId = Integer.parseInt(pathInfo.substring(1));
            Project project = projectService.getProjectById(projectId);
            
            if (project == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
                return;
            }
            
            // Check if user is the project owner
            if (!project.getOwnerId().equals(currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only project owner can delete the project");
                return;
            }
            
            boolean deleted = projectService.deleteProject(projectId);
            
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID format");
        }
    }
    
    /**
     * Helper method to check if a user is a member of a project
     */
    private boolean isUserProjectMember(Integer userId, Integer projectId) throws SQLException {
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            return false;
        }
        
        // Check if user is the owner
        if (project.getOwnerId().equals(userId)) {
            return true;
        }
        
        // Otherwise check team membership (using ProjectDAO through service)
        List<Project> memberProjects = projectService.getProjectsByTeamMember(userId);
        for (Project memberProject : memberProjects) {
            if (memberProject.getId().equals(projectId)) {
                return true;
            }
        }
        
        return false;
    }
}