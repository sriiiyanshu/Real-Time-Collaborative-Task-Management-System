package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.taskmanager.model.Task;

/**
 * Data Access Object for Task entities
 */
public class TaskDAO extends BaseDAO {
    
    /**
     * Insert a new task into the database
     */
    public Integer insert(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (title, description, creation_date, due_date, " +
                     "project_id, assignee_id, creator_id, priority, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Timestamp dueDate = task.getDueDate() != null ? 
                new Timestamp(task.getDueDate().getTime()) : null;
        
        return executeInsert(sql, 
                task.getTitle(), 
                task.getDescription(), 
                new Timestamp(task.getCreationDate().getTime()),
                dueDate,
                task.getProjectId(),
                task.getAssigneeId(),
                task.getCreatorId(),
                task.getPriority(),
                task.getStatus());
    }
    
    /**
     * Update an existing task
     */
    public boolean update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, " +
                     "assignee_id = ?, priority = ?, status = ?, completion_date = ? " +
                     "WHERE task_id = ?";
        
        Timestamp dueDate = task.getDueDate() != null ? 
                new Timestamp(task.getDueDate().getTime()) : null;
        
        Timestamp completionDate = task.getCompletionDate() != null ? 
                new Timestamp(task.getCompletionDate().getTime()) : null;
        
        int rowsUpdated = executeUpdate(sql, 
                task.getTitle(), 
                task.getDescription(), 
                dueDate,
                task.getAssigneeId(),
                task.getPriority(),
                task.getStatus(),
                completionDate,
                task.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Find a task by ID
     */
    public Task findById(Integer taskId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE task_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToTask(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all tasks for a project
     */
    public List<Task> findByProjectId(Integer projectId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE project_id = ? ORDER BY due_date, priority DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get tasks assigned to a user
     */
    public List<Task> findByAssigneeId(Integer assigneeId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ? ORDER BY due_date, priority DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, assigneeId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get tasks created by a user
     */
    public List<Task> findByCreatorId(Integer creatorId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE creator_id = ? ORDER BY creation_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, creatorId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get tasks by status
     */
    public List<Task> findByStatus(String status, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status = ? AND project_id = ? ORDER BY due_date, priority DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, projectId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get overdue tasks
     */
    public List<Task> findOverdueTasks() throws SQLException {
        String sql = "SELECT * FROM tasks WHERE due_date < ? AND status != 'Completed' " +
                     "ORDER BY due_date, priority DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Delete a task
     */
    public boolean delete(Integer taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        
        int rowsDeleted = executeUpdate(sql, taskId);
        return rowsDeleted > 0;
    }
    
    /**
     * Search tasks by title or description
     */
    public List<Task> searchTasks(String query) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE title LIKE ? OR description LIKE ? " +
                     "ORDER BY due_date, priority DESC";
        String searchPattern = "%" + query + "%";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count tasks in a project
     */
    public Integer countTasksByProject(Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE project_id = ?";
        return executeCountQuery(sql, projectId);
    }
    
    /**
     * Count completed tasks in a project
     */
    public Integer countCompletedTasksByProject(Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND status = 'Completed'";
        return executeCountQuery(sql, projectId);
    }
    
    /**
     * Count total number of tasks in the system
     */
    public Integer countTasks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks";
        return executeCountQuery(sql);
    }
    
    /**
     * Count total number of completed tasks in the system
     */
    public Integer countCompletedTasks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE status = 'Completed'";
        return executeCountQuery(sql);
    }
    
    /**
     * Count total number of overdue tasks in the system
     */
    public Integer countOverdueTasks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE due_date < ? AND status != 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            
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
     * Count tasks assigned to a specific user
     */
    public Integer countTasksByAssignee(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ?";
        return executeCountQuery(sql, userId);
    }
    
    /**
     * Count completed tasks by a specific user
     */
    public Integer countCompletedTasksByAssignee(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND status = 'Completed'";
        return executeCountQuery(sql, userId);
    }
    
    /**
     * Count overdue tasks assigned to a specific user
     */
    public Integer countOverdueTasksByAssignee(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND due_date < ? AND status != 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
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
     * Count tasks completed since a specific date by a user
     */
    public Integer countTasksCompletedSinceByAssignee(Integer userId, Date since) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND completion_date >= ? AND status = 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setTimestamp(2, new Timestamp(since.getTime()));
            
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
     * Count tasks completed on time by a user (before or on due date)
     */
    public Integer countTasksCompletedOnTimeByAssignee(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND status = 'Completed' " +
                     "AND (completion_date <= due_date OR due_date IS NULL)";
        return executeCountQuery(sql, userId);
    }
    
    /**
     * Count overdue tasks in a project
     */
    public Integer countOverdueTasksByProject(Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND due_date < ? AND status != 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
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
     * Count tasks assigned to a user in a specific project
     */
    public Integer countTasksByAssigneeAndProject(Integer userId, Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND project_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            
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
     * Count completed tasks by a user in a specific project
     */
    public Integer countCompletedTasksByAssigneeAndProject(Integer userId, Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND project_id = ? AND status = 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            
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
     * Count tasks created on a specific date
     */
    public Integer countTasksCreatedOnDate(Date date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE DATE(creation_date) = DATE(?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            
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
     * Count tasks created on a specific date for a project
     */
    public Integer countTasksCreatedOnDateForProject(Date date, Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE DATE(creation_date) = DATE(?) AND project_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            stmt.setInt(2, projectId);
            
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
     * Count tasks completed on a specific date
     */
    public Integer countTasksCompletedOnDate(Date date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE DATE(completion_date) = DATE(?) AND status = 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            
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
     * Count tasks completed on a specific date for a project
     */
    public Integer countTasksCompletedOnDateForProject(Date date, Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE DATE(completion_date) = DATE(?) " +
                     "AND project_id = ? AND status = 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            stmt.setInt(2, projectId);
            
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
     * Find all tasks accessible by a specific user (assigned to them or in projects they are members of)
     */
    public List<Task> findTasksAccessibleByUser(Integer userId) throws SQLException {
        String sql = "SELECT DISTINCT t.* FROM tasks t " +
                     "LEFT JOIN project_members pm ON t.project_id = pm.project_id " +
                     "WHERE t.assignee_id = ? OR t.creator_id = ? OR pm.user_id = ? " +
                     "ORDER BY t.due_date, t.priority DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find tags that contain the given search string
     * Limited by projectId (if not null) and limit count
     */
    public List<String> findTagsContaining(String searchText, Integer projectId, int limit) throws SQLException {
        String sql;
        if (projectId != null) {
            sql = "SELECT DISTINCT tag FROM task_tags " +
                  "JOIN tasks ON task_tags.task_id = tasks.task_id " +
                  "WHERE tag LIKE ? AND tasks.project_id = ? " +
                  "ORDER BY tag LIMIT ?";
        } else {
            sql = "SELECT DISTINCT tag FROM task_tags " +
                  "WHERE tag LIKE ? " +
                  "ORDER BY tag LIMIT ?";
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> tags = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");
            
            if (projectId != null) {
                stmt.setInt(2, projectId);
                stmt.setInt(3, limit);
            } else {
                stmt.setInt(2, limit);
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
            return tags;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find tasks with titles containing the given search string
     * Limited by projectId (if not null) and limit count
     */
    public List<Task> findTasksWithTitleContaining(String searchText, Integer projectId, int limit) throws SQLException {
        String sql;
        if (projectId != null) {
            sql = "SELECT * FROM tasks " +
                  "WHERE title LIKE ? AND project_id = ? " +
                  "ORDER BY due_date, priority DESC LIMIT ?";
        } else {
            sql = "SELECT * FROM tasks " +
                  "WHERE title LIKE ? " +
                  "ORDER BY due_date, priority DESC LIMIT ?";
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");
            
            if (projectId != null) {
                stmt.setInt(2, projectId);
                stmt.setInt(3, limit);
            } else {
                stmt.setInt(2, limit);
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find related tasks for a given task
     * 
     * @param taskId The ID of the task to find related tasks for
     * @return List of tasks related to the given task
     * @throws SQLException if a database error occurs
     */
    public List<Task> findRelatedTasks(Integer taskId) throws SQLException {
        // First get the task's project ID to find tasks in the same project
        Task task = findById(taskId);
        if (task == null) {
            return new ArrayList<>();
        }
        
        Integer projectId = task.getProjectId();
        String tagsSql = "SELECT tag FROM task_tags WHERE task_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> taskTags = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(tagsSql);
            stmt.setInt(1, taskId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                taskTags.add(rs.getString("tag"));
            }
        } finally {
            closeResources(rs, stmt, null);
        }
        
        // If no tags, just return tasks from the same project
        if (taskTags.isEmpty()) {
            String sql = "SELECT * FROM tasks WHERE project_id = ? AND task_id != ? " +
                         "ORDER BY due_date LIMIT 5";
            
            List<Task> relatedTasks = new ArrayList<>();
            
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, projectId);
                stmt.setInt(2, taskId);
                
                rs = stmt.executeQuery();
                while (rs.next()) {
                    relatedTasks.add(mapRowToTask(rs));
                }
                return relatedTasks;
            } finally {
                closeResources(rs, stmt, conn);
            }
        } else {
            // If there are tags, find tasks that share the same tags
            StringBuilder tagQuery = new StringBuilder();
            tagQuery.append("SELECT t.*, COUNT(tt.tag) as tag_match_count FROM tasks t ");
            tagQuery.append("JOIN task_tags tt ON t.task_id = tt.task_id ");
            tagQuery.append("WHERE tt.tag IN (");
            
            for (int i = 0; i < taskTags.size(); i++) {
                tagQuery.append("?");
                if (i < taskTags.size() - 1) {
                    tagQuery.append(",");
                }
            }
            
            tagQuery.append(") AND t.task_id != ? ");
            tagQuery.append("GROUP BY t.task_id ");
            tagQuery.append("ORDER BY tag_match_count DESC, t.due_date ");
            tagQuery.append("LIMIT 5");
            
            List<Task> relatedTasks = new ArrayList<>();
            
            try {
                stmt = conn.prepareStatement(tagQuery.toString());
                
                int paramIndex = 1;
                for (String tag : taskTags) {
                    stmt.setString(paramIndex++, tag);
                }
                stmt.setInt(paramIndex, taskId);
                
                rs = stmt.executeQuery();
                while (rs.next()) {
                    relatedTasks.add(mapRowToTask(rs));
                }
                return relatedTasks;
            } finally {
                closeResources(rs, stmt, conn);
            }
        }
    }
    
    /**
     * Count the number of comments for a task
     * 
     * @param taskId The ID of the task
     * @return Count of comments for the task
     * @throws SQLException if a database error occurs
     */
    public Integer countCommentsByTaskId(Integer taskId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comments WHERE task_id = ?";
        return executeCountQuery(sql, taskId);
    }
    
    /**
     * Count the number of file attachments for a task
     * 
     * @param taskId The ID of the task
     * @return Count of attachments for the task
     * @throws SQLException if a database error occurs
     */
    public Integer countAttachmentsByTaskId(Integer taskId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM task_attachments WHERE task_id = ?";
        return executeCountQuery(sql, taskId);
    }
    
    /**
     * Find upcoming tasks assigned to a user
     * 
     * @param assigneeId The ID of the user to find tasks for
     * @param limit Maximum number of tasks to return
     * @return List of upcoming tasks for the user
     * @throws SQLException if a database error occurs
     */
    public List<Task> findUpcomingTasksByAssignee(Integer assigneeId, int limit) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ? AND status != 'Completed' " +
                     "AND due_date >= ? ORDER BY due_date ASC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, assigneeId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find overdue tasks assigned to a user
     * 
     * @param assigneeId The ID of the user to find tasks for
     * @param limit Maximum number of tasks to return
     * @return List of overdue tasks for the user
     * @throws SQLException if a database error occurs
     */
    public List<Task> findOverdueTasksByAssignee(Integer assigneeId, int limit) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ? AND status != 'Completed' " +
                     "AND due_date < ? ORDER BY due_date ASC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, assigneeId);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count tasks due today for a specific user
     *
     * @param userId The ID of the user
     * @return Count of tasks due today for the user
     * @throws SQLException if a database error occurs
     */
    public int countTasksDueToday(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND DATE(due_date) = CURRENT_DATE AND status != 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            
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
     * Count overdue tasks for a specific user
     *
     * @param userId The ID of the user
     * @return Count of overdue tasks for the user
     * @throws SQLException if a database error occurs
     */
    public int countOverdueTasks(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND due_date < CURRENT_DATE AND status != 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            
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
     * Count completed tasks for a specific user
     *
     * @param userId The ID of the user
     * @return Count of completed tasks for the user
     * @throws SQLException if a database error occurs
     */
    public int countCompletedTasks(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE assignee_id = ? AND status = 'Completed'";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            
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
     * Find recent tasks for a specific user
     *
     * @param userId The ID of the user
     * @param limit Maximum number of tasks to return
     * @return List of recent tasks for the user
     * @throws SQLException if a database error occurs
     */
    public List<Task> findRecentTasks(Long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ? OR creator_id = ? " +
                     "ORDER BY creation_date DESC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Find upcoming deadlines for a specific user
     *
     * @param userId The ID of the user
     * @param limit Maximum number of tasks to return
     * @return List of upcoming tasks with deadlines for the user
     * @throws SQLException if a database error occurs
     */
    public List<Task> findUpcomingDeadlines(Long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ? AND status != 'Completed' " +
                     "AND due_date >= CURRENT_DATE ORDER BY due_date ASC LIMIT ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Task> tasks = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            return tasks;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Map a database row to a Task object
     */
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("task_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            task.setCreationDate(new Date(creationDate.getTime()));
        }
        
        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) {
            task.setDueDate(new Date(dueDate.getTime()));
        }
        
        Timestamp completionDate = rs.getTimestamp("completion_date");
        if (completionDate != null) {
            task.setCompletionDate(new Date(completionDate.getTime()));
        }
        
        task.setProjectId(rs.getInt("project_id"));
        task.setAssigneeId(rs.getInt("assignee_id"));
        task.setCreatorId(rs.getInt("creator_id"));
        task.setPriority(rs.getString("priority"));
        task.setStatus(rs.getString("status"));
        
        return task;
    }
}