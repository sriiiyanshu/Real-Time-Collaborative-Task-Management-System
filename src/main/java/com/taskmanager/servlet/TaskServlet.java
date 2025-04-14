package com.taskmanager.servlet;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import com.taskmanager.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Servlet responsible for handling task-related operations.
 * Provides endpoints for creating, retrieving, updating, and deleting tasks.
 */
@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {
    
    private TaskService taskService;
    private JsonUtil jsonUtil;
    
    @Override
    public void init() {
        taskService = new TaskService();
        jsonUtil = new JsonUtil();
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
                // Handle different listing options
                String mode = request.getParameter("mode");
                String projectIdStr = request.getParameter("projectId");
                String statusFilter = request.getParameter("status");
                
                List<Task> tasks = null;
                
                if (mode != null) {
                    if (mode.equals("assignedToMe")) {
                        tasks = taskService.getTasksByAssignee(currentUser.getId());
                    } else if (mode.equals("createdByMe")) {
                        tasks = taskService.getTasksByCreator(currentUser.getId());
                    } else if (mode.equals("overdue")) {
                        tasks = taskService.getOverdueTasks();
                    }
                } else if (projectIdStr != null) {
                    Integer projectId = Integer.parseInt(projectIdStr);
                    
                    if (statusFilter != null) {
                        tasks = taskService.getTasksByStatus(statusFilter, projectId);
                    } else {
                        tasks = taskService.getTasksByProject(projectId);
                    }
                } else {
                    // Default: return tasks assigned to current user
                    tasks = taskService.getTasksByAssignee(currentUser.getId());
                }
                
                JSONArray result = new JSONArray();
                if (tasks != null) {
                    for (Task task : tasks) {
                        result.put(convertTaskToJson(task));
                    }
                }
                
                out.print(result.toString());
            } else {
                // Get specific task by ID
                String taskIdStr = pathInfo.substring(1);
                Integer taskId = Integer.parseInt(taskIdStr);
                
                Task task = taskService.getTaskById(taskId);
                
                if (task == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                    return;
                }
                
                JSONObject taskJson = convertTaskToJson(task);
                out.print(taskJson.toString());
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
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
        
        try {
            JSONObject taskData = new JSONObject(sb.toString());
            
            // Parse task data
            String title = taskData.getString("title");
            String description = taskData.optString("description", null);
            Integer projectId = taskData.getInt("projectId");
            Integer assigneeId = taskData.optInt("assigneeId", 0);
            if (assigneeId == 0) assigneeId = null;
            
            // Parse due date if present
            Date dueDate = null;
            if (taskData.has("dueDate") && !taskData.isNull("dueDate")) {
                String dueDateStr = taskData.getString("dueDate");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dueDate = dateFormat.parse(dueDateStr);
            }
            
            String priority = taskData.optString("priority", "Medium");
            
            // Create the task
            Task newTask = taskService.createTask(
                title,
                description,
                projectId,
                currentUser.getId(),  // creator
                assigneeId,
                dueDate,
                priority
            );
            
            if (newTask != null) {
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                JSONObject result = convertTaskToJson(newTask);
                out.print(result.toString());
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create task");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error creating task: " + e.getMessage());
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID is required");
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
            // Extract task ID from path
            Integer taskId = Integer.parseInt(pathInfo.substring(1));
            JSONObject taskData = new JSONObject(sb.toString());
            
            // Check if it's a status-only update
            if (taskData.has("action") && taskData.getString("action").equals("updateStatus")) {
                String newStatus = taskData.getString("status");
                boolean updated = taskService.updateTaskStatus(taskId, newStatus, currentUser.getId());
                
                if (updated) {
                    Task updatedTask = taskService.getTaskById(taskId);
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    out.print(convertTaskToJson(updatedTask).toString());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found or update failed");
                }
                return;
            }
            
            // Full task update
            Task existingTask = taskService.getTaskById(taskId);
            if (existingTask == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                return;
            }
            
            // Update task properties
            if (taskData.has("title")) existingTask.setTitle(taskData.getString("title"));
            if (taskData.has("description")) existingTask.setDescription(taskData.optString("description", null));
            if (taskData.has("assigneeId")) {
                Integer assigneeId = taskData.optInt("assigneeId", 0);
                existingTask.setAssigneeId(assigneeId == 0 ? null : assigneeId);
            }
            if (taskData.has("status")) existingTask.setStatus(taskData.getString("status"));
            if (taskData.has("priority")) existingTask.setPriority(taskData.getString("priority"));
            
            // Update due date if present
            if (taskData.has("dueDate") && !taskData.isNull("dueDate")) {
                String dueDateStr = taskData.getString("dueDate");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                existingTask.setDueDate(dateFormat.parse(dueDateStr));
            } else if (taskData.has("dueDate") && taskData.isNull("dueDate")) {
                existingTask.setDueDate(null);
            }
            
            boolean updated = taskService.updateTask(existingTask, currentUser.getId());
            
            if (updated) {
                Task updatedTask = taskService.getTaskById(taskId);
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                out.print(convertTaskToJson(updatedTask).toString());
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update task");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error updating task: " + e.getMessage());
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID is required");
            return;
        }
        
        try {
            Integer taskId = Integer.parseInt(pathInfo.substring(1));
            
            // Get the task to check permissions
            Task task = taskService.getTaskById(taskId);
            
            if (task == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                return;
            }
            
            // Only allow creator or assignee to delete task
            if (!task.getCreatorId().equals(currentUser.getId()) && 
                !(task.getAssigneeId() != null && task.getAssigneeId().equals(currentUser.getId()))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to delete this task");
                return;
            }
            
            boolean deleted = taskService.deleteTask(taskId);
            
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete task");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting task: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to convert Task object to JSON
     */
    private JSONObject convertTaskToJson(Task task) {
        JSONObject json = new JSONObject();
        
        json.put("id", task.getId());
        json.put("title", task.getTitle());
        json.put("description", task.getDescription() != null ? task.getDescription() : "");
        json.put("status", task.getStatus());
        json.put("priority", task.getPriority());
        json.put("projectId", task.getProjectId());
        json.put("creatorId", task.getCreatorId());
        
        // Handle optional fields
        if (task.getAssigneeId() != null) {
            json.put("assigneeId", task.getAssigneeId());
        }
        
        if (task.getDueDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            json.put("dueDate", dateFormat.format(task.getDueDate()));
        }
        
        if (task.getCreationDate() != null) {
            json.put("creationDate", task.getCreationDate().getTime());
        }
        
        if (task.getCompletionDate() != null) {
            json.put("completionDate", task.getCompletionDate().getTime());
        }
        
        return json;
    }
}