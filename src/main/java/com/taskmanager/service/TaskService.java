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
}