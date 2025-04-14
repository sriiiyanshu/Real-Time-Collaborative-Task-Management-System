package com.taskmanager.util;

import com.taskmanager.model.User;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for authentication-related operations.
 */
public class AuthUtil {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int REMEMBER_ME_EXPIRY_DAYS = 30;
    private static final Map<String, TokenInfo> resetTokens = new HashMap<>();
    
    // Inner class to store reset token information
    private static class TokenInfo {
        private final String userEmail;
        private final LocalDateTime expiryTime;
        
        public TokenInfo(String userEmail, LocalDateTime expiryTime) {
            this.userEmail = userEmail;
            this.expiryTime = expiryTime;
        }
        
        public String getUserEmail() {
            return userEmail;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
    
    /**
     * Retrieves the current user from the session.
     * 
     * @param request The HTTP request object
     * @return The User object if logged in, null otherwise
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
    
    /**
     * Generates a secure random token for password reset or API authentication.
     * 
     * @return A Base64 encoded random token
     */
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Checks if the user has the specified role.
     * 
     * @param user The user to check
     * @param role The role to verify
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }
        String[] roles = user.getRoles();
        if (roles == null) {
            return false;
        }
        
        for (String userRole : roles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validates a password against the security requirements.
     * 
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        
        return hasLetter && hasDigit && hasSpecial;
    }
    
    /**
     * Hashes a password with a salt using SHA-256 algorithm.
     * 
     * @param password The password to hash
     * @return A string in the format "hash:salt" where both are Base64 encoded
     * @throws RuntimeException if the hashing algorithm is not available
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hashedPassword);
            
            return hashString + ":" + saltString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies a password against its stored hash.
     * 
     * @param password The password to verify
     * @param storedHash The stored hash in the format "hash:salt"
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] hash = Base64.getDecoder().decode(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] newHash = md.digest(password.getBytes());
            
            // Compare the two hashes
            if (hash.length != newHash.length) {
                return false;
            }
            
            int diff = 0;
            for (int i = 0; i < hash.length; i++) {
                diff |= hash[i] ^ newHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Sets a remember-me cookie for auto-login functionality.
     * 
     * @param response The HTTP response object
     * @param userId The user ID to remember
     * @param rememberToken A secure token associated with the user
     */
    public static void setRememberMeCookie(HttpServletResponse response, long userId, String rememberToken) {
        // Create userId cookie
        Cookie userIdCookie = new Cookie("auth_user_id", String.valueOf(userId));
        userIdCookie.setHttpOnly(true);
        userIdCookie.setSecure(true); // Use only in HTTPS
        userIdCookie.setMaxAge(REMEMBER_ME_EXPIRY_DAYS * 24 * 60 * 60); // days to seconds
        userIdCookie.setPath("/");
        
        // Create token cookie
        Cookie tokenCookie = new Cookie("auth_token", rememberToken);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setMaxAge(REMEMBER_ME_EXPIRY_DAYS * 24 * 60 * 60);
        tokenCookie.setPath("/");
        
        response.addCookie(userIdCookie);
        response.addCookie(tokenCookie);
    }
    
    /**
     * Checks if a password reset token is valid.
     * 
     * @param token The reset token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public static boolean isValidResetToken(String token) {
        if (token == null) {
            return false;
        }
        
        TokenInfo info = resetTokens.get(token);
        if (info == null) {
            return false;
        }
        
        // Check if the token is not expired
        return LocalDateTime.now().isBefore(info.getExpiryTime());
    }

    /**
     * Checks for a remember-me cookie and authenticates the user if a valid token is found.
     * This overload accepts an HttpSession parameter.
     * 
     * @param request The HTTP request object
     * @param response The HTTP response object
     * @param session The current session (or null if none exists)
     * @return true if user was authenticated, false otherwise
     */
    public boolean checkRememberMeCookie(HttpServletRequest request, HttpServletResponse response, 
            HttpSession session) {
        try {
            // Check if user is already logged in
            if (session != null && session.getAttribute("user") != null) {
                return true;
            }
            
            // Look for remember-me cookies
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return false;
            }
            
            String userId = null;
            String authToken = null;
            
            // Find the user ID and auth token cookies
            for (Cookie cookie : cookies) {
                if ("auth_user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                } else if ("auth_token".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
            
            // If we found both cookies
            if (userId != null && authToken != null) {
                try {
                    int userIdInt = Integer.parseInt(userId);
                    com.taskmanager.dao.UserDAO userDAO = new com.taskmanager.dao.UserDAO();
                    User user = userDAO.findById(userIdInt);
                    
                    // Check that the user exists and has a matching remember token
                    if (user != null && user.getRememberToken() != null && 
                            user.getRememberToken().equals(authToken) && user.isActive()) {
                        
                        // Create a new session and authenticate the user
                        HttpSession newSession = request.getSession(true);
                        newSession.setAttribute("user", user);
                        
                        // For security, rotate the remember token
                        String newToken = generateSecureToken();
                        user.setRememberToken(newToken);
                        userDAO.update(user);
                        
                        // Update the cookie with the new token
                        setRememberMeCookie(response, user.getId(), newToken);
                        
                        return true;
                    }
                } catch (Exception e) {
                    // Log the error and continue - auto-login failure should be silent to the user
                    LogUtil.error("Error in instance checkRememberMeCookie", e);
                }
            }
            
            return false;
        } catch (Exception e) {
            // Log the error and return false
            LogUtil.error("Unexpected error in instance checkRememberMeCookie", e);
            return false;
        }
    }
}

