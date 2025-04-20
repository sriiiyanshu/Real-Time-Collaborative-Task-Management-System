package com.taskmanager.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.taskmanager.dao.CommentDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Comment;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import org.json.JSONObject;

/**
 * Service class for managing comments
 */
public class CommentService {
    
    private CommentDAO commentDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    
    public CommentService() {
        commentDAO = new CommentDAO();
        taskDAO = new TaskDAO();
        userDAO = new UserDAO();
        notificationService = new NotificationService();
    }
    
    /**
     * Create a new comment on a task
     * @return Comment object with ID or null if creation failed
     */
    public Comment createTaskComment(Integer taskId, Integer userId, String content) throws SQLException {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        // Check if task exists
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task does not exist");
        }
        
        // Create comment
        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreationDate(new Date());
        
        // Save comment
        Integer commentId = commentDAO.insert(comment);
        if (commentId != null) {
            comment.setId(commentId);
            
            // Notify task assignee about the comment if they're not the commenter
            if (task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
                notificationService.notifyTaskComment(
                    taskId, 
                    userId, 
                    task.getTitle(), 
                    task.getAssigneeId()
                );
            }
            
            return comment;
        }
        
        return null;
    }
    
    /**
     * Create a comment from JSON data
     * 
     * @param jsonData JSON object containing comment data (taskId, userId, content)
     * @return Comment object with ID or null if creation failed
     * @throws SQLException if a database error occurs
     */
    public Comment createComment(JSONObject jsonData) throws SQLException {
        // Extract data from JSON
        Integer taskId = null;
        Integer userId = null;
        String content = null;
        
        if (jsonData.has("taskId")) {
            taskId = jsonData.getInt("taskId");
        }
        
        if (jsonData.has("userId")) {
            userId = jsonData.getInt("userId");
        }
        
        if (jsonData.has("content")) {
            content = jsonData.getString("content");
        }
        
        // Validate required fields
        if (taskId == null || userId == null || content == null) {
            throw new IllegalArgumentException("Missing required fields: taskId, userId, and content are required");
        }
        
        // Call the existing method to create the task comment
        return createTaskComment(taskId, userId, content);
    }
    
    /**
     * Get a comment by its ID
     */
    public Comment getCommentById(Integer commentId) throws SQLException {
        return commentDAO.findById(commentId);
    }
    
    /**
     * Get all comments for a specific task
     */
    public List<Comment> getCommentsByTaskId(Integer taskId) throws SQLException {
        return commentDAO.findByTaskId(taskId);
    }
    
    /**
     * Update an existing comment
     */
    public boolean updateComment(Integer commentId, String content, Integer userId) throws SQLException {
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        // Check if comment exists and user is the author
        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            return false;
        }
        
        // Check if user is the comment author
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to edit this comment");
        }
        
        // Update comment
        comment.setContent(content);
        comment.setLastModified(new Date());
        
        return commentDAO.update(comment);
    }
    
    /**
     * Delete a comment
     */
    public boolean deleteComment(Integer commentId, Integer userId) throws SQLException {
        // Check if comment exists
        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            return false;
        }
        
        // Check if user is the comment author
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }
        
        return commentDAO.delete(commentId);
    }
    
    /**
     * Count comments for a task
     */
    public int countCommentsByTaskId(Integer taskId) throws SQLException {
        return commentDAO.countByTaskId(taskId);
    }
    
    /**
     * Get recent comments for a user (comments on their tasks)
     */
    public List<Comment> getRecentCommentsForUser(Integer userId, int limit) throws SQLException {
        return commentDAO.findRecentCommentsForUser(userId, limit);
    }
    
    /**
     * Get user information for a comment
     */
    public User getCommentAuthor(Comment comment) throws SQLException {
        if (comment != null && comment.getUserId() != null) {
            return userDAO.findById(comment.getUserId());
        }
        return null;
    }
    
    /**
     * Get comments for a specific task (alias method for getCommentsByTaskId)
     * @param taskId The ID of the task to get comments for
     * @return List of comments for the specified task
     * @throws SQLException if a database error occurs
     */
    public List<Comment> getCommentsByTask(int taskId) throws SQLException {
        return getCommentsByTaskId(taskId);
    }
    
    /**
     * Check if a comment was edited
     */
    public boolean wasCommentEdited(Comment comment) {
        return comment != null && comment.getLastModified() != null;
    }
}