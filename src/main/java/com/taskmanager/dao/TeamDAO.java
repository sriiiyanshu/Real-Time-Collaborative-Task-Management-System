package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.taskmanager.model.Team;
import com.taskmanager.model.User;
import com.taskmanager.model.Project;

/**
 * Data Access Object for Team entities
 */
public class TeamDAO extends BaseDAO {
    
    /**
     * Insert a new team into the database
     */
    public Integer insert(Team team) throws SQLException {
        String sql = "INSERT INTO teams (name, description, creation_date, creator_id) " +
                     "VALUES (?, ?, ?, ?)";
        
        return executeInsert(sql, 
                team.getName(),
                team.getDescription(),
                new Timestamp(team.getCreationDate().getTime()),
                team.getCreatorId());
    }
    
    /**
     * Update an existing team
     */
    public boolean update(Team team) throws SQLException {
        String sql = "UPDATE teams SET name = ?, description = ? WHERE team_id = ?";
        
        int rowsUpdated = executeUpdate(sql,
                team.getName(),
                team.getDescription(),
                team.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Find a team by ID
     */
    public Team findById(Integer teamId) throws SQLException {
        String sql = "SELECT * FROM teams WHERE team_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToTeam(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all teams
     */
    public List<Team> findAll() throws SQLException {
        String sql = "SELECT * FROM teams ORDER BY name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                teams.add(mapRowToTeam(rs));
            }
            return teams;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get teams created by a user
     */
    public List<Team> findByCreatorId(Integer creatorId) throws SQLException {
        String sql = "SELECT * FROM teams WHERE creator_id = ? ORDER BY creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, creatorId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                teams.add(mapRowToTeam(rs));
            }
            return teams;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get teams where user is a member
     */
    public List<Team> findByMemberId(Integer userId) throws SQLException {
        String sql = "SELECT t.* FROM teams t " +
                     "JOIN team_members tm ON t.team_id = tm.team_id " +
                     "WHERE tm.user_id = ? " +
                     "ORDER BY t.name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                teams.add(mapRowToTeam(rs));
            }
            return teams;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Delete a team
     */
    public boolean delete(Integer teamId) throws SQLException {
        // First delete team members
        String deleteMembers = "DELETE FROM team_members WHERE team_id = ?";
        executeUpdate(deleteMembers, teamId);
        
        // Then delete the team
        String deleteTeam = "DELETE FROM teams WHERE team_id = ?";
        int rowsDeleted = executeUpdate(deleteTeam, teamId);
        
        return rowsDeleted > 0;
    }
    
    /**
     * Add a member to a team
     */
    public boolean addTeamMember(Integer teamId, Integer userId, String role) throws SQLException {
        String sql = "INSERT INTO team_members (team_id, user_id, role, joined_date) VALUES (?, ?, ?, ?)";
        
        int rowsInserted = executeUpdate(sql, 
                teamId, 
                userId, 
                role, 
                new Timestamp(System.currentTimeMillis()));
        
        return rowsInserted > 0;
    }
    
    /**
     * Remove a member from a team
     */
    public boolean removeTeamMember(Integer teamId, Integer userId) throws SQLException {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        
        int rowsDeleted = executeUpdate(sql, teamId, userId);
        return rowsDeleted > 0;
    }
    
    /**
     * Update a team member's role
     */
    public boolean updateTeamMemberRole(Integer teamId, Integer userId, String newRole) throws SQLException {
        String sql = "UPDATE team_members SET role = ? WHERE team_id = ? AND user_id = ?";
        
        int rowsUpdated = executeUpdate(sql, newRole, teamId, userId);
        return rowsUpdated > 0;
    }
    
    /**
     * Get team members
     */
    public List<User> findTeamMembers(Integer teamId) throws SQLException {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN team_members tm ON u.user_id = tm.user_id " +
                     "WHERE tm.team_id = ? " +
                     "ORDER BY u.full_name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> members = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setActive(rs.getBoolean("is_active"));
                
                members.add(user);
            }
            return members;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Check if user is a member of a team
     */
    public boolean isTeamMember(Integer teamId, Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get user's role in a team
     */
    public String getUserTeamRole(Integer teamId, Integer userId) throws SQLException {
        String sql = "SELECT role FROM team_members WHERE team_id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Search teams by name or description
     */
    public List<Team> searchTeams(String query) throws SQLException {
        String sql = "SELECT * FROM teams WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        String searchPattern = "%" + query + "%";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                teams.add(mapRowToTeam(rs));
            }
            return teams;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count team members
     */
    public int countTeamMembers(Integer teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ?";
        return executeCountQuery(sql, teamId);
    }
    
    /**
     * Find projects associated with a team
     * 
     * @param teamId The ID of the team
     * @return List of projects associated with the team
     * @throws SQLException if a database error occurs
     */
    public List<Project> findTeamProjects(Integer teamId) throws SQLException {
        String sql = "SELECT p.* FROM projects p " +
                     "JOIN team_projects tp ON p.project_id = tp.project_id " +
                     "WHERE tp.team_id = ? " +
                     "ORDER BY p.name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getInt("project_id"));
                project.setName(rs.getString("name"));
                project.setDescription(rs.getString("description"));
                
                Timestamp creationDate = rs.getTimestamp("creation_date");
                if (creationDate != null) {
                    project.setCreationDate(new Date(creationDate.getTime()));
                }
                
                Timestamp dueDate = rs.getTimestamp("due_date");
                if (dueDate != null) {
                    project.setDueDate(new Date(dueDate.getTime()));
                }
                
                project.setOwnerId(rs.getInt("owner_id"));
                project.setStatus(rs.getString("status"));
                
                projects.add(project);
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Add a project to a team
     * 
     * @param teamId The ID of the team
     * @param projectId The ID of the project to add
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean addTeamProject(Integer teamId, Integer projectId) throws SQLException {
        String sql = "INSERT INTO team_projects (team_id, project_id, added_date) VALUES (?, ?, ?)";
        
        int rowsInserted = executeUpdate(sql, 
                teamId, 
                projectId, 
                new Timestamp(System.currentTimeMillis()));
        
        return rowsInserted > 0;
    }
    
    /**
     * Remove a project from a team
     * 
     * @param teamId The ID of the team
     * @param projectId The ID of the project to remove
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean removeTeamProject(Integer teamId, Integer projectId) throws SQLException {
        String sql = "DELETE FROM team_projects WHERE team_id = ? AND project_id = ?";
        
        int rowsDeleted = executeUpdate(sql, teamId, projectId);
        return rowsDeleted > 0;
    }
    
    /**
     * Map a database row to a Team object
     */
    private Team mapRowToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getInt("team_id"));
        team.setName(rs.getString("name"));
        team.setDescription(rs.getString("description"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            team.setCreationDate(new Date(creationDate.getTime()));
        }
        
        team.setCreatorId(rs.getInt("creator_id"));
        
        return team;
    }
}