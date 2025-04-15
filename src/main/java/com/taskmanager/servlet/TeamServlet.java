package com.taskmanager.servlet;

import com.taskmanager.model.Team;
import com.taskmanager.model.User;
import com.taskmanager.service.TeamService;
import com.taskmanager.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Servlet responsible for handling team-related operations.
 * Provides endpoints for creating, retrieving, updating, and deleting teams,
 * as well as managing team memberships.
 */
@WebServlet("/api/teams/*")
public class TeamServlet extends HttpServlet {
    
    private TeamService teamService;
    private JsonUtil jsonUtil;
    
    @Override
    public void init() {
        teamService = new TeamService();
        jsonUtil = new JsonUtil();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List all teams for user
                List<Team> teams = teamService.getUserTeams(currentUser.getId());
                JSONArray teamsArray = new JSONArray();
                
                for (Team team : teams) {
                    teamsArray.put(convertTeamToJson(team, false));
                }
                
                out.print(teamsArray.toString());
            } else if (pathInfo.matches("/\\d+")) {
                // Get specific team
                Integer teamId = Integer.parseInt(pathInfo.substring(1));
                Team team = teamService.getTeamById(teamId);
                
                if (team == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Team not found");
                    return;
                }
                
                // Check if user is a member of the team
                if (!teamService.isTeamMember(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this team");
                    return;
                }
                
                // Include members in response
                out.print(convertTeamToJson(team, true).toString());
            } else if (pathInfo.matches("/\\d+/members")) {
                // Get team members
                Integer teamId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/members")));
                
                // Check if user is a member of the team
                if (!teamService.isTeamMember(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this team");
                    return;
                }
                
                JSONArray membersArray = teamService.getTeamMembersAsJson(teamId);
                out.print(membersArray.toString());
            } else if (pathInfo.matches("/\\d+/projects")) {
                // Get team projects
                Integer teamId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/projects")));
                
                // Check if user is a member of the team
                if (!teamService.isTeamMember(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied to this team");
                    return;
                }
                
                JSONArray projectsArray = teamService.getTeamProjects(teamId);
                out.print(projectsArray.toString());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
        
        // Read request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            JSONObject requestData = new JSONObject(sb.toString());
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Create new team
                String name = requestData.getString("name");
                String description = requestData.optString("description", null);
                
                Team newTeam = teamService.createTeam(name, description, currentUser.getId());
                
                if (newTeam != null) {
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(convertTeamToJson(newTeam, false).toString());
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create team");
                }
            } else if (pathInfo.matches("/\\d+/members")) {
                // Add member to team
                Integer teamId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/members")));
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can add members");
                    return;
                }
                
                Integer userId = requestData.getInt("userId");
                String role = requestData.optString("role", "member");
                
                boolean added = teamService.addTeamMember(teamId, userId, role);
                
                if (added) {
                    JSONObject result = new JSONObject();
                    result.put("success", true);
                    result.put("message", "User added to team successfully");
                    
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(result.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add user to team");
                }
            } else if (pathInfo.matches("/\\d+/projects")) {
                // Link project to team
                Integer teamId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/projects")));
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can link projects");
                    return;
                }
                
                Integer projectId = requestData.getInt("projectId");
                boolean linked = teamService.linkProjectToTeam(teamId, projectId);
                
                if (linked) {
                    JSONObject result = new JSONObject();
                    result.put("success", true);
                    result.put("message", "Project linked to team successfully");
                    
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(result.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to link project to team");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error processing request: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Team ID is required");
            return;
        }
        
        // Read request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        try {
            // Extract team ID from path
            if (pathInfo.matches("/\\d+")) {
                Integer teamId = Integer.parseInt(pathInfo.substring(1));
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can update team information");
                    return;
                }
                
                JSONObject requestData = new JSONObject(sb.toString());
                String name = requestData.optString("name");
                String description = requestData.optString("description");
                
                Team teamToUpdate = teamService.getTeamById(teamId);
                if (teamToUpdate == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Team not found");
                    return;
                }
                
                if (!name.isEmpty()) {
                    teamToUpdate.setName(name);
                }
                
                if (requestData.has("description")) {
                    teamToUpdate.setDescription(description);
                }
                
                boolean updated = teamService.updateTeam(teamToUpdate);
                
                if (updated) {
                    Team updatedTeam = teamService.getTeamById(teamId);
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(convertTeamToJson(updatedTeam, false).toString());
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update team");
                }
            } else if (pathInfo.matches("/\\d+/members/\\d+")) {
                // Update team member role
                String[] parts = pathInfo.split("/");
                Integer teamId = Integer.parseInt(parts[1]);
                Integer memberId = Integer.parseInt(parts[3]);
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can update member roles");
                    return;
                }
                
                JSONObject requestData = new JSONObject(sb.toString());
                String role = requestData.getString("role");
                
                boolean updated = teamService.updateTeamMemberRole(teamId, memberId, role);
                
                if (updated) {
                    JSONObject result = new JSONObject();
                    result.put("success", true);
                    result.put("message", "Member role updated successfully");
                    
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(result.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update member role");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error updating team: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Team ID is required");
            return;
        }
        
        try {
            if (pathInfo.matches("/\\d+")) {
                // Delete entire team
                Integer teamId = Integer.parseInt(pathInfo.substring(1));
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can delete teams");
                    return;
                }
                
                boolean deleted = teamService.deleteTeam(teamId);
                
                if (deleted) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete team");
                }
            } else if (pathInfo.matches("/\\d+/members/\\d+")) {
                // Remove member from team
                String[] parts = pathInfo.split("/");
                Integer teamId = Integer.parseInt(parts[1]);
                Integer memberId = Integer.parseInt(parts[3]);
                
                // Check if user is team admin or the member themselves
                if (!teamService.isTeamAdmin(teamId, currentUser.getId()) && currentUser.getId() != memberId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators or the member themselves can remove members");
                    return;
                }
                
                boolean removed = teamService.removeTeamMember(teamId, memberId);
                
                if (removed) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to remove member from team");
                }
            } else if (pathInfo.matches("/\\d+/projects/\\d+")) {
                // Unlink project from team
                String[] parts = pathInfo.split("/");
                Integer teamId = Integer.parseInt(parts[1]);
                Integer projectId = Integer.parseInt(parts[3]);
                
                // Check if user is team admin
                if (!teamService.isTeamAdmin(teamId, currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only team administrators can unlink projects");
                    return;
                }
                
                boolean unlinked = teamService.unlinkProjectFromTeam(teamId, projectId);
                
                if (unlinked) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to unlink project from team");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to convert Team object to JSON
     */
    private JSONObject convertTeamToJson(Team team, boolean includeMembers) {
        JSONObject json = new JSONObject();
        
        json.put("id", team.getId());
        json.put("name", team.getName());
        if (team.getDescription() != null) {
            json.put("description", team.getDescription());
        }
        json.put("createdBy", team.getCreatorId());
        json.put("createdDate", team.getCreationDate().getTime());
        
        if (includeMembers) {
            try {
                json.put("members", teamService.getTeamMembersAsJson(team.getId()));
            } catch (Exception e) {
                json.put("members", new JSONArray());
            }
        }
        
        return json;
    }
}