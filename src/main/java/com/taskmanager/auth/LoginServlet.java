package com.taskmanager.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.AuthUtil;

/**
 * Servlet implementation class LoginServlet
 * Handles user login functionality
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private AuthUtil authUtil;
    
    public void init() {
        userDAO = new UserDAO();
        authUtil = new AuthUtil();
    }
    
    /**
     * Display login page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // User is already logged in, redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Forward to the login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /**
     * Process login form submission
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        try {
            // Validate login credentials
            User user = userDAO.findByUsername(username);
            
            if (user != null && authUtil.verifyPassword(password, user.getPassword())) {
                // Create session
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                
                // Handle "remember me" functionality
                if (rememberMe != null && rememberMe.equals("on")) {
                    authUtil.setRememberMeCookie(response, username);
                }
                
                // Log successful login
                System.out.println("User logged in: " + username);
                
                // Redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                // Invalid credentials
                request.setAttribute("errorMessage", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.out.println("Error in LoginServlet: " + e.getMessage());
            request.setAttribute("errorMessage", "An error occurred during login");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}