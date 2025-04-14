package com.taskmanager.exception;

/**
 * Exception thrown when a file operation fails.
 * This exception is used for file upload, download, deletion, or other file operation issues.
 */
public class FileOperationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Error codes for specific file-related issues
     */
    public static final int FILE_NOT_FOUND = 2001;
    public static final int FILE_ACCESS_DENIED = 2002;
    public static final int FILE_TOO_LARGE = 2003;
    public static final int INVALID_FILE_FORMAT = 2004;
    public static final int UPLOAD_ERROR = 2005;
    public static final int DOWNLOAD_ERROR = 2006;
    public static final int DELETE_ERROR = 2007;
    public static final int STORAGE_LIMIT_EXCEEDED = 2008;
    public static final int IO_ERROR = 2009;
    
    private int errorCode;
    private String filePath;
    
    /**
     * Constructs a new file operation exception with null as its detail message.
     */
    public FileOperationException() {
        super();
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public FileOperationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message and error code.
     * 
     * @param message the detail message
     * @param errorCode the error code
     */
    public FileOperationException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message, 
     * error code, and file path.
     * 
     * @param message the detail message
     * @param errorCode the error code
     * @param filePath the path of the file that caused the exception
     */
    public FileOperationException(String message, int errorCode, String filePath) {
        super(message);
        this.errorCode = errorCode;
        this.filePath = filePath;
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message,
     * cause, and error code.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param errorCode the error code
     */
    public FileOperationException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message,
     * cause, error code, and file path.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param errorCode the error code
     * @param filePath the path of the file that caused the exception
     */
    public FileOperationException(String message, Throwable cause, int errorCode, String filePath) {
        super(message, cause);
        this.errorCode = errorCode;
        this.filePath = filePath;
    }
    
    /**
     * Constructs a new file operation exception with the specified cause.
     * 
     * @param cause the cause
     */
    public FileOperationException(Throwable cause) {
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
    
    /**
     * Gets the file path associated with this exception.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Sets the file path associated with this exception.
     * 
     * @param filePath the file path to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}