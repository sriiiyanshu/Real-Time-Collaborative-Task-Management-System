package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.taskmanager.model.Project;
import com.taskmanager.model.User;

/**
 * Data Access Object for Project entities
 */
public class ProjectDAO extends BaseDAO {
    
    /**
     * Insert a new project into the database
     */
    public Integer insert(Project project) throws SQLException {
        String sql = "INSERT INTO projects (name, description, creation_date, due_date, owner_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Timestamp dueDate = project.getDueDate() != null ? 
                new Timestamp(project.getDueDate().getTime()) : null;
        
        return executeInsert(sql, 
                project.getName(), 
                project.getDescription(), 
                new Timestamp(project.getCreationDate().getTime()),
                dueDate,
                project.getOwnerId(),
                project.getStatus());
    }
    
    /**
     * Update an existing project
     */
    public boolean update(Project project) throws SQLException {
        String sql = "UPDATE projects SET name = ?, description = ?, due_date = ?, " +
                     "status = ? WHERE project_id = ?";
        
        Timestamp dueDate = project.getDueDate() != null ? 
                new Timestamp(project.getDueDate().getTime()) : null;
        
        int rowsUpdated = executeUpdate(sql, 
                project.getName(), 
                project.getDescription(), 
                dueDate,
                project.getStatus(),
                project.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Find a project by ID
     */
    public Project findById(Integer projectId) throws SQLException {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToProject(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all projects
     */
    public List<Project> findAll() throws SQLException {
        String sql = "SELECT * FROM projects ORDER BY creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get projects by owner ID
     */
    public List<Project> findByOwnerId(Integer ownerId) throws SQLException {
        String sql = "SELECT * FROM projects WHERE owner_id = ? ORDER BY creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ownerId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get projects by team member ID
     */
    public List<Project> findByTeamMemberId(Integer userId) throws SQLException {
        String sql = "SELECT p.* FROM projects p " +
                     "JOIN project_members pm ON p.project_id = pm.project_id " +
                     "WHERE pm.user_id = ? " +
                     "ORDER BY p.creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Delete a project
     */
    public boolean delete(Integer projectId) throws SQLException {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        
        int rowsDeleted = executeUpdate(sql, projectId);
        return rowsDeleted > 0;
    }
    
    /**
     * Add a member to a project
     */
    public boolean addProjectMember(Integer projectId, Integer userId, String role) throws SQLException {
        String sql = "INSERT INTO project_members (project_id, user_id, role) VALUES (?, ?, ?)";
        
        int rowsInserted = executeUpdate(sql, projectId, userId, role);
        return rowsInserted > 0;
    }
    
    /**
     * Remove a member from a project
     */
    public boolean removeProjectMember(Integer projectId, Integer userId) throws SQLException {
        String sql = "DELETE FROM project_members WHERE project_id = ? AND user_id = ?";
        
        int rowsDeleted = executeUpdate(sql, projectId, userId);
        return rowsDeleted > 0;
    }
    
    /**
     * Search projects by name or description
     */
    public List<Project> searchProjects(String query) throws SQLException {
        String sql = "SELECT * FROM projects WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        String searchPattern = "%" + query + "%";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count total projects
     */
    public Integer countProjects() throws SQLException {
        String sql = "SELECT COUNT(*) FROM projects";
        return executeCountQuery(sql);
    }
    
    /**
     * Get team members for a project
     * 
     * @param projectId The ID of the project
     * @return List of users who are members of the project
     * @throws SQLException if a database error occurs
     */
    public List<User> findTeamMembers(Integer projectId) throws SQLException {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN project_members pm ON u.user_id = pm.user_id " +
                     "WHERE pm.project_id = ? " +
                     "ORDER BY u.full_name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> members = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                
                members.add(user);
            }
            return members;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all projects accessible by a specific user (owned by them or projects they are members of)
     * 
     * @param userId The ID of the user
     * @return List of projects the user can access
     * @throws SQLException if a database error occurs
     */
    public List<Project> findProjectsAccessibleByUser(Integer userId) throws SQLException {
        String sql = "SELECT DISTINCT p.* FROM projects p " +
                     "LEFT JOIN project_members pm ON p.project_id = pm.project_id " +
                     "WHERE p.owner_id = ? OR pm.user_id = ? " +
                     "ORDER BY p.creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find projects with names containing the given search string
     * Optionally limited by ownerId (if not null) and max result count
     * 
     * @param searchText The text to search for in project names
     * @param ownerId Optional owner ID to filter by (can be null)
     * @param limit Maximum number of results to return
     * @return List of matching projects
     * @throws SQLException if a database error occurs
     */
    public List<Project> findProjectsWithNameContaining(String searchText, Integer ownerId, int limit) throws SQLException {
        String sql;
        if (ownerId != null) {
            sql = "SELECT * FROM projects " +
                  "WHERE name LIKE ? AND owner_id = ? " +
                  "ORDER BY name LIMIT ?";
        } else {
            sql = "SELECT * FROM projects " +
                  "WHERE name LIKE ? " +
                  "ORDER BY name LIMIT ?";
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");
            
            if (ownerId != null) {
                stmt.setInt(2, ownerId);
                stmt.setInt(3, limit);
            } else {
                stmt.setInt(2, limit);
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all projects associated with a specific team
     * 
     * @param teamId The ID of the team to find projects for
     * @return List of projects associated with the specified team
     * @throws SQLException if a database error occurs
     */
    public List<Project> findByTeamId(Integer teamId) throws SQLException {
        String sql = "SELECT p.* FROM projects p " +
                     "JOIN team_projects tp ON p.project_id = tp.project_id " +
                     "WHERE tp.team_id = ? " +
                     "ORDER BY p.creation_date DESC";
        
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
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all projects associated with a specific user
     * This includes projects owned by the user and projects the user is a member of
     * 
     * @param userId The ID of the user to find projects for
     * @return List of projects associated with the specified user
     * @throws SQLException if a database error occurs
     */
    public List<Project> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT DISTINCT p.* FROM projects p " +
                     "LEFT JOIN project_members pm ON p.project_id = pm.project_id " +
                     "WHERE p.owner_id = ? OR pm.user_id = ? " +
                     "ORDER BY p.name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Check if a user is a member of a project
     * 
     * @param userId The ID of the user to check
     * @param projectId The ID of the project to check
     * @return true if the user is a member of the project, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isUserTeamMember(Integer userId, Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM project_members WHERE project_id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
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
     * Count active projects for a specific user
     *
     * @param userId The ID of the user
     * @return Count of active projects for the user
     * @throws SQLException if a database error occurs
     */
    public int countActiveProjects(Long userId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT p.project_id) FROM projects p " +
                     "LEFT JOIN project_members pm ON p.project_id = pm.project_id " +
                     "WHERE (p.owner_id = ? OR pm.user_id = ?) AND p.status = 'In Progress'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find active projects for a specific user
     *
     * @param userId The ID of the user
     * @param limit Maximum number of projects to return
     * @return List of active projects for the user
     * @throws SQLException if a database error occurs
     */
    public List<Project> findActiveProjects(Long userId, int limit) throws SQLException {
        String sql = "SELECT DISTINCT p.* FROM projects p " +
                     "LEFT JOIN project_members pm ON p.project_id = pm.project_id " +
                     "WHERE (p.owner_id = ? OR pm.user_id = ?) AND p.status = 'In Progress' " +
                     "ORDER BY p.due_date ASC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Project> projects = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            return projects;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Map a database row to a Project object
     */
    private Project mapRowToProject(ResultSet rs) throws SQLException {
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
        
        return project;
    }
}