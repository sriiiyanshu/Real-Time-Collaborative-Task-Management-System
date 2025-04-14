package com.taskmanager.servlet;

import com.taskmanager.model.User;
import com.taskmanager.service.SearchService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;

/**
 * Servlet responsible for handling search operations.
 * Provides endpoints for searching across tasks, projects, users, and comments.
 */
@WebServlet("/api/search/*")
public class SearchServlet extends HttpServlet {
    
    private SearchService searchService;
    
    @Override
    public void init() {
        searchService = new SearchService();
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
        
        String query = request.getParameter("query");
        if (query == null || query.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Search query is required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject results = new JSONObject();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Global search across all entities
                results = searchService.searchAll(query, currentUser.getId());
            } else if (pathInfo.equals("/tasks")) {
                // Search only tasks
                results.put("tasks", searchService.searchTasks(query, currentUser.getId()));
            } else if (pathInfo.equals("/projects")) {
                // Search only projects
                results.put("projects", searchService.searchProjects(query, currentUser.getId()));
            } else if (pathInfo.equals("/users")) {
                // Search only users - Check if user is admin
                boolean isAdmin = currentUser.isAdmin(); // Assuming User class has isAdmin() method
                results.put("users", searchService.searchUsers(query, isAdmin));
            } else if (pathInfo.equals("/comments")) {
                // Search only comments
                results.put("comments", searchService.searchComments(query, currentUser.getId()));
            } else if (pathInfo.equals("/files")) {
                // Search only files
                results.put("files", searchService.searchFiles(query, currentUser.getId()));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Search endpoint not found");
                return;
            }
            
            out.print(results.toString());
            
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing search: " + e.getMessage());
        }
    }
}