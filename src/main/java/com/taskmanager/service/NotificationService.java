package com.taskmanager.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.taskmanager.dao.NotificationDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Notification;
import com.taskmanager.model.User;
import org.json.JSONObject;

/**
 * Service class for managing user notifications
 */
public class NotificationService {
    
    private NotificationDAO notificationDAO;
    private UserDAO userDAO;
    
    public NotificationService() {
        notificationDAO = new NotificationDAO();
        userDAO = new UserDAO();
    }
    
    /**
     * Create and save a new notification
     */
    public Notification createNotification(String message, String type, Integer userId, String link) throws SQLException {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setUserId(userId);
        notification.setLink(link);
        notification.setCreationDate(new Date());
        notification.setRead(false);
        
        Integer notificationId = notificationDAO.insert(notification);
        if (notificationId != null) {
            notification.setId(notificationId);
            return notification;
        }
        
        return null;
    }
    
    /**
     * Create a notification with related entity ID
     */
    public Notification createNotificationWithRelatedId(String message, String type, Integer userId, 
                                                       String link, Integer relatedId) throws SQLException {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setUserId(userId);
        notification.setLink(link);
        notification.setRelatedId(relatedId);
        notification.setCreationDate(new Date());
        notification.setRead(false);
        
        Integer notificationId = notificationDAO.insert(notification);
        if (notificationId != null) {
            notification.setId(notificationId);
            return notification;
        }
        
        return null;
    }
    
    /**
     * Get notifications for a specific user
     */
    public List<Notification> getUserNotifications(Integer userId) throws SQLException {
        return notificationDAO.findByUserId(userId);
    }
    
    /**
     * Get unread notifications count for a user
     */
    public int getUnreadNotificationCount(Integer userId) throws SQLException {
        return notificationDAO.countUnreadByUserId(userId);
    }
    
    /**
     * Get recent notifications for a user
     */
    public List<Notification> getRecentNotifications(Integer userId, int limit) throws SQLException {
        return notificationDAO.findRecentByUserId(userId, limit);
    }
    
    /**
     * Mark a notification as read
     */
    public boolean markAsRead(Integer notificationId) throws SQLException {
        Notification notification = notificationDAO.findById(notificationId);
        if (notification != null) {
            notification.markAsRead();
            return notificationDAO.update(notification);
        }
        return false;
    }
    
    /**
     * Mark all notifications for a user as read
     */
    public boolean markAllAsRead(Integer userId) throws SQLException {
        return notificationDAO.markAllAsRead(userId);
    }
    
    /**
     * Delete a notification
     */
    public boolean deleteNotification(Integer notificationId) throws SQLException {
        return notificationDAO.delete(notificationId);
    }
    
    /**
     * Delete old notifications (e.g., older than 30 days)
     */
    public int deleteOldNotifications(int daysOld) throws SQLException {
        return notificationDAO.deleteOldNotifications(daysOld);
    }
    
    /**
     * Create task assignment notification
     */
    public Notification notifyTaskAssignment(Integer taskId, Integer assigneeId, 
                                           String taskTitle, Integer projectId) throws SQLException {
        User assignee = userDAO.findById(assigneeId);
        if (assignee != null) {
            String message = "You were assigned a new task: " + taskTitle;
            String link = "/tasks/view/" + taskId;
            
            return createNotificationWithRelatedId(message, "task_assignment", assigneeId, link, taskId);
        }
        return null;
    }
    
    /**
     * Create task comment notification
     */
    public Notification notifyTaskComment(Integer taskId, Integer commenterId, 
                                        String taskTitle, Integer assigneeId) throws SQLException {
        // Don't notify the commenter about their own comment
        if (commenterId.equals(assigneeId)) {
            return null;
        }
        
        User commenter = userDAO.findById(commenterId);
        if (commenter != null) {
            String message = commenter.getFullName() + " commented on task: " + taskTitle;
            String link = "/tasks/view/" + taskId + "#comments";
            
            return createNotificationWithRelatedId(message, "task_comment", assigneeId, link, taskId);
        }
        return null;
    }
    
    /**
     * Create task due soon notification
     */
    public Notification notifyTaskDueSoon(Integer taskId, Integer assigneeId, 
                                        String taskTitle, String dueDate) throws SQLException {
        String message = "Task due soon: " + taskTitle + " (Due: " + dueDate + ")";
        String link = "/tasks/view/" + taskId;
        
        return createNotificationWithRelatedId(message, "task_due_soon", assigneeId, link, taskId);
    }
    
    /**
     * Create file upload notification
     */
    public Notification notifyFileUpload(Integer fileId, Integer uploaderId, Integer projectId, String fileName) throws SQLException {
        User uploader = userDAO.findById(uploaderId);
        if (uploader != null) {
            String message = uploader.getFullName() + " uploaded a new file: " + fileName;
            String link = "/projects/view/" + projectId + "?tab=files";
            
            // Get project members to notify (excluding uploader)
            List<User> projectMembers = userDAO.findByProjectId(projectId);
            for (User member : projectMembers) {
                if (!member.getId().equals(uploaderId)) {
                    createNotificationWithRelatedId(message, "file_upload", member.getId(), link, fileId);
                }
            }
            
            // Return the first notification created (if any members)
            if (!projectMembers.isEmpty()) {
                for (User member : projectMembers) {
                    if (!member.getId().equals(uploaderId)) {
                        return createNotificationWithRelatedId(message, "file_upload", member.getId(), link, fileId);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get a notification by ID and return it as a JSONObject
     */
    public JSONObject getNotificationById(long notificationId) throws SQLException {
        Notification notification = notificationDAO.findById((int)notificationId);
        if (notification != null) {
            JSONObject json = new JSONObject();
            json.put("id", notification.getId());
            json.put("message", notification.getMessage());
            json.put("type", notification.getType());
            json.put("read", notification.isRead());
            json.put("creationDate", notification.getCreationDate().toString());
            json.put("link", notification.getLink());
            if (notification.getRelatedId() != null) {
                json.put("relatedId", notification.getRelatedId());
            }
            json.put("userId", notification.getUserId());
            return json;
        }
        return null;
    }
    
    /**
     * Find a notification by ID
     */
    public Notification findById(Integer notificationId) throws SQLException {
        return notificationDAO.findById(notificationId);
    }
    
    /**
     * Delete all notifications for a user
     */
    public boolean deleteAllUserNotifications(Integer userId) throws SQLException {
        return notificationDAO.deleteAllByUserId(userId);
    }
}