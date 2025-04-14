package com.taskmanager.socket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.service.ProjectService;
import com.taskmanager.service.TaskService;
import org.json.JSONObject;

/**
 * WebSocket endpoint for real-time task updates.
 * Enables collaborative task management by broadcasting task changes to all relevant users.
 */
@ServerEndpoint(
    value = "/ws/tasks/{projectId}",
    configurator = SocketConfigurator.class,
    decoders = SocketMessageDecoder.class,
    encoders = SocketMessageEncoder.class
)
public class TaskUpdateEndpoint {
    
    private static final Logger LOGGER = Logger.getLogger(TaskUpdateEndpoint.class.getName());
    
    // Map of project IDs to sets of sessions (users watching the project)
    private static final Map<String, Set<Session>> projectSessions = new HashMap<>();
    
    // Map of user IDs to their WebSocket sessions
    private static final Map<String, Session> userSessions = Collections.synchronizedMap(new HashMap<>());
    
    private TaskService taskService = new TaskService();
    private ProjectService projectService = new ProjectService();
    private User user;
    private String userId;
    private String projectId;
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("projectId") String projectId) {
        this.projectId = projectId;
        this.user = (User) config.getUserProperties().get("user");
        
        if (user == null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, 
                                             "User authentication failed"));
                return;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error closing unauthorized WebSocket connection", e);
            }
            return;
        }
        
        this.userId = String.valueOf(user.getId());
        
        // Verify that the user has access to this project
        try {
            boolean hasAccess = projectService.hasAccess(user.getId(), Integer.parseInt(projectId));
            if (!hasAccess) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, 
                                             "User does not have access to this project"));
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying project access", e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                                             "Error verifying project access"));
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Error closing WebSocket connection", ioe);
            }
            return;
        }
        
        // Add user session to the maps
        userSessions.put(userId, session);
        
        // Add user to project watchers
        synchronized (projectSessions) {
            Set<Session> projectWatchers = projectSessions.computeIfAbsent(
                projectId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            projectWatchers.add(session);
        }
        
        // Send confirmation message
        SocketMessage welcomeMessage = new SocketMessage(
            "system", 
            "Connected to task update service for project " + projectId,
            "System"
        );
        try {
            session.getBasicRemote().sendObject(welcomeMessage);
        } catch (IOException | EncodeException e) {
            LOGGER.log(Level.SEVERE, "Error sending welcome message", e);
        }
        
        LOGGER.info("User " + userId + " connected to task updates for project: " + projectId);
    }
    
    @OnMessage
    public void onMessage(SocketMessage message, Session session) {
        LOGGER.info("Received task message: " + message);
        
        try {
            if ("task_update".equals(message.getType())) {
                // Process task update from client
                Integer taskId = message.getObjectId();
                if (taskId != null) {
                    // Verify the task belongs to the correct project
                    Task task = taskService.getTaskById(taskId);
                    
                    if (task != null && task.getProjectId().toString().equals(projectId)) {
                        // Broadcast the update to all project watchers
                        broadcastTaskUpdate(message, taskId);
                    } else {
                        session.getBasicRemote().sendObject(
                            new SocketMessage("error", "Invalid task or project", "System")
                        );
                    }
                }
            } else if ("task_status_change".equals(message.getType())) {
                // Handle task status change
                processTaskStatusChange(message);
            } else if ("task_assignment".equals(message.getType())) {
                // Handle task assignment change
                processTaskAssignment(message);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing task message", e);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // Remove user from active sessions
        if (userId != null) {
            userSessions.remove(userId);
            
            // Remove from project watchers
            synchronized (projectSessions) {
                Set<Session> projectWatchers = projectSessions.get(projectId);
                if (projectWatchers != null) {
                    projectWatchers.remove(session);
                    
                    // Clean up empty sets
                    if (projectWatchers.isEmpty()) {
                        projectSessions.remove(projectId);
                    }
                }
            }
            
            LOGGER.info("User " + userId + " disconnected from task updates for project " + 
                       projectId + " - " + reason.getReasonPhrase());
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error in task update endpoint for user " + userId + 
                  " and project " + projectId, throwable);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, 
                                         "Server error occurred"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing WebSocket connection after error", e);
        }
    }
    
    /**
     * Process a task status change message
     */
    private void processTaskStatusChange(SocketMessage message) throws Exception {
        JSONObject contentJson = new JSONObject(message.getContent());
        Integer taskId = message.getObjectId();
        String newStatus = contentJson.getString("status");
        
        // Update the task status in the database
        boolean updated = taskService.updateTaskStatus(taskId, newStatus, user.getId());
        
        if (updated) {
            // Broadcast the change to all project watchers
            broadcastTaskUpdate(message, taskId);
            
            // Notify the assignee if it's not the current user
            Task task = taskService.getTaskById(taskId);
            if (task.getAssigneeId() != null && !task.getAssigneeId().equals(user.getId())) {
                String assigneeId = task.getAssigneeId().toString();
                String content = "Task status changed to '" + newStatus + "': " + task.getTitle();
                
                // Use NotificationEndpoint to send real-time notification
                NotificationEndpoint.sendNotification(
                    assigneeId, 
                    content, 
                    "task_status_change", 
                    taskId
                );
            }
        }
    }
    
    /**
     * Process a task assignment message
     */
    private void processTaskAssignment(SocketMessage message) throws Exception {
        JSONObject contentJson = new JSONObject(message.getContent());
        Integer taskId = message.getObjectId();
        Integer assigneeId = contentJson.optInt("assigneeId", 0);
        
        Task task = taskService.getTaskById(taskId);
        if (task != null) {
            // Update the assignee
            task.setAssigneeId(assigneeId == 0 ? null : assigneeId);
            boolean updated = taskService.updateTask(task, user.getId());
            
            if (updated) {
                // Broadcast the change to all project watchers
                broadcastTaskUpdate(message, taskId);
                
                // Notify the new assignee
                if (assigneeId != 0 && !assigneeId.equals(user.getId())) {
                    String assigneeIdStr = assigneeId.toString();
                    String content = "You have been assigned to task: " + task.getTitle();
                    
                    // Use NotificationEndpoint to send real-time notification
                    NotificationEndpoint.sendNotification(
                        assigneeIdStr, 
                        content, 
                        "task_assignment", 
                        taskId
                    );
                }
            }
        }
    }
    
    /**
     * Broadcast a task update to all users watching the project
     */
    private void broadcastTaskUpdate(SocketMessage message, Integer taskId) {
        Set<Session> projectWatchers = projectSessions.get(projectId);
        
        if (projectWatchers != null) {
            for (Session watcher : projectWatchers) {
                if (watcher.isOpen()) {
                    try {
                        watcher.getBasicRemote().sendObject(message);
                    } catch (IOException | EncodeException e) {
                        LOGGER.log(Level.SEVERE, "Error broadcasting task update", e);
                    }
                }
            }
        }
    }
    
    /**
     * Broadcast a task update to all project watchers from an external source
     * This method can be called from other parts of the application
     */
    public static void broadcastExternalTaskUpdate(String projectId, SocketMessage message) {
        Set<Session> projectWatchers = projectSessions.get(projectId);
        
        if (projectWatchers != null) {
            for (Session watcher : projectWatchers) {
                if (watcher.isOpen()) {
                    try {
                        watcher.getBasicRemote().sendObject(message);
                    } catch (IOException | EncodeException e) {
                        LOGGER.log(Level.SEVERE, "Error broadcasting external task update", e);
                    }
                }
            }
        }
    }
}