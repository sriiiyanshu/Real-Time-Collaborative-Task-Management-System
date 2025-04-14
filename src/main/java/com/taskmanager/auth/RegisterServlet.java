package com.taskmanager.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.AuthUtil;
import com.taskmanager.util.ValidationUtil;

/**
 * Servlet implementation class RegisterServlet
 * Handles user registration functionality
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private AuthUtil authUtil;
    private ValidationUtil validationUtil;
       
    public void init() {
        userDAO = new UserDAO();
        authUtil = new AuthUtil();
        validationUtil = new ValidationUtil();
    }

    /**
     * Display registration page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    /**
     * Process registration form submission
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName = request.getParameter("fullName");
        
        // Form validation
        boolean hasErrors = false;
        
        if (!validationUtil.isValidUsername(username)) {
            request.setAttribute("usernameError", "Username must be 4-20 characters and contain only letters, numbers, and underscores");
            hasErrors = true;
        }
        
        if (!validationUtil.isValidEmail(email)) {
            request.setAttribute("emailError", "Please enter a valid email address");
            hasErrors = true;
        }
        
        if (!validationUtil.isValidPassword(password)) {
            request.setAttribute("passwordError", "Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one number");
            hasErrors = true;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("confirmPasswordError", "Passwords do not match");
            hasErrors = true;
        }
        
        try {
            // Check if username or email already exists
            if (userDAO.findByUsername(username) != null) {
                request.setAttribute("usernameError", "Username already exists");
                hasErrors = true;
            }
            
            if (userDAO.findByEmail(email) != null) {
                request.setAttribute("emailError", "Email already registered");
                hasErrors = true;
            }
            
            if (hasErrors) {
                // Preserve form data for re-display
                request.setAttribute("username", username);
                request.setAttribute("email", email);
                request.setAttribute("fullName", fullName);
                
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            newUser.setPassword(authUtil.hashPassword(password)); // Hash the password
            newUser.setRegistrationDate(new java.util.Date());
            newUser.setActive(true);
            
            // Save user to database
            userDAO.insert(newUser);
            
            // Redirect to login page with success message
            request.getSession().setAttribute("registrationSuccess", "Registration successful! Please log in.");
            response.sendRedirect(request.getContextPath() + "/login");
            
        } catch (Exception e) {
            System.out.println("Error in RegisterServlet: " + e.getMessage());
            request.setAttribute("errorMessage", "An error occurred during registration");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}