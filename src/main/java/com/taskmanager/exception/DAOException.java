package com.taskmanager.exception;

/**
 * Exception thrown when a database operation fails.
 * This exception encapsulates SQL exceptions and other data access issues
 * to provide more meaningful error messages to higher layers.
 */
public class DAOException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Error codes for specific database-related issues
     */
    public static final int CONNECTION_ERROR = 1001;
    public static final int QUERY_ERROR = 1002;
    public static final int INSERT_ERROR = 1003;
    public static final int UPDATE_ERROR = 1004;
    public static final int DELETE_ERROR = 1005;
    public static final int TRANSACTION_ERROR = 1006;
    public static final int DUPLICATE_KEY = 1007;
    public static final int FOREIGN_KEY_VIOLATION = 1008;
    
    private int errorCode;
    
    /**
     * Constructs a new DAO exception with null as its detail message.
     */
    public DAOException() {
        super();
    }
    
    /**
     * Constructs a new DAO exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public DAOException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DAO exception with the specified detail message and error code.
     * 
     * @param message the detail message
     * @param errorCode the error code
     */
    public DAOException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new DAO exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new DAO exception with the specified detail message, cause, and error code.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param errorCode the error code
     */
    public DAOException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new DAO exception with the specified cause.
     * 
     * @param cause the cause
     */
    public DAOException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Sets the error code for this exception.
     * 
     * @param errorCode the error code to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}