package com.taskmanager.auth;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.service.EmailService;
import com.taskmanager.util.AuthUtil;
import com.taskmanager.util.ValidationUtil;

/**
 * Servlet implementation class PasswordResetServlet
 * Handles password reset functionality
 */
@WebServlet("/password-reset")
public class PasswordResetServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private AuthUtil authUtil;
    private ValidationUtil validationUtil;
    private EmailService emailService;
    
    public void init() {
        userDAO = new UserDAO();
        authUtil = new AuthUtil();
        validationUtil = new ValidationUtil();
        emailService = new EmailService();
    }
    
    /**
     * Display password reset request page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if this is a reset token verification
        String token = request.getParameter("token");
        
        if (token != null && !token.isEmpty()) {
            // Verify token and show password reset form
            try {
                User user = userDAO.findByResetToken(token);
                
                if (user != null && authUtil.isValidResetToken(token)) {
                    request.setAttribute("token", token);
                    request.getRequestDispatcher("/reset-password-form.jsp").forward(request, response);
                    return;
                } else {
                    request.setAttribute("errorMessage", "Invalid or expired password reset link");
                }
            } catch (SQLException e) {
                System.out.println("Database error verifying reset token: " + e.getMessage());
                request.setAttribute("errorMessage", "An error occurred while processing your request");
            }
        }
        
        // Show the initial password reset request form
        request.getRequestDispatcher("/reset-password-request.jsp").forward(request, response);
    }

    /**
     * Process password reset request or new password submission
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("requestReset".equals(action)) {
            processResetRequest(request, response);
        } else if ("resetPassword".equals(action)) {
            processNewPassword(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
    
    /**
     * Process the initial password reset request
     */
    private void processResetRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        
        if (!validationUtil.isValidEmail(email)) {
            request.setAttribute("emailError", "Please enter a valid email address");
            request.getRequestDispatcher("/reset-password-request.jsp").forward(request, response);
            return;
        }
        
        try {
            // Find user by email
            User user = userDAO.findByEmail(email);
            
            if (user != null) {
                // Generate reset token
                String resetToken = UUID.randomUUID().toString();
                user.setResetToken(resetToken);
                user.setResetTokenExpiry(new java.util.Date(System.currentTimeMillis() + 24*60*60*1000)); // 24 hours
                userDAO.update(user);
                
                // Send reset email
                String resetLink = request.getScheme() + "://" + request.getServerName() + ":" + 
                                  request.getServerPort() + request.getContextPath() + 
                                  "/password-reset?token=" + resetToken;
                
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            }
            
            // Always show success message even if email not found (security best practice)
            request.setAttribute("successMessage", "If the email address exists in our system, you will receive password reset instructions shortly.");
            request.getRequestDispatcher("/reset-password-request.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.out.println("Error in password reset request: " + e.getMessage());
            request.setAttribute("errorMessage", "An error occurred while processing your request");
            request.getRequestDispatcher("/reset-password-request.jsp").forward(request, response);
        }
    }
    
    /**
     * Process the new password submission
     */
    private void processNewPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate inputs
        boolean hasErrors = false;
        
        if (!validationUtil.isValidPassword(newPassword)) {
            request.setAttribute("passwordError", "Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one number");
            hasErrors = true;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("confirmPasswordError", "Passwords do not match");
            hasErrors = true;
        }
        
        try {
            // Verify token and find user
            User user = userDAO.findByResetToken(token);
            
            if (user == null || !authUtil.isValidResetToken(token)) {
                request.setAttribute("errorMessage", "Invalid or expired password reset link");
                request.getRequestDispatcher("/reset-password-request.jsp").forward(request, response);
                return;
            }
            
            if (hasErrors) {
                request.setAttribute("token", token);
                request.getRequestDispatcher("/reset-password-form.jsp").forward(request, response);
                return;
            }
            
            // Update password and clear reset token
            user.setPassword(authUtil.hashPassword(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userDAO.update(user);
            
            // Redirect to login page with success message
            request.getSession().setAttribute("resetSuccess", "Your password has been reset successfully. Please log in with your new password.");
            response.sendRedirect(request.getContextPath() + "/login");
            
        } catch (SQLException e) {
            System.out.println("Database error in password reset: " + e.getMessage());
            request.setAttribute("errorMessage", "An error occurred while resetting your password");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/reset-password-form.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("Error in password reset: " + e.getMessage());
            request.setAttribute("errorMessage", "An error occurred while resetting your password");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/reset-password-form.jsp").forward(request, response);
        }
    }
}