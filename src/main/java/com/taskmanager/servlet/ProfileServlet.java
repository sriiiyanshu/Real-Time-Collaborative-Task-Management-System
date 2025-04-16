package com.taskmanager.servlet;

import com.taskmanager.exception.DAOException;
import com.taskmanager.model.User;
import com.taskmanager.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Fetch user statistics
            Map<String, Integer> userStats = getUserStatistics(user.getId());
            request.setAttribute("userStats", userStats);
            
            // Fetch user activity
            // In a real implementation, you would fetch user activity from a service
            request.setAttribute("userActivity", userService.getUserActivity(user.getId()));
            
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
        } catch (DAOException e) {
            // Log the exception
            getServletContext().log("Error retrieving user profile data", e);
            request.setAttribute("errorMessage", "Failed to load profile data. Please try again later.");
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle profile updates
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            switch (action) {
                case "updateProfile":
                    updateUserProfile(request, user);
                    request.setAttribute("successMessage", "Profile updated successfully.");
                    break;
                case "changePassword":
                    changePassword(request, user);
                    request.setAttribute("successMessage", "Password changed successfully.");
                    break;
                default:
                    break;
            }
            
            // Refresh user data in session
            user = userService.getUserById(user.getId());
            session.setAttribute("user", user);
            
            // Redirect to prevent form resubmission
            response.sendRedirect(request.getContextPath() + "/profile?updated=true");
        } catch (Exception e) {
            // Log the exception
            getServletContext().log("Error updating user profile", e);
            request.setAttribute("errorMessage", "Failed to update profile. " + e.getMessage());
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
        }
    }
    
    private Map<String, Integer> getUserStatistics(int userId) throws DAOException {
        // In a real implementation, you would get this data from services
        Map<String, Integer> stats = new HashMap<>();
        
        // Example stats - replace with actual data from your services
        stats.put("taskCount", userService.countUserTasks(userId));
        stats.put("projectCount", userService.countUserProjects(userId));
        stats.put("teamCount", userService.countUserTeams(userId));
        
        return stats;
    }
    
    private void updateUserProfile(HttpServletRequest request, User user) throws DAOException {
        user.setFirstName(request.getParameter("firstName"));
        user.setLastName(request.getParameter("lastName"));
        user.setEmail(request.getParameter("email"));
        user.setJobTitle(request.getParameter("jobTitle"));
        user.setDepartment(request.getParameter("department"));
        user.setPhone(request.getParameter("phone"));
        
        userService.updateUser(user);
    }
    
    private void changePassword(HttpServletRequest request, User user) throws DAOException, SQLException {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        
        userService.changePassword(user.getId(), currentPassword, newPassword);
    }
}
