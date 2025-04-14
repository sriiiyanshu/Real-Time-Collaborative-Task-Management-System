package com.taskmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.taskmanager.config.DatabaseConfig;

/**
 * Base Data Access Object class that provides common functionality
 * for all DAO implementations.
 */
public abstract class BaseDAO {
    
    /**
     * Get a database connection
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
    
    /**
     * Close database resources safely
     */
    protected void closeResources(AutoCloseable... resources) {
        DatabaseConfig.closeResources(resources);
    }
    
    /**
     * Execute a query that returns a single integer result
     */
    protected Integer executeCountQuery(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
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
     * Execute an update operation (INSERT, UPDATE, DELETE)
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
        } finally {
            closeResources(stmt, conn);
        }
    }
    
    /**
     * Execute an insert operation and return the generated key
     */
    protected Integer executeInsert(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * Begin a database transaction
     */
    protected Connection beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    
    /**
     * Commit a transaction
     */
    protected void commitTransaction(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Rollback a transaction
     */
    protected void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
}