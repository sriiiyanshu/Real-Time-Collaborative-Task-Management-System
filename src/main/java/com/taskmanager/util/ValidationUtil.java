package com.taskmanager.util;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for validating user inputs.
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s'-]{2,50}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$"
    );

    /**
     * Validates an email address.
     * 
     * @param email The email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates a username.
     * 
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validates a name.
     * 
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }
    
    /**
     * Validates a phone number.
     * 
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates a URL.
     * 
     * @param url The URL to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if (url == null) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * Validates a password.
     * Uses AuthUtil for detailed password validation.
     * 
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return AuthUtil.isValidPassword(password);
    }
    
    /**
     * Validates if a string is not null or empty.
     * 
     * @param str The string to check
     * @return true if not null or empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validates if a string is a valid integer.
     * 
     * @param str The string to check
     * @return true if valid integer, false otherwise
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates if a string is a valid long.
     * 
     * @param str The string to check
     * @return true if valid long, false otherwise
     */
    public static boolean isLong(String str) {
        if (str == null) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates if a string is a valid double.
     * 
     * @param str The string to check
     * @return true if valid double, false otherwise
     */
    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates that the value falls within a specified range.
     * 
     * @param value The value to check
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validates input data and returns validation errors.
     * 
     * @param data Map of data to validate
     * @param rules Map of validation rules (field name -> rule name)
     * @return Map of field names to error messages, empty if all valid
     */
    public static Map<String, String> validate(Map<String, String> data, Map<String, String> rules) {
        Map<String, String> errors = new HashMap<>();
        
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            String fieldName = rule.getKey();
            String ruleType = rule.getValue();
            String value = data.get(fieldName);
            
            if (ruleType.equals("required") && !isNotEmpty(value)) {
                errors.put(fieldName, fieldName + " is required");
            } else if (value != null && !value.isEmpty()) {
                switch (ruleType) {
                    case "email":
                        if (!isValidEmail(value)) {
                            errors.put(fieldName, "Invalid email format");
                        }
                        break;
                    case "username":
                        if (!isValidUsername(value)) {
                            errors.put(fieldName, "Username must be 3-20 alphanumeric characters or underscores");
                        }
                        break;
                    case "name":
                        if (!isValidName(value)) {
                            errors.put(fieldName, "Name must be 2-50 characters and contain only letters, spaces, hyphens or apostrophes");
                        }
                        break;
                    case "phone":
                        if (!isValidPhone(value)) {
                            errors.put(fieldName, "Invalid phone number format");
                        }
                        break;
                    case "url":
                        if (!isValidUrl(value)) {
                            errors.put(fieldName, "Invalid URL format");
                        }
                        break;
                    case "integer":
                        if (!isInteger(value)) {
                            errors.put(fieldName, "Must be a valid integer");
                        }
                        break;
                    case "double":
                        if (!isDouble(value)) {
                            errors.put(fieldName, "Must be a valid number");
                        }
                        break;
                }
            }
        }
        
        return errors;
    }
}
