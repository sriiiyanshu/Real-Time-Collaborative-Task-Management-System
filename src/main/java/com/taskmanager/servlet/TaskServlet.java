package com.taskmanager.servlet;

import com.taskmanager.model.Comment;
import com.taskmanager.model.Project;
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
@WebServlet({"/task/*", "/task", "/api/tasks/*"})
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

        // Check if this is a JSP form request or an API request
        String action = request.getParameter("action");
        String acceptHeader = request.getHeader("Accept");
        boolean isApiRequest = acceptHeader != null && acceptHeader.contains("application/json");
        
        // Handle JSP requests
        if (!isApiRequest && (action != null || request.getParameter("id") != null)) {
            try {
                // Set up common attributes for task forms and views
                String[] taskStatuses = {"To Do", "In Progress", "Under Review", "Completed"};
                String[] taskPriorities = {"Low", "Medium", "High", "Critical"};
                request.setAttribute("taskStatuses", taskStatuses);
                request.setAttribute("taskPriorities", taskPriorities);
                
                // Load user projects for project selection
                List<Project> userProjects = getProjectService().getUserProjects(currentUser.getId());
                request.setAttribute("userProjects", userProjects);
                
                // Handle specific actions
                if ("new".equals(action)) {
                    // New task form
                    String projectIdParam = request.getParameter("projectId");
                    if (projectIdParam != null && !projectIdParam.isEmpty()) {
                        int projectId = Integer.parseInt(projectIdParam);
                        Project project = getProjectService().getProjectById(projectId);
                        
                        if (project != null) {
                            // Get project members for assignee selection
                            List<User> projectMembers = getProjectService().getProjectMembers(projectId);
                            request.setAttribute("projectMembers", projectMembers);
                            request.setAttribute("project", project);
                        }
                    } else {
                        // No project selected, load all users for assignee dropdown
                        List<User> allUsers = getUserDAO().findAll();
                        request.setAttribute("projectMembers", allUsers);
                    }
                    
                    // Forward to the task.jsp form
                    request.getRequestDispatcher("/task.jsp").forward(request, response);
                    return;
                } else if ("edit".equals(action)) {
                    // Edit task form
                    String taskIdStr = request.getParameter("id");
                    if (taskIdStr != null && !taskIdStr.isEmpty()) {
                        int taskId = Integer.parseInt(taskIdStr);
                        Task task = taskService.getTaskById(taskId);
                        
                        if (task != null) {
                            request.setAttribute("task", task);
                            
                            // Get project members for assignee selection
                            if (task.getProjectId() != null) {
                                List<User> projectMembers = getProjectService().getProjectMembers(task.getProjectId());
                                request.setAttribute("projectMembers", projectMembers);
                            } else {
                                // No project associated, load all users
                                List<User> allUsers = getUserDAO().findAll();
                                request.setAttribute("projectMembers", allUsers);
                            }
                            
                            // Forward to the task.jsp form
                            request.getRequestDispatcher("/task.jsp").forward(request, response);
                            return;
                        }
                    }
                } else if (request.getParameter("id") != null) {
                    // Task detail view
                    String taskIdStr = request.getParameter("id");
                    int taskId = Integer.parseInt(taskIdStr);
                    Task task = taskService.getTaskById(taskId);
                    
                    if (task != null) {
                        // Load task details
                        request.setAttribute("task", task);
                        
                        // Get assignee details
                        if (task.getAssigneeId() != null) {
                            User assignee = getUserDAO().findById(task.getAssigneeId());
                            request.setAttribute("assignee", assignee);
                        }
                        
                        // Get project details
                        if (task.getProjectId() != null) {
                            Project project = getProjectService().getProjectById(task.getProjectId());
                            request.setAttribute("project", project);
                        }
                        
                        // Get task comments
                        List<Comment> comments = getCommentService().getCommentsByTask(taskId);
                        request.setAttribute("taskComments", comments);
                        
                        // Forward to the task.jsp view
                        request.getRequestDispatcher("/task.jsp").forward(request, response);
                        return;
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                        return;
                    }
                } else {
                    // Task list view
                    List<Task> userTasks = taskService.getTasksByAssignee(currentUser.getId());
                    request.setAttribute("userTasks", userTasks);
                    request.setAttribute("now", new Date());
                    
                    // Forward to the task.jsp list view
                    request.getRequestDispatcher("/task.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                request.setAttribute("errorMessage", "Error loading task data: " + e.getMessage());
                request.getRequestDispatcher("/task.jsp").forward(request, response);
                return;
            }
        }
        
        // Handle API requests
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
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
        
        // Check if this is a time logging request
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/log-time")) {
            try {
                // Extract form parameters for time logging
                String taskIdStr = request.getParameter("taskId");
                String hoursStr = request.getParameter("hours");
                
                if (taskIdStr == null || hoursStr == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                    return;
                }
                
                Integer taskId = Integer.parseInt(taskIdStr);
                Double hours = Double.parseDouble(hoursStr);
                
                // Get the task
                Task task = taskService.getTaskById(taskId);
                if (task == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                    return;
                }
                
                // Log time for the task
                boolean success = taskService.logTimeForTask(taskId, currentUser.getId(), hours);
                
                if (success) {
                    // Redirect back to task detail page
                    response.sendRedirect(request.getContextPath() + "/task?id=" + taskId);
                } else {
                    request.setAttribute("errorMessage", "Failed to log time for task");
                    request.getRequestDispatcher("/task?id=" + taskId).forward(request, response);
                }
                return;
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format: " + e.getMessage());
                return;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error logging time: " + e.getMessage());
                return;
            }
        }
        
        // Check Content-Type to determine how to process the request
        String contentType = request.getContentType();
        
        // Handle form submissions from JSP
        String action = request.getParameter("action");
        if (action != null) {
            if (action.equals("new") || action.equals("edit")) {
                try {
                    // Extract form parameters
                    String title = request.getParameter("title");
                    String description = request.getParameter("description");
                    
                    // Parse projectId
                    Integer projectId = null;
                    String projectIdStr = request.getParameter("projectId");
                    if (projectIdStr != null && !projectIdStr.isEmpty()) {
                        projectId = Integer.parseInt(projectIdStr);
                    }
                    
                    // Parse assigneeId
                    Integer assigneeId = null;
                    String assigneeIdStr = request.getParameter("assigneeId");
                    if (assigneeIdStr != null && !assigneeIdStr.isEmpty()) {
                        assigneeId = Integer.parseInt(assigneeIdStr);
                    }
                    
                    // Parse dueDate
                    Date dueDate = null;
                    String dueDateStr = request.getParameter("dueDate");
                    if (dueDateStr != null && !dueDateStr.isEmpty()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        dueDate = dateFormat.parse(dueDateStr);
                    }
                    
                    // Get status and priority
                    String status = request.getParameter("status");
                    String priority = request.getParameter("priority");
                    
                    // Parse estimated hours
                    Double estimatedHours = 0.0;
                    String estimatedHoursStr = request.getParameter("estimatedHours");
                    if (estimatedHoursStr != null && !estimatedHoursStr.isEmpty()) {
                        estimatedHours = Double.parseDouble(estimatedHoursStr);
                    }
                    
                    Task task;
                    if (action.equals("edit")) {
                        // Update existing task
                        Integer taskId = Integer.parseInt(request.getParameter("id"));
                        task = taskService.getTaskById(taskId);
                        
                        if (task == null) {
                            request.setAttribute("errorMessage", "Task not found");
                            request.getRequestDispatcher("/task.jsp?action=edit&id=" + taskId).forward(request, response);
                            return;
                        }
                        
                        task.setTitle(title);
                        task.setDescription(description);
                        task.setProjectId(projectId);
                        task.setAssigneeId(assigneeId);
                        task.setDueDate(dueDate);
                        task.setStatus(status);
                        task.setPriority(priority);
                        task.setEstimatedHours(estimatedHours);
                        
                        boolean updated = taskService.updateTask(task, currentUser.getId());
                        if (!updated) {
                            request.setAttribute("errorMessage", "Failed to update task");
                            request.getRequestDispatcher("/task.jsp?action=edit&id=" + taskId).forward(request, response);
                            return;
                        }
                    } else {
                        // Create new task
                        task = taskService.createTask(
                            title,
                            description,
                            projectId,
                            currentUser.getId(),
                            assigneeId,
                            dueDate,
                            priority
                        );
                        
                        if (task == null) {
                            request.setAttribute("errorMessage", "Failed to create task");
                            request.getRequestDispatcher("/task.jsp?action=new").forward(request, response);
                            return;
                        }
                        
                        // Set estimated hours if provided
                        if (estimatedHours > 0) {
                            task.setEstimatedHours(estimatedHours);
                            taskService.updateTask(task, currentUser.getId());
                        }
                    }
                    
                    // Process subtasks if any
                    String[] subtaskIds = request.getParameterValues("subtaskId");
                    String[] subtaskTitles = request.getParameterValues("subtaskTitle");
                    String[] subtaskStatuses = request.getParameterValues("subtaskStatus");
                    
                    if (subtaskTitles != null && subtaskTitles.length > 0) {
                        // Handle subtask creation/updates
                        taskService.updateSubtasks(task.getId(), subtaskIds, subtaskTitles, subtaskStatuses);
                    }
                    
                    // Redirect to task detail view
                    response.sendRedirect(request.getContextPath() + "/task?id=" + task.getId());
                    return;
                } catch (Exception e) {
                    request.setAttribute("errorMessage", "Error processing task: " + e.getMessage());
                    request.getRequestDispatcher("/task.jsp?action=" + action).forward(request, response);
                    return;
                }
            }
        }
        
        // Handle API JSON requests (existing functionality)
        try {
            // Read request body
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
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
    
    // Helper methods to access other services
    
    private com.taskmanager.service.ProjectService projectService;
    private com.taskmanager.dao.UserDAO userDAO;
    private com.taskmanager.service.CommentService commentService;
    
    private com.taskmanager.service.ProjectService getProjectService() {
        if (projectService == null) {
            projectService = new com.taskmanager.service.ProjectService();
        }
        return projectService;
    }
    
    private com.taskmanager.dao.UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new com.taskmanager.dao.UserDAO();
        }
        return userDAO;
    }
    
    private com.taskmanager.service.CommentService getCommentService() {
        if (commentService == null) {
            commentService = new com.taskmanager.service.CommentService();
        }
        return commentService;
    }
}