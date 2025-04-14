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

import com.taskmanager.model.File;

/**
 * Data Access Object for File entities
 */
public class FileDAO extends BaseDAO {
    
    /**
     * Insert a new file record into the database
     */
    public Integer insert(File file) throws SQLException {
        String sql = "INSERT INTO files (filename, file_path, file_size, file_type, upload_date, " +
                     "uploader_id, task_id, project_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        return executeInsert(sql, 
                file.getFilename(), 
                file.getFilePath(), 
                file.getFileSize(),
                file.getFileType(),
                new Timestamp(file.getUploadDate().getTime()),
                file.getUploaderId(),
                file.getTaskId(),
                file.getProjectId());
    }
    
    /**
     * Update an existing file record
     */
    public boolean update(File file) throws SQLException {
        String sql = "UPDATE files SET filename = ?, file_path = ?, file_size = ?, " +
                     "file_type = ? WHERE file_id = ?";
        
        int rowsUpdated = executeUpdate(sql, 
                file.getFilename(), 
                file.getFilePath(), 
                file.getFileSize(),
                file.getFileType(),
                file.getId());
        
        return rowsUpdated > 0;
    }
    
    /**
     * Find a file by ID
     */
    public File findById(Integer fileId) throws SQLException {
        String sql = "SELECT * FROM files WHERE file_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, fileId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToFile(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all files for a project
     */
    public List<File> findByProjectId(Integer projectId) throws SQLException {
        String sql = "SELECT * FROM files WHERE project_id = ? ORDER BY upload_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<File> files = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                files.add(mapRowToFile(rs));
            }
            return files;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all files for a task
     */
    public List<File> findByTaskId(Integer taskId) throws SQLException {
        String sql = "SELECT * FROM files WHERE task_id = ? ORDER BY upload_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<File> files = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                files.add(mapRowToFile(rs));
            }
            return files;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get files uploaded by a specific user
     */
    public List<File> findByUploaderId(Integer uploaderId) throws SQLException {
        String sql = "SELECT * FROM files WHERE uploader_id = ? ORDER BY upload_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<File> files = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, uploaderId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                files.add(mapRowToFile(rs));
            }
            return files;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get files uploaded by a specific user (alias for findByUploaderId)
     */
    public List<File> findByUserId(Integer userId) throws SQLException {
        return findByUploaderId(userId);
    }
    
    /**
     * Get total storage size of all files in the system
     */
    public long getTotalStorageSize() throws SQLException {
        String sql = "SELECT SUM(file_size) FROM files";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get total storage size used by a specific user
     */
    public long getUserStorageSize(Integer userId) throws SQLException {
        String sql = "SELECT SUM(file_size) FROM files WHERE uploader_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count files by file type
     */
    public int countByFileType(String fileType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM files WHERE file_type = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fileType);
            
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
     * Delete a file record
     */
    public boolean delete(Integer fileId) throws SQLException {
        String sql = "DELETE FROM files WHERE file_id = ?";
        
        int rowsDeleted = executeUpdate(sql, fileId);
        return rowsDeleted > 0;
    }
    
    /**
     * Search files by filename
     */
    public List<File> searchFiles(String query) throws SQLException {
        String sql = "SELECT * FROM files WHERE filename LIKE ? ORDER BY upload_date DESC";
        String searchPattern = "%" + query + "%";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<File> files = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchPattern);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                files.add(mapRowToFile(rs));
            }
            return files;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Count files in a project
     */
    public Integer countFilesByProject(Integer projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM files WHERE project_id = ?";
        return executeCountQuery(sql, projectId);
    }
    
    /**
     * Get all files for a list of task IDs
     * 
     * @param taskIds List of task IDs to fetch files for
     * @return Map of task ID to list of files for that task
     * @throws SQLException if a database error occurs
     */
    public Map<Integer, List<File>> findByTaskIds(List<Integer> taskIds) throws SQLException {
        if (taskIds == null || taskIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // Create placeholder string for the IN clause (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(taskIds.size(), "?"));
        String sql = "SELECT * FROM files WHERE task_id IN (" + placeholders + ") ORDER BY task_id, upload_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, List<File>> filesByTaskId = new HashMap<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Set task IDs as parameters
            for (int i = 0; i < taskIds.size(); i++) {
                stmt.setInt(i + 1, taskIds.get(i));
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                File file = mapRowToFile(rs);
                Integer taskId = file.getTaskId();
                
                // Add file to the appropriate list in the map
                filesByTaskId
                    .computeIfAbsent(taskId, k -> new ArrayList<>())
                    .add(file);
            }
            
            return filesByTaskId;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Get all files for a list of project IDs
     * 
     * @param projectIds List of project IDs to fetch files for
     * @return Map of project ID to list of files for that project
     * @throws SQLException if a database error occurs
     */
    public Map<Integer, List<File>> findByProjectIds(List<Integer> projectIds) throws SQLException {
        if (projectIds == null || projectIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // Create placeholder string for the IN clause (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(projectIds.size(), "?"));
        String sql = "SELECT * FROM files WHERE project_id IN (" + placeholders + ") ORDER BY project_id, upload_date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, List<File>> filesByProjectId = new HashMap<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Set project IDs as parameters
            for (int i = 0; i < projectIds.size(); i++) {
                stmt.setInt(i + 1, projectIds.get(i));
            }
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                File file = mapRowToFile(rs);
                Integer projectId = file.getProjectId();
                
                // Add file to the appropriate list in the map
                filesByProjectId
                    .computeIfAbsent(projectId, k -> new ArrayList<>())
                    .add(file);
            }
            
            return filesByProjectId;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Map a database row to a File object
     */
    private File mapRowToFile(ResultSet rs) throws SQLException {
        File file = new File();
        file.setId(rs.getInt("file_id"));
        file.setFilename(rs.getString("filename"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileSize(rs.getLong("file_size"));
        file.setFileType(rs.getString("file_type"));
        
        Timestamp uploadDate = rs.getTimestamp("upload_date");
        if (uploadDate != null) {
            file.setUploadDate(new Date(uploadDate.getTime()));
        }
        
        file.setUploaderId(rs.getInt("uploader_id"));
        file.setTaskId(rs.getInt("task_id"));
        file.setProjectId(rs.getInt("project_id"));
        
        return file;
    }
}