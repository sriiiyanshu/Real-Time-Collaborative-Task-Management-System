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
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String terms = request.getParameter("terms");
        
        // Form validation
        boolean hasErrors = false;
        
        if (firstName == null || firstName.trim().isEmpty()) {
            request.setAttribute("firstNameError", "First name is required");
            hasErrors = true;
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            request.setAttribute("lastNameError", "Last name is required");
            hasErrors = true;
        }
        
        if (!validationUtil.isValidEmail(email)) {
            request.setAttribute("emailError", "Please enter a valid email address");
            hasErrors = true;
        }
        
        if (!validationUtil.isValidPassword(password)) {
            request.setAttribute("passwordError", "Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, one number, and one special character");
            hasErrors = true;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("confirmPasswordError", "Passwords do not match");
            hasErrors = true;
        }
        
        if (terms == null) {
            request.setAttribute("termsError", "You must agree to the Terms of Service");
            hasErrors = true;
        }
        
        try {
            // Check if email already exists
            if (userDAO.findByEmail(email) != null) {
                request.setAttribute("emailError", "Email already registered");
                hasErrors = true;
            }
            
            if (hasErrors) {
                // Preserve form data for re-display
                request.setAttribute("firstName", firstName);
                request.setAttribute("lastName", lastName);
                request.setAttribute("email", email);
                
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
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
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred during registration: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}