package com.taskmanager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * User model class representing a system user
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String firstName;
    private String lastName;
    private Date registrationDate;
    private boolean active;
    private String role;
    private String resetToken;
    private Date resetTokenExpiry;
    private String profileImage;
    private String rememberToken;
    
    // Constructors
    
    public User() {
        // Default constructor
    }
    
    public User(Integer id, String username, String email, String password, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.registrationDate = new Date();
        this.active = true;
        this.role = "user"; // Default role
    }
    
    // Getters and setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        if (fullName == null && firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }
    
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }
    
    public Date getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getResetToken() {
        return resetToken;
    }
    
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
    
    public Date getResetTokenExpiry() {
        return resetTokenExpiry;
    }
    
    public void setResetTokenExpiry(Date resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    public String getRememberToken() {
        return rememberToken;
    }
    
    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
    }
    
    public String[] getRoles() {
        // Split the role string into an array if multiple roles are stored as comma-separated values
        if (role != null && !role.isEmpty()) {
            return role.split(",");
        }
        return new String[]{role};
    }
    
    /**
     * Check if user has admin privileges
     * @return true if user has admin role, false otherwise
     */
    public boolean isAdmin() {
        // Check if the role contains "admin" (case insensitive)
        if (role != null) {
            // For multiple roles separated by commas
            String[] roles = getRoles();
            for (String r : roles) {
                if (r.trim().equalsIgnoreCase("admin")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}