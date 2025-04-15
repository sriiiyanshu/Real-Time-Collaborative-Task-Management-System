package com.taskmanager.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.taskmanager.config.AppConfig;
import com.taskmanager.dao.TeamDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.model.Team;
import com.taskmanager.model.User;
import com.taskmanager.model.Project;

/**
 * Service class for team-related business logic
 */
public class TeamService {
    
    private TeamDAO teamDAO;
    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private NotificationService notificationService;
    private EmailService emailService;
    
    public TeamService() {
        teamDAO = new TeamDAO();
        userDAO = new UserDAO();
        projectDAO = new ProjectDAO();
        notificationService = new NotificationService();
        emailService = new EmailService();
    }
    
    /**
     * Create a new team
     * @return Team object or null if creation failed
     */
    public Team createTeam(String name, String description, Integer creatorId) throws SQLException {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be empty");
        }
        
        // Create team
        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        team.setCreatorId(creatorId);
        team.setCreationDate(new Date());
        
        // Save team to database
        Integer teamId = teamDAO.insert(team);
        if (teamId != null) {
            team.setId(teamId);
            
            // Add creator as team admin
            teamDAO.addTeamMember(teamId, creatorId, "Admin");
            
            return team;
        }
        
        return null;
    }
    
    /**
     * Get a team by ID
     */
    public Team getTeamById(Integer teamId) throws SQLException {
        return teamDAO.findById(teamId);
    }
    
    /**
     * Get all teams
     */
    public List<Team> getAllTeams() throws SQLException {
        return teamDAO.findAll();
    }
    
    /**
     * Get teams created by a specific user
     */
    public List<Team> getTeamsByCreator(Integer creatorId) throws SQLException {
        return teamDAO.findByCreatorId(creatorId);
    }
    
    /**
     * Get teams where user is a member
     */
    public List<Team> getTeamsByMember(Integer userId) throws SQLException {
        return teamDAO.findByMemberId(userId);
    }
    
    /**
     * Get all teams a user belongs to (created or is a member of)
     */
    public List<Team> getUserTeams(Integer userId) throws SQLException {
        List<Team> createdTeams = getTeamsByCreator(userId);
        List<Team> memberTeams = getTeamsByMember(userId);
        
        // Combine the lists, avoiding duplicates
        List<Team> allTeams = new ArrayList<>(createdTeams);
        
        for (Team team : memberTeams) {
            boolean isDuplicate = false;
            for (Team existingTeam : allTeams) {
                if (existingTeam.getId().equals(team.getId())) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                allTeams.add(team);
            }
        }
        
        return allTeams;
    }
    
    /**
     * Update team details
     */
    public boolean updateTeam(Team team) throws SQLException {
        return teamDAO.update(team);
    }
    
    /**
     * Delete a team
     */
    public boolean deleteTeam(Integer teamId) throws SQLException {
        // Consider implications of team deletion - you might want to
        // handle team memberships, projects, etc.
        return teamDAO.delete(teamId);
    }
    
    /**
     * Add a member to a team
     */
    public boolean addTeamMember(Integer teamId, Integer userId, String role) throws SQLException {
        // Check if team and user exist
        Team team = teamDAO.findById(teamId);
        User user = userDAO.findById(userId);
        
        if (team != null && user != null) {
            boolean success = teamDAO.addTeamMember(teamId, userId, role);
            
            if (success) {
                // Notify the user about being added to the team
                String message = "You have been added to team: " + team.getName();
                notificationService.createNotification(
                        message,
                        "team_invite",
                        userId,
                        "/teams/view/" + teamId);
                
                // Send email notification
                String teamLink = AppConfig.getAppBaseUrl() + "/teams/view/" + teamId;
                emailService.sendNotificationEmail(
                        user.getEmail(),
                        user.getFullName(),
                        "Added to Team: " + team.getName(),
                        "You have been added to the team '" + team.getName() + "'.",
                        teamLink);
            }
            
            return success;
        }
        
        return false;
    }
    
    /**
     * Remove a member from a team
     */
    public boolean removeTeamMember(Integer teamId, Integer userId) throws SQLException {
        Team team = teamDAO.findById(teamId);
        
        // Check if user is team creator
        if (team != null && team.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove the team creator");
        }
        
        return teamDAO.removeTeamMember(teamId, userId);
    }
    
    /**
     * Change team member role
     */
    public boolean changeTeamMemberRole(Integer teamId, Integer userId, String newRole) throws SQLException {
        return teamDAO.updateTeamMemberRole(teamId, userId, newRole);
    }
    
    /**
     * Update team member role (alias for changeTeamMemberRole)
     */
    public boolean updateTeamMemberRole(Integer teamId, Integer userId, String newRole) throws SQLException {
        return changeTeamMemberRole(teamId, userId, newRole);
    }
    
    /**
     * Get all team members
     */
    public List<User> getTeamMembers(Integer teamId) throws SQLException {
        return teamDAO.findTeamMembers(teamId);
    }
    
    /**
     * Get all team members as JSONArray
     */
    public JSONArray getTeamMembersAsJson(Integer teamId) throws SQLException {
        List<User> members = getTeamMembers(teamId);
        JSONArray membersArray = new JSONArray();
        
        for (User member : members) {
            JSONObject memberJson = new JSONObject();
            memberJson.put("id", member.getId());
            memberJson.put("name", member.getFullName());
            memberJson.put("email", member.getEmail());
            memberJson.put("role", getUserTeamRole(teamId, member.getId()));
            membersArray.put(memberJson);
        }
        
        return membersArray;
    }
    
    /**
     * Search teams by name or description
     */
    public List<Team> searchTeams(String query) throws SQLException {
        return teamDAO.searchTeams(query);
    }
    
    /**
     * Check if user is team member
     */
    public boolean isTeamMember(Integer teamId, Integer userId) throws SQLException {
        return teamDAO.isTeamMember(teamId, userId);
    }
    
    /**
     * Check if user is team admin
     */
    public boolean isTeamAdmin(Integer teamId, Integer userId) throws SQLException {
        String role = getUserTeamRole(teamId, userId);
        return role != null && (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Administrator"));
    }
    
    /**
     * Get user role in team
     */
    public String getUserTeamRole(Integer teamId, Integer userId) throws SQLException {
        return teamDAO.getUserTeamRole(teamId, userId);
    }
    
    /**
     * Link project to team
     */
    public boolean linkProjectToTeam(Integer teamId, Integer projectId) throws SQLException {
        // Check if team and project exist
        Team team = teamDAO.findById(teamId);
        Project project = projectDAO.findById(projectId);
        
        if (team != null && project != null) {
            return teamDAO.addTeamProject(teamId, projectId);
        }
        
        return false;
    }
    
    /**
     * Unlink project from team
     */
    public boolean unlinkProjectFromTeam(Integer teamId, Integer projectId) throws SQLException {
        return teamDAO.removeTeamProject(teamId, projectId);
    }
    
    /**
     * Get team projects
     */
    public List<Project> getTeamProjectsList(Integer teamId) throws SQLException {
        return teamDAO.findTeamProjects(teamId);
    }
    
    /**
     * Get team projects as JSONArray
     */
    public JSONArray getTeamProjects(Integer teamId) throws SQLException {
        List<Project> projects = getTeamProjectsList(teamId);
        JSONArray projectsArray = new JSONArray();
        
        for (Project project : projects) {
            JSONObject projectJson = new JSONObject();
            projectJson.put("id", project.getId());
            projectJson.put("name", project.getName());
            projectJson.put("description", project.getDescription());
            projectJson.put("status", project.getStatus());
            projectJson.put("ownerId", project.getOwnerId());
            projectJson.put("creationDate", project.getCreationDate().getTime());
            if (project.getDueDate() != null) {
                projectJson.put("dueDate", project.getDueDate().getTime());
            }
            projectsArray.put(projectJson);
        }
        
        return projectsArray;
    }
}