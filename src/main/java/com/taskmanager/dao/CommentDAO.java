package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.taskmanager.model.Comment;

/**
 * Data Access Object for Comment entities
 */
public class CommentDAO extends BaseDAO {
    
    /**
     * Insert a new comment into the database
     * 
     * @param comment The comment to insert
     * @return The ID of the newly created comment
     * @throws SQLException if a database error occurs
     */
    public Integer insert(Comment comment) throws SQLException {
        String sql = "INSERT INTO comments (task_id, user_id, content, creation_date) " +
                     "VALUES (?, ?, ?, ?)";
        
        return executeInsert(sql, 
                comment.getTaskId(), 
                comment.getUserId(), 
                comment.getContent(), 
                new Timestamp(comment.getCreationDate().getTime()));
    }
    
    /**
     * Update an existing comment
     * 
     * @param comment The comment to update
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean update(Comment comment) throws SQLException {
        String sql = "UPDATE comments SET content = ?, last_modified = ? WHERE comment_id = ?";
        
        Timestamp lastModified = comment.getLastModified() != null ? 
                new Timestamp(comment.getLastModified().getTime()) : null;
        
        int rowsUpdated = executeUpdate(sql, 
                comment.getContent(), 
                lastModified, 
                comment.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Delete a comment by its ID
     * 
     * @param commentId The ID of the comment to delete
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean delete(Integer commentId) throws SQLException {
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        
        int rowsDeleted = executeUpdate(sql, commentId);
        return rowsDeleted > 0;
    }
    
    /**
     * Find a comment by its ID
     * 
     * @param commentId The ID of the comment to find
     * @return The comment object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Comment findById(Integer commentId) throws SQLException {
        String sql = "SELECT * FROM comments WHERE comment_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, commentId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToComment(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all comments for a specific task
     * 
     * @param taskId The ID of the task
     * @return List of comments for the task
     * @throws SQLException if a database error occurs
     */
    public List<Comment> findByTaskId(Integer taskId) throws SQLException {
        String sql = "SELECT * FROM comments WHERE task_id = ? ORDER BY creation_date";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Comment> comments = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapRowToComment(rs));
            }
            return comments;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count comments for a specific task
     * 
     * @param taskId The ID of the task
     * @return The number of comments for the task
     * @throws SQLException if a database error occurs
     */
    public int countByTaskId(Integer taskId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comments WHERE task_id = ?";
        return executeCountQuery(sql, taskId);
    }
    
    /**
     * Find recent comments for tasks assigned to a user
     * 
     * @param userId The ID of the user
     * @param limit Maximum number of comments to return
     * @return List of recent comments
     * @throws SQLException if a database error occurs
     */
    public List<Comment> findRecentCommentsForUser(Integer userId, int limit) throws SQLException {
        String sql = "SELECT c.* FROM comments c " +
                     "JOIN tasks t ON c.task_id = t.task_id " +
                     "WHERE t.assignee_id = ? " +
                     "ORDER BY c.creation_date DESC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Comment> comments = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapRowToComment(rs));
            }
            return comments;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find all comments for a list of tasks
     * 
     * @param taskIds The list of task IDs
     * @return Map of task ID to list of comments for that task
     * @throws SQLException if a database error occurs
     */
    public Map<Integer, List<Comment>> findByTaskIds(List<Integer> taskIds) throws SQLException {
        if (taskIds == null || taskIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // Create placeholder string for the IN clause (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(taskIds.size(), "?"));
        String sql = "SELECT * FROM comments WHERE task_id IN (" + placeholders + ") ORDER BY task_id, creation_date";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, List<Comment>> commentsByTaskId = new HashMap<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Set task IDs as parameters
            for (int i = 0; i < taskIds.size(); i++) {
                stmt.setInt(i + 1, taskIds.get(i));
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = mapRowToComment(rs);
                Integer taskId = comment.getTaskId();
                
                // Add comment to the appropriate list in the map
                commentsByTaskId
                    .computeIfAbsent(taskId, k -> new ArrayList<>())
                    .add(comment);
            }
            
            return commentsByTaskId;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Map a database row to a Comment object
     * 
     * @param rs The ResultSet to map
     * @return A Comment object
     * @throws SQLException if a database error occurs
     */
    private Comment mapRowToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("comment_id"));
        comment.setTaskId(rs.getInt("task_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setContent(rs.getString("content"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            comment.setCreationDate(new Date(creationDate.getTime()));
        }
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            comment.setLastModified(new Date(lastModified.getTime()));
        }
        
        return comment;
    }
}