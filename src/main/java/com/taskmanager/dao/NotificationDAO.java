package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.taskmanager.model.Notification;

/**
 * Data Access Object for Notification entities
 */
public class NotificationDAO extends BaseDAO {

    /**
     * Insert a new notification
     */
    public Integer insert(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (message, type, creation_date, is_read, user_id, link, related_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return executeInsert(sql, 
                notification.getMessage(),
                notification.getType(),
                new Timestamp(notification.getCreationDate().getTime()),
                notification.isRead(),
                notification.getUserId(),
                notification.getLink(),
                notification.getRelatedId());
    }
    
    /**
     * Update an existing notification
     */
    public boolean update(Notification notification) throws SQLException {
        String sql = "UPDATE notifications SET message = ?, type = ?, is_read = ?, " +
                     "link = ?, related_id = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql,
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getLink(),
                notification.getRelatedId(),
                notification.getId());
        
        return rowsAffected > 0;
    }
    
    /**
     * Delete a notification by ID
     */
    public boolean delete(Integer notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id = ?";
        int rowsAffected = executeUpdate(sql, notificationId);
        return rowsAffected > 0;
    }
    
    /**
     * Find a notification by ID
     */
    public Notification findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToNotification(rs);
            }
            
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find notifications by user ID
     */
    public List<Notification> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Notification> notifications = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
            
            return notifications;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find recent notifications by user ID with limit
     */
    public List<Notification> findRecentByUserId(Integer userId, int limit) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY creation_date DESC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Notification> notifications = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
            
            return notifications;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find recent notifications for a specific user with Long userId
     *
     * @param userId The ID of the user (as Long)
     * @param limit Maximum number of notifications to return
     * @return List of recent notifications for the user
     * @throws SQLException if a database error occurs
     */
    public List<Notification> findRecentNotifications(Long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY creation_date DESC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Notification> notifications = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
            
            return notifications;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count unread notifications by user ID
     */
    public int countUnreadByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        return executeCountQuery(sql, userId);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public boolean markAllAsRead(Integer userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        int rowsAffected = executeUpdate(sql, userId);
        return rowsAffected > 0;
    }
    
    /**
     * Delete old notifications (older than specified days)
     */
    public int deleteOldNotifications(int daysOld) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysOld);
        
        String sql = "DELETE FROM notifications WHERE creation_date < ?";
        return executeUpdate(sql, new Timestamp(cal.getTimeInMillis()));
    }
    
    /**
     * Delete all notifications for a specific user
     */
    public boolean deleteAllByUserId(Integer userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        int rowsAffected = executeUpdate(sql, userId);
        return rowsAffected > 0;
    }
    
    /**
     * Helper method to map ResultSet to Notification object
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        notification.setCreationDate(rs.getTimestamp("creation_date"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setLink(rs.getString("link"));
        
        // Check for NULL related_id (might be null in database)
        int relatedId = rs.getInt("related_id");
        if (!rs.wasNull()) {
            notification.setRelatedId(relatedId);
        }
        
        return notification;
    }
}