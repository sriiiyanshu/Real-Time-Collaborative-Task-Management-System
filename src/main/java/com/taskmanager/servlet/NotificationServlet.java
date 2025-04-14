package com.taskmanager.servlet;

import com.taskmanager.model.User;
import com.taskmanager.service.NotificationService;
import com.taskmanager.model.Notification;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Servlet responsible for handling notification-related operations.
 * Provides endpoints for retrieving, marking notifications as read, and managing user notifications.
 */
@WebServlet("/api/notifications/*")
public class NotificationServlet extends HttpServlet {
    
    private NotificationService notificationService;
    
    @Override
    public void init() {
        notificationService = new NotificationService();
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
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all notifications for current user
                boolean unreadOnly = "true".equals(request.getParameter("unreadOnly"));
                int limit = 10;
                try {
                    String limitParam = request.getParameter("limit");
                    if (limitParam != null) {
                        limit = Integer.parseInt(limitParam);
                    }
                } catch (NumberFormatException e) {
                    // Use default limit
                }
                
                // Use appropriate service method based on parameters
                JSONArray notifications = new JSONArray();
                List<Notification> notificationList;
                if (unreadOnly) {
                    // If only unread notifications are requested, we'll need to filter them
                    notificationList = notificationService.getUserNotifications(currentUser.getId());
                    notificationList = notificationList.stream()
                        .filter(n -> !n.isRead())
                        .limit(limit)
                        .collect(Collectors.toList());
                } else {
                    // Otherwise use the getRecentNotifications method which has limit parameter
                    notificationList = notificationService.getRecentNotifications(currentUser.getId(), limit);
                }
                
                // Convert to JSONArray
                for (Notification notification : notificationList) {
                    JSONObject json = new JSONObject();
                    json.put("id", notification.getId());
                    json.put("message", notification.getMessage());
                    json.put("type", notification.getType());
                    json.put("read", notification.isRead());
                    json.put("creationDate", notification.getCreationDate().toString());
                    json.put("link", notification.getLink());
                    if (notification.getRelatedId() != null) {
                        json.put("relatedId", notification.getRelatedId());
                    }
                    notifications.put(json);
                }
                
                out.print(notifications.toString());
            } else if (pathInfo.equals("/count")) {
                // Get unread notification count
                int count = notificationService.getUnreadNotificationCount(currentUser.getId());
                JSONObject result = new JSONObject();
                result.put("count", count);
                out.print(result.toString());
            } else {
                // Get specific notification
                String notificationId = pathInfo.substring(1);
                Notification notification = notificationService.findById(Integer.parseInt(notificationId));
                
                if (notification == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Notification not found");
                    return;
                }
                
                // Check if user has access to this notification
                if (!notification.getUserId().equals(currentUser.getId())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                    return;
                }
                
                // Convert to JSON
                JSONObject json = new JSONObject();
                json.put("id", notification.getId());
                json.put("message", notification.getMessage());
                json.put("type", notification.getType());
                json.put("read", notification.isRead());
                json.put("creationDate", notification.getCreationDate().toString());
                json.put("link", notification.getLink());
                if (notification.getRelatedId() != null) {
                    json.put("relatedId", notification.getRelatedId());
                }
                json.put("userId", notification.getUserId());
                
                out.print(json.toString());
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Notification ID is required");
            return;
        }
        
        // Read request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        try {
            JSONObject requestData = new JSONObject(sb.toString());
            String action = requestData.optString("action");
            long notificationId = Long.parseLong(pathInfo.substring(1));
            
            boolean success = false;
            if ("markAsRead".equals(action)) {
                success = notificationService.markAsRead(Integer.valueOf((int)notificationId));
            } else if ("markAsUnread".equals(action)) {
                // Implement unmark functionality by retrieving notification and updating its read status
                Notification notification = notificationService.findById(Integer.valueOf((int)notificationId));
                if (notification != null && notification.getUserId().equals(currentUser.getId())) {
                    notification.setRead(false);
                    success = true;
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }
            
            if (success) {
                JSONObject notification = notificationService.getNotificationById(notificationId);
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.print(notification.toString());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Notification not found or access denied");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid notification ID");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            boolean success = false;
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Delete all notifications for user
                success = notificationService.deleteAllUserNotifications(currentUser.getId());
            } else {
                // Delete specific notification
                long notificationId = Long.parseLong(pathInfo.substring(1));
                success = notificationService.deleteNotification((int) notificationId);
            }
            
            if (success) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Notification(s) not found or access denied");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid notification ID");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}