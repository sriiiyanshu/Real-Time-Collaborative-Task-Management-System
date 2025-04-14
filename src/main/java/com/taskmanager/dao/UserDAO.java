package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.taskmanager.model.User;

/**
 * Data Access Object for User entities
 */
public class UserDAO extends BaseDAO {
    
    /**
     * Insert a new user into the database
     */
    public Integer insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, full_name, registration_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        return executeInsert(sql, 
                user.getUsername(), 
                user.getEmail(), 
                user.getPassword(),
                user.getFullName(),
                new Timestamp(user.getRegistrationDate().getTime()),
                user.isActive());
    }
    
    /**
     * Update an existing user
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, full_name = ?, " +
                     "is_active = ?, reset_token = ?, reset_token_expiry = ? WHERE user_id = ?";
        
        Timestamp resetExpiry = user.getResetTokenExpiry() != null ? 
                new Timestamp(user.getResetTokenExpiry().getTime()) : null;
        
        int rowsUpdated = executeUpdate(sql, 
                user.getUsername(), 
                user.getEmail(), 
                user.getPassword(),
                user.getFullName(),
                user.isActive(),
                user.getResetToken(),
                resetExpiry,
                user.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Find a user by ID
     */
    public User findById(Integer userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find a user by username
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find a user by email
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find a user by reset token
     */
    public User findByResetToken(String resetToken) throws SQLException {
        String sql = "SELECT * FROM users WHERE reset_token = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, resetToken);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find a user by username or email (for login functionality)
     * 
     * @param nameOrEmail The username or email to search for
     * @return The matching user, or null if not found
     * @throws SQLException if a database error occurs
     */
    public User findByNameOrEmail(String nameOrEmail) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameOrEmail);
            stmt.setString(2, nameOrEmail);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find an active user by username or email (for login functionality)
     * Only returns users where is_active = true
     * 
     * @param nameOrEmail The username or email to search for
     * @return The matching active user, or null if not found
     * @throws SQLException if a database error occurs
     */
    public User findActiveByNameOrEmail(String nameOrEmail) throws SQLException {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND is_active = true";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameOrEmail);
            stmt.setString(2, nameOrEmail);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all users
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY username";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
            return users;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all users associated with a specific project
     */
    public List<User> findByProjectId(Integer projectId) throws SQLException {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN project_members pm ON u.user_id = pm.user_id " +
                     "WHERE pm.project_id = ? " +
                     "ORDER BY u.username";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
            return users;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all users that belong to a specific team
     * 
     * @param teamId The ID of the team to find users for
     * @return List of users belonging to the specified team
     * @throws SQLException if a database error occurs
     */
    public List<User> findByTeamId(Integer teamId) throws SQLException {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN team_members tm ON u.user_id = tm.user_id " +
                     "WHERE tm.team_id = ? " +
                     "ORDER BY u.last_name, u.first_name";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teamId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
            return users;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Delete a user
     */
    public boolean delete(Integer userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        int rowsDeleted = executeUpdate(sql, userId);
        return rowsDeleted > 0;
    }
    
    /**
     * Count total users
     */
    public Integer countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        return executeCountQuery(sql);
    }
    
    /**
     * Map a database row to a User object
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        
        Timestamp registrationDate = rs.getTimestamp("registration_date");
        if (registrationDate != null) {
            user.setRegistrationDate(new Date(registrationDate.getTime()));
        }
        
        user.setActive(rs.getBoolean("is_active"));
        user.setResetToken(rs.getString("reset_token"));
        
        Timestamp resetExpiry = rs.getTimestamp("reset_token_expiry");
        if (resetExpiry != null) {
            user.setResetTokenExpiry(new Date(resetExpiry.getTime()));
        }
        
        return user;
    }
}