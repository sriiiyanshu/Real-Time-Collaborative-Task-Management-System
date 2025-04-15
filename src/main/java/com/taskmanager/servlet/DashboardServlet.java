package com.taskmanager.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.taskmanager.dao.NotificationDAO;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Notification;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;

/**
 * DashboardServlet handles requests to the dashboard page
 * Loads and displays dashboard data for the logged-in user
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private NotificationDAO notificationDAO;
    
    /**
     * Initialize DAOs for data access
     */
    public void init() {
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        notificationDAO = new NotificationDAO();
    }
    
    /**
     * Handle GET requests to the dashboard
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get the current session
        HttpSession session = request.getSession(false);
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            // User is not logged in, redirect to login page
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        try {
            // Get the logged-in user
            User user = (User) session.getAttribute("user");
            Integer userId = user.getId();
            
            // Set current date for the dashboard
            request.setAttribute("now", new Date());
            
            // Load dashboard statistics
            int tasksDueToday = taskDAO.countTasksDueToday(userId.longValue());
            int tasksOverdue = taskDAO.countOverdueTasks(userId.longValue());
            int projectsInProgress = projectDAO.countActiveProjects(userId.longValue());
            int tasksCompleted = taskDAO.countCompletedTasks(userId.longValue());
            
            // Set dashboard statistics
            request.setAttribute("tasksDueToday", tasksDueToday);
            request.setAttribute("tasksOverdue", tasksOverdue);
            request.setAttribute("projectsInProgress", projectsInProgress);
            request.setAttribute("tasksCompleted", tasksCompleted);
            
            // Load recent tasks
            List<Task> recentTasks = taskDAO.findRecentTasks(userId.longValue(), 5);
            request.setAttribute("recentTasks", recentTasks);
            
            // Load active projects
            List<Project> activeProjects = projectDAO.findActiveProjects(userId.longValue(), 3);
            request.setAttribute("activeProjects", activeProjects);
            
            // Load recent notifications
            List<Notification> recentNotifications = notificationDAO.findRecentNotifications(userId.longValue(), 5);
            request.setAttribute("recentNotifications", recentNotifications);
            
            // Load upcoming deadlines
            List<Task> upcomingDeadlines = taskDAO.findUpcomingDeadlines(userId.longValue(), 5);
            request.setAttribute("upcomingDeadlines", upcomingDeadlines);
            
            // Forward to the dashboard JSP page
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.out.println("Error in DashboardServlet: " + e.getMessage());
            e.printStackTrace();
            // Handle error and show error page
            request.setAttribute("errorMessage", "Failed to load dashboard data");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}