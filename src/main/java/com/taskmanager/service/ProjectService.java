package com.taskmanager.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.taskmanager.config.AppConfig;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;

/**
 * Service class for project-related business logic
 */
public class ProjectService {
    
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    private EmailService emailService;
    
    public ProjectService() {
        projectDAO = new ProjectDAO();
        taskDAO = new TaskDAO();
        userDAO = new UserDAO();
        notificationService = new NotificationService();
        emailService = new EmailService();
    }
    
    /**
     * Create a new project
     * @return Project object or null if creation failed
     */
    public Project createProject(String name, String description, Integer ownerId, Date dueDate) throws SQLException {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        
        // Create project
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwnerId(ownerId);
        project.setCreationDate(new Date());
        project.setDueDate(dueDate);
        project.setStatus("Active");
        
        // Save project to database
        Integer projectId = projectDAO.insert(project);
        if (projectId != null) {
            project.setId(projectId);
            
            // Add the creator as a project member
            projectDAO.addProjectMember(projectId, ownerId, "Owner");
            
            return project;
        }
        
        return null;
    }
    
    /**
     * Get a project by ID
     */
    public Project getProjectById(Integer projectId) throws SQLException {
        return projectDAO.findById(projectId);
    }
    
    /**
     * Get all projects
     */
    public List<Project> getAllProjects() throws SQLException {
        return projectDAO.findAll();
    }
    
    /**
     * Get projects owned by a specific user
     */
    public List<Project> getProjectsByOwnerId(Integer ownerId) throws SQLException {
        return projectDAO.findByOwnerId(ownerId);
    }
    
    /**
     * Get projects where user is a team member
     */
    public List<Project> getProjectsByTeamMember(Integer userId) throws SQLException {
        return projectDAO.findByTeamMemberId(userId);
    }
    
    /**
     * Get all projects for a user (both owned projects and team member projects)
     * @param userId The user ID
     * @return List of projects associated with the user
     */
    public List<Project> getUserProjects(Integer userId) throws SQLException {
        List<Project> ownedProjects = projectDAO.findByOwnerId(userId);
        List<Project> memberProjects = projectDAO.findByTeamMemberId(userId);
        
        // Create a new combined list to avoid modifying the original lists
        List<Project> allProjects = new ArrayList<>(ownedProjects);
        
        // Add projects where user is a member but not the owner
        for (Project project : memberProjects) {
            if (!project.getOwnerId().equals(userId)) {
                allProjects.add(project);
            }
        }
        
        return allProjects;
    }
    
    /**
     * Update project details
     */
    public boolean updateProject(Project project) throws SQLException {
        return projectDAO.update(project);
    }
    
    /**
     * Delete a project
     */
    public boolean deleteProject(Integer projectId) throws SQLException {
        // In a real application, you might want to check if the project has any tasks
        // and handle them accordingly (e.g., delete them or prevent project deletion)
        return projectDAO.delete(projectId);
    }
    
    /**
     * Change project status
     */
    public boolean changeProjectStatus(Integer projectId, String newStatus) throws SQLException {
        Project project = projectDAO.findById(projectId);
        if (project != null) {
            project.setStatus(newStatus);
            return projectDAO.update(project);
        }
        return false;
    }
    
    /**
     * Add a member to a project
     */
    public boolean addProjectMember(Integer projectId, Integer userId, String role) throws SQLException {
        // Check if project and user exist
        Project project = projectDAO.findById(projectId);
        User user = userDAO.findById(userId);
        
        if (project != null && user != null) {
            boolean success = projectDAO.addProjectMember(projectId, userId, role);
            
            if (success) {
                // Notify the user about being added to the project
                String message = "You have been added to project: " + project.getName();
                notificationService.createNotification(
                        message, 
                        "project_invite", 
                        userId, 
                        "/projects/view/" + projectId);
                
                // Send email notification
                String projectLink = AppConfig.getAppBaseUrl() + "/projects/view/" + projectId;
                emailService.sendNotificationEmail(
                        user.getEmail(), 
                        user.getFullName(), 
                        "Added to Project: " + project.getName(),
                        "You have been added to the project '" + project.getName() + "'. " +
                        "You can now access project details and contribute to tasks.",
                        projectLink);
            }
            
            return success;
        }
        
        return false;
    }
    
    /**
     * Remove a member from a project
     */
    public boolean removeProjectMember(Integer projectId, Integer userId) throws SQLException {
        // Check if user is project owner
        Project project = projectDAO.findById(projectId);
        if (project != null && project.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove the project owner");
        }
        
        return projectDAO.removeProjectMember(projectId, userId);
    }
    
    /**
     * Search projects by name or description
     */
    public List<Project> searchProjects(String query) throws SQLException {
        return projectDAO.searchProjects(query);
    }
    
    /**
     * Get project statistics
     */
    public ProjectStatistics getProjectStatistics(Integer projectId) throws SQLException {
        ProjectStatistics stats = new ProjectStatistics();
        
        int totalTasks = taskDAO.countTasksByProject(projectId);
        int completedTasks = taskDAO.countCompletedTasksByProject(projectId);
        
        stats.setTotalTasks(totalTasks);
        stats.setCompletedTasks(completedTasks);
        
        if (totalTasks > 0) {
            double completionPercentage = ((double) completedTasks / totalTasks) * 100;
            stats.setCompletionPercentage(Math.round(completionPercentage));
        } else {
            stats.setCompletionPercentage(0);
        }
        
        return stats;
    }
    
    /**
     * Check if a user has access to a project
     * @param userId The ID of the user to check
     * @param projectId The ID of the project to check
     * @return true if the user has access to the project, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean hasAccess(Integer userId, int projectId) throws SQLException {
        // Check if the user is the project owner
        Project project = getProjectById(projectId);
        if (project != null && project.getOwnerId().equals(userId)) {
            return true;
        }
        
        // Check if the user is a team member
        return projectDAO.isUserTeamMember(userId, projectId);
    }
    
    /**
     * Inner class for project statistics
     */
    public static class ProjectStatistics {
        private int totalTasks;
        private int completedTasks;
        private long completionPercentage;
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public long getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(long completionPercentage) { this.completionPercentage = completionPercentage; }
        
        public int getPendingTasks() { return totalTasks - completedTasks; }
    }
}