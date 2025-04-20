package com.taskmanager.service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.taskmanager.config.AppConfig;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;

/**
 * Service class for task-related business logic
 */
public class TaskService {
    
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    private EmailService emailService;
    
    public TaskService() {
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();
        notificationService = new NotificationService();
        emailService = new EmailService();
    }
    
    /**
     * Create a new task
     * @return Task object or null if creation failed
     */
    public Task createTask(String title, String description, Integer projectId, 
                           Integer creatorId, Integer assigneeId,
                           Date dueDate, String priority) throws SQLException {
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        
        // Check if project exists
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project does not exist");
        }
        
        // Create task
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setProjectId(projectId);
        task.setCreatorId(creatorId);
        task.setAssigneeId(assigneeId);
        task.setDueDate(dueDate);
        task.setPriority(priority != null ? priority : "Medium");
        task.setStatus("To Do");
        task.setCreationDate(new Date());
        
        // Save task to database
        Integer taskId = taskDAO.insert(task);
        if (taskId != null) {
            task.setId(taskId);
            
            // Notify assignee if not the creator
            if (assigneeId != null && !assigneeId.equals(creatorId)) {
                User assignee = userDAO.findById(assigneeId);
                if (assignee != null) {
                    // Create in-app notification
                    notificationService.notifyTaskAssignment(taskId, assigneeId, title, projectId);
                    
                    // Send email notification
                    String taskLink = AppConfig.getAppBaseUrl() + "/tasks/view/" + taskId;
                    emailService.sendTaskAssignmentEmail(
                            assignee.getEmail(), 
                            assignee.getFullName(),
                            title,
                            project.getName(),
                            taskLink);
                }
            }
            
            return task;
        }
        
        return null;
    }
    
    /**
     * Get a task by ID
     */
    public Task getTaskById(Integer taskId) throws SQLException {
        return taskDAO.findById(taskId);
    }
    
    /**
     * Get all tasks for a project
     */
    public List<Task> getTasksByProject(Integer projectId) throws SQLException {
        return taskDAO.findByProjectId(projectId);
    }
    
    /**
     * Get tasks assigned to a user
     */
    public List<Task> getTasksByAssignee(Integer assigneeId) throws SQLException {
        return taskDAO.findByAssigneeId(assigneeId);
    }
    
    /**
     * Get tasks created by a user
     */
    public List<Task> getTasksByCreator(Integer creatorId) throws SQLException {
        return taskDAO.findByCreatorId(creatorId);
    }
    
    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(String status, Integer projectId) throws SQLException {
        return taskDAO.findByStatus(status, projectId);
    }
    
    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks() throws SQLException {
        return taskDAO.findOverdueTasks();
    }
    
    /**
     * Update task details
     */
    public boolean updateTask(Task task, Integer updatedByUserId) throws SQLException {
        Task existingTask = taskDAO.findById(task.getId());
        if (existingTask == null) {
            return false;
        }
        
        boolean assigneeChanged = existingTask.getAssigneeId() == null ? 
                task.getAssigneeId() != null : 
                !existingTask.getAssigneeId().equals(task.getAssigneeId());
                
        boolean result = taskDAO.update(task);
        
        if (result && assigneeChanged && task.getAssigneeId() != null && !task.getAssigneeId().equals(updatedByUserId)) {
            // Notify the new assignee
            User assignee = userDAO.findById(task.getAssigneeId());
            Project project = projectDAO.findById(task.getProjectId());
            
            if (assignee != null && project != null) {
                // Create in-app notification
                notificationService.notifyTaskAssignment(task.getId(), task.getAssigneeId(), task.getTitle(), task.getProjectId());
                
                // Send email notification
                String taskLink = AppConfig.getAppBaseUrl() + "/tasks/view/" + task.getId();
                emailService.sendTaskAssignmentEmail(
                        assignee.getEmail(), 
                        assignee.getFullName(), 
                        task.getTitle(), 
                        project.getName(), 
                        taskLink);
            }
        }
        
        return result;
    }
    
    /**
     * Update task status
     */
    public boolean updateTaskStatus(Integer taskId, String newStatus, Integer updatedByUserId) throws SQLException {
        Task task = taskDAO.findById(taskId);
        if (task != null) {
            String oldStatus = task.getStatus();
            task.setStatus(newStatus);
            
            if ("Completed".equals(newStatus) && !"Completed".equals(oldStatus)) {
                task.setCompletionDate(new Date());
            } else if (!"Completed".equals(newStatus) && "Completed".equals(oldStatus)) {
                task.setCompletionDate(null);
            }
            
            boolean result = taskDAO.update(task);
            
            if (result && updatedByUserId != null && task.getAssigneeId() != null && !updatedByUserId.equals(task.getAssigneeId())) {
                // Notify the assignee about status change
                String message = "Task status changed to '" + newStatus + "': " + task.getTitle();
                notificationService.createNotificationWithRelatedId(
                        message, 
                        "task_status_change", 
                        task.getAssigneeId(), 
                        "/tasks/view/" + taskId, 
                        taskId);
            }
            
            return result;
        }
        return false;
    }
    
    /**
     * Delete a task
     */
    public boolean deleteTask(Integer taskId) throws SQLException {
        return taskDAO.delete(taskId);
    }
    
    /**
     * Update or create subtasks for a parent task
     * @param parentTaskId The ID of the parent task
     * @param subtaskIds Array of existing subtask IDs (null for new subtasks)
     * @param subtaskTitles Array of subtask titles
     * @param subtaskStatuses Array of subtask statuses
     * @return True if the operation was successful
     * @throws SQLException If a database error occurs
     */
    public boolean updateSubtasks(Integer parentTaskId, String[] subtaskIds, 
                                String[] subtaskTitles, String[] subtaskStatuses) throws SQLException {
        if (parentTaskId == null) {
            return false;
        }
        
        // Validate that the parent task exists
        Task parentTask = taskDAO.findById(parentTaskId);
        if (parentTask == null) {
            return false;
        }
        
        boolean success = true;
        
        // Process each subtask
        if (subtaskTitles != null && subtaskTitles.length > 0) {
            for (int i = 0; i < subtaskTitles.length; i++) {
                String title = subtaskTitles[i];
                String subtaskId = subtaskIds != null && i < subtaskIds.length ? subtaskIds[i] : null;
                String status = subtaskStatuses != null && i < subtaskStatuses.length ? 
                                subtaskStatuses[i] : "To Do";
                
                if (title != null && !title.trim().isEmpty()) {
                    // If subtask ID is provided, update existing subtask
                    if (subtaskId != null && !subtaskId.trim().isEmpty() && !subtaskId.equals("0")) {
                        try {
                            Integer taskId = Integer.parseInt(subtaskId);
                            Task subtask = taskDAO.findById(taskId);
                            
                            if (subtask != null) {
                                subtask.setTitle(title);
                                subtask.setStatus(status);
                                success = success && taskDAO.update(subtask);
                            }
                        } catch (NumberFormatException e) {
                            success = false;
                        }
                    } else {
                        // Create new subtask
                        Task subtask = new Task();
                        subtask.setTitle(title);
                        subtask.setStatus(status);
                        subtask.setProjectId(parentTask.getProjectId());
                        subtask.setCreatorId(parentTask.getCreatorId());
                        subtask.setAssigneeId(parentTask.getAssigneeId());
                        subtask.setPriority(parentTask.getPriority());
                        subtask.setCreationDate(new Date());
                        
                        // Store parent-child relationship in a task_relationships table or metadata
                        // For simplicity in this implementation, we'll just add a tag
                        subtask.addTag("subtask:" + parentTaskId);
                        
                        Integer newSubtaskId = taskDAO.insert(subtask);
                        success = success && (newSubtaskId != null);
                    }
                }
            }
        }
        
        return success;
    }
    
    /**
     * Search tasks
     */
    public List<Task> searchTasks(String query) throws SQLException {
        return taskDAO.searchTasks(query);
    }
    
    /**
     * Send reminders for tasks due soon (e.g., within the next 24 hours)
     * This method would typically be called by a scheduled job
     */
    public int sendTaskDueReminders() throws SQLException {
        List<Task> tasks = taskDAO.findOverdueTasks();
        int remindersSent = 0;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Task task : tasks) {
            if (task.getAssigneeId() != null && task.getDueDate() != null) {
                User assignee = userDAO.findById(task.getAssigneeId());
                if (assignee != null) {
                    // Send notification
                    notificationService.notifyTaskDueSoon(
                            task.getId(), 
                            task.getAssigneeId(), 
                            task.getTitle(), 
                            dateFormat.format(task.getDueDate()));
                    
                    // Send email
                    String taskLink = AppConfig.getAppBaseUrl() + "/tasks/view/" + task.getId();
                    emailService.sendTaskDeadlineReminderEmail(
                            assignee.getEmail(), 
                            assignee.getFullName(), 
                            task.getTitle(), 
                            dateFormat.format(task.getDueDate()), 
                            taskLink);
                    
                    remindersSent++;
                }
            }
        }
        
        return remindersSent;
    }
    
    /**
     * Log time spent on a task
     * 
     * @param taskId The ID of the task
     * @param userId The ID of the user logging the time
     * @param hours The number of hours to log
     * @return True if the time was successfully logged, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean logTimeForTask(Integer taskId, Integer userId, Double hours) throws SQLException {
        if (taskId == null || userId == null || hours == null || hours <= 0) {
            return false;
        }
        
        // Get the task
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            return false;
        }
        
        // Add the logged time to the current logged hours
        Double currentLoggedHours = task.getLoggedHours();
        if (currentLoggedHours == null) {
            currentLoggedHours = 0.0;
        }
        
        task.setLoggedHours(currentLoggedHours + hours);
        
        // Update the task in the database
        boolean updated = taskDAO.update(task);
        
        // If the update was successful, create a time log entry
        if (updated) {
            // In a real application, you'd store this in a separate time_logs table
            // For this example, we'll just update the task
            
            // Notify assignee if different from the user logging time
            if (task.getAssigneeId() != null && !task.getAssigneeId().equals(userId)) {
                String message = String.format("%.2f hours logged on task: %s", hours, task.getTitle());
                notificationService.createNotificationWithRelatedId(
                    message,
                    "time_logged",
                    task.getAssigneeId(),
                    "/task?id=" + taskId,
                    taskId
                );
            }
            
            return true;
        }
        
        return false;
    }
}