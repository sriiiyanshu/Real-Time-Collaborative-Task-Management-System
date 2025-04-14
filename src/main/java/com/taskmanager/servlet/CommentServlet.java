package com.taskmanager.servlet;

import com.taskmanager.service.CommentService;
import com.taskmanager.model.User;
import com.taskmanager.model.Comment;
import com.taskmanager.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Servlet responsible for handling comment-related operations.
 * Provides endpoints for creating, retrieving, updating, and deleting comments.
 */
@WebServlet("/api/comments/*")
public class CommentServlet extends HttpServlet {
    
    private CommentService commentService;
    
    @Override
    public void init() {
        commentService = new CommentService();
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
                // Get all comments for a task
                String taskId = request.getParameter("taskId");
                if (taskId == null || taskId.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID is required");
                    return;
                }
                
                List<Comment> commentsList = commentService.getCommentsByTaskId(Integer.parseInt(taskId));
                JSONArray comments = new JSONArray();
                for (Comment comment : commentsList) {
                    comments.put(new JSONObject(JsonUtil.toJson(comment)));
                }
                out.print(comments.toString());
            } else {
                // Get specific comment by ID
                String commentId = pathInfo.substring(1);
                Comment comment = commentService.getCommentById(Integer.parseInt(commentId));
                
                if (comment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comment not found");
                    return;
                }
                
                out.print(new JSONObject(JsonUtil.toJson(comment)).toString());
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }
        
        // Read request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        JSONObject commentData = new JSONObject(sb.toString());
        
        try {
            commentData.put("userId", currentUser.getId());
            Comment comment = commentService.createComment(commentData);
            
            // Convert Comment to JSONObject
            JSONObject result = new JSONObject(JsonUtil.toJson(comment));
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(result.toString());
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comment ID is required");
            return;
        }
        
        String commentId = pathInfo.substring(1);
        
        // Read request body
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        JSONObject commentData = new JSONObject(sb.toString());
        String content = commentData.getString("content");
        
        try {
            // Call updateComment with the extracted parameters
            boolean updated = commentService.updateComment(
                Integer.parseInt(commentId), 
                content, 
                currentUser.getId()
            );
            
            if (!updated) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comment not found or you don't have permission to edit it");
                return;
            }
            
            // Get the updated comment
            Comment updatedComment = commentService.getCommentById(Integer.parseInt(commentId));
            JSONObject result = new JSONObject(JsonUtil.toJson(updatedComment));
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(result.toString());
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
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comment ID is required");
            return;
        }
        
        String commentId = pathInfo.substring(1);
        
        try {
            boolean deleted = commentService.deleteComment(Integer.parseInt(commentId), currentUser.getId());
            
            if (!deleted) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comment not found or you don't have permission to delete it");
                return;
            }
            
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}