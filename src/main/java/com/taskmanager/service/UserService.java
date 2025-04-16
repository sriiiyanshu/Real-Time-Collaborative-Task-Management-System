package com.taskmanager.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.util.ArrayList;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TeamDAO;
import com.taskmanager.model.User;
import com.taskmanager.model.Activity;
import com.taskmanager.exception.DAOException;
import com.taskmanager.util.AuthUtil;
import com.taskmanager.util.ValidationUtil;

/**
 * Service class for user-related business logic
 */
public class UserService {
    
    private UserDAO userDAO;
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private TeamDAO teamDAO;
    private AuthUtil authUtil;
    private ValidationUtil validationUtil;
    private EmailService emailService;
    
    public UserService() {
        userDAO = new UserDAO();
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        teamDAO = new TeamDAO();
        authUtil = new AuthUtil();
        validationUtil = new ValidationUtil();
        emailService = new EmailService();
    }
    
    /**
     * Register a new user
     * @return User object or null if registration failed
     */
    public User registerUser(String username, String email, String password, String fullName) throws Exception {
        // Validate input
        if (!validationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username format");
        }
        
        if (!validationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (!validationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password format");
        }
        
        // Check if username or email already exists
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(authUtil.hashPassword(password)); // Hash the password
        user.setRegistrationDate(new Date());
        user.setActive(true);
        user.setRole("user"); // Default role
        
        // Save user to database
        Integer userId = userDAO.insert(user);
        if (userId != null) {
            user.setId(userId);
            
            // Send welcome email
            emailService.sendWelcomeEmail(email, fullName);
            
            return user;
        }
        
        return null;
    }
    
    /**
     * Authenticate a user
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        
        if (user != null && authUtil.verifyPassword(password, user.getPassword())) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Get a user by ID
     */
    public User getUserById(Integer userId) throws SQLException {
        return userDAO.findById(userId);
    }
    
    /**
     * Get a user by username
     */
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
    
    /**
     * Get a user by email
     */
    public User getUserByEmail(String email) throws SQLException {
        return userDAO.findByEmail(email);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
    
    /**
     * Update user profile
     */
    public boolean updateUserProfile(User user) throws SQLException {
        // Verify email isn't already used by another user
        User existingUser = userDAO.findByEmail(user.getEmail());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Email already in use by another account");
        }
        
        return userDAO.update(user);
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(Integer userId, String currentPassword, String newPassword) throws SQLException {
        User user = userDAO.findById(userId);
        
        if (user != null && authUtil.verifyPassword(currentPassword, user.getPassword())) {
            if (!validationUtil.isValidPassword(newPassword)) {
                throw new IllegalArgumentException("New password does not meet requirements");
            }
            
            user.setPassword(authUtil.hashPassword(newPassword));
            return userDAO.update(user);
        }
        
        return false;
    }
    
    /**
     * Initiate password reset
     */
    public boolean requestPasswordReset(String email, String resetUrl) throws SQLException {
        User user = userDAO.findByEmail(email);
        
        if (user != null) {
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(new Date(System.currentTimeMillis() + 24*60*60*1000)); // 24 hours
            userDAO.update(user);
            
            // Send reset email with link
            String resetLink = resetUrl + "?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Complete password reset
     */
    public boolean resetPassword(String token, String newPassword) throws SQLException {
        User user = userDAO.findByResetToken(token);
        
        if (user != null && authUtil.isValidResetToken(token)) {
            if (!validationUtil.isValidPassword(newPassword)) {
                throw new IllegalArgumentException("New password does not meet requirements");
            }
            
            // Update password and clear reset token
            user.setPassword(authUtil.hashPassword(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            return userDAO.update(user);
        }
        
        return false;
    }
    
    /**
     * Delete a user
     */
    public boolean deleteUser(Integer userId) throws SQLException {
        return userDAO.delete(userId);
    }
    
    /**
     * Count total users
     */
    public int countUsers() throws SQLException {
        return userDAO.countUsers();
    }
    
    /**
     * Get user activity history
     */
    public List<Activity> getUserActivity(int userId) throws DAOException {
        try {
            return userDAO.getUserActivities(userId, 10); // Limit to 10 recent activities
        } catch (SQLException e) {
            throw new DAOException("Error retrieving user activities", e);
        }
    }
    
    /**
     * Count tasks associated with a user
     */
    public int countUserTasks(int userId) throws DAOException {
        try {
            return taskDAO.countByUserId(userId);
        } catch (SQLException e) {
            throw new DAOException("Error counting user tasks", e);
        }
    }
    
    /**
     * Count projects associated with a user
     */
    public int countUserProjects(int userId) throws DAOException {
        try {
            return projectDAO.countByUserId(userId);
        } catch (SQLException e) {
            throw new DAOException("Error counting user projects", e);
        }
    }
    
    /**
     * Count teams associated with a user
     */
    public int countUserTeams(int userId) throws DAOException {
        try {
            return teamDAO.countByUserId(userId);
        } catch (SQLException e) {
            throw new DAOException("Error counting user teams", e);
        }
    }
    
    /**
     * Update user with all profile fields
     */
    public boolean updateUser(User user) throws DAOException {
        try {
            return userDAO.update(user);
        } catch (SQLException e) {
            throw new DAOException("Error updating user", e);
        }
    }
}