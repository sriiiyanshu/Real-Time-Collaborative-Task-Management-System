package com.taskmanager.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Utility class for database operations.
 */
public class DatabaseUtil {
    
    private static DataSource dataSource;
    
    static {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/taskmanager");
        } catch (NamingException e) {
            LogUtil.error("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Gets a database connection from the connection pool.
     * 
     * @return A database connection
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Data source is not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes database resources safely.
     * 
     * @param connection The database connection to close
     * @param statement The prepared statement to close
     * @param resultSet The result set to close
     */
    public static void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LogUtil.error("Error closing ResultSet", e);
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LogUtil.error("Error closing PreparedStatement", e);
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LogUtil.error("Error closing Connection", e);
            }
        }
    }
    
    /**
     * Begins a transaction by setting auto-commit to false.
     * 
     * @param connection The database connection
     * @throws SQLException If a database access error occurs
     */
    public static void beginTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.setAutoCommit(false);
        }
    }
    
    /**
     * Commits a transaction and restores auto-commit mode.
     * 
     * @param connection The database connection
     * @throws SQLException If a database access error occurs
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Rolls back a transaction and restores auto-commit mode.
     * 
     * @param connection The database connection
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LogUtil.error("Error during transaction rollback", e);
            }
        }
    }
}