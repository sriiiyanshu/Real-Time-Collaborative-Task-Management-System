package com.taskmanager.exception;

/**
 * Exception thrown when authentication fails.
 * This exception is used for login failures, token validation failures,
 * session expiration, or other authentication-related issues.
 */
public class AuthenticationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new authentication exception with null as its detail message.
     */
    public AuthenticationException() {
        super();
    }
    
    /**
     * Constructs a new authentication exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new authentication exception with the specified cause.
     * 
     * @param cause the cause
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
