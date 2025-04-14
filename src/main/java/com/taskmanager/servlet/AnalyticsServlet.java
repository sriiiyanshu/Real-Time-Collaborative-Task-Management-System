package com.taskmanager.servlet;

import com.taskmanager.service.AnalyticsService;
import com.taskmanager.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.json.JSONObject;

/**
 * Servlet responsible for handling analytics-related requests.
 * Provides endpoints for retrieving various analytics data related to
 * projects, tasks, and user performance.
 */
@WebServlet("/api/analytics/*")
public class AnalyticsServlet extends HttpServlet {
    
    private AnalyticsService analyticsService;
    
    @Override
    public void init() {
        analyticsService = new AnalyticsService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            Map<String, Object> resultMap = null;
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Summary dashboard data
                resultMap = analyticsService.getDashboardSummary(currentUser.getId());
            } else if (pathInfo.equals("/projects")) {
                // Project analytics
                String projectId = request.getParameter("projectId");
                if (projectId != null && !projectId.isEmpty()) {
                    resultMap = analyticsService.getProjectAnalytics(Integer.parseInt(projectId));
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                    return;
                }
            } else if (pathInfo.equals("/tasks")) {
                // Task completion analytics
                String taskId = request.getParameter("taskId");
                if (taskId != null && !taskId.isEmpty()) {
                    resultMap = analyticsService.getTaskAnalytics(Integer.parseInt(taskId));
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID is required");
                    return;
                }
            } else if (pathInfo.equals("/team")) {
                // Team performance analytics
                String teamId = request.getParameter("teamId");
                if (teamId != null && !teamId.isEmpty()) {
                    resultMap = analyticsService.getTeamAnalytics(Integer.parseInt(teamId));
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Team ID is required");
                    return;
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Analytics endpoint not found");
                return;
            }
            
            // Convert Map to JSONObject
            JSONObject result = new JSONObject(resultMap);
            out.print(result.toString());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing analytics: " + e.getMessage());
        }
    }
}