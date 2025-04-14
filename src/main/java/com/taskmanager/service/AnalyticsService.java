package com.taskmanager.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taskmanager.config.DatabaseConfig;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;

/**
 * Service class for analytics and reporting functionality
 */
public class AnalyticsService {
    
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    
    public AnalyticsService() {
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();
    }
    
    /**
     * Get overall system statistics
     */
    public SystemStats getSystemStats() throws SQLException {
        SystemStats stats = new SystemStats();
        
        stats.setTotalUsers(userDAO.countUsers());
        stats.setTotalProjects(projectDAO.countProjects());
        stats.setTotalTasks(taskDAO.countTasks());
        stats.setCompletedTasks(taskDAO.countCompletedTasks());
        stats.setOverdueTasks(taskDAO.countOverdueTasks());
        
        if (stats.getTotalTasks() > 0) {
            double completionRate = ((double) stats.getCompletedTasks() / stats.getTotalTasks()) * 100;
            stats.setCompletionRate(Math.round(completionRate));
        }
        
        return stats;
    }
    
    /**
     * Get user productivity statistics
     */
    public UserProductivityStats getUserProductivity(Integer userId) throws SQLException {
        UserProductivityStats stats = new UserProductivityStats();
        
        stats.setUserId(userId);
        stats.setAssignedTasks(taskDAO.countTasksByAssignee(userId));
        stats.setCompletedTasks(taskDAO.countCompletedTasksByAssignee(userId));
        stats.setOverdueTasks(taskDAO.countOverdueTasksByAssignee(userId));
        
        // Tasks completed in last 7 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date lastWeek = cal.getTime();
        stats.setCompletedLast7Days(taskDAO.countTasksCompletedSinceByAssignee(userId, lastWeek));
        
        // Tasks completed on time vs late
        stats.setCompletedOnTime(taskDAO.countTasksCompletedOnTimeByAssignee(userId));
        stats.setCompletedLate(stats.getCompletedTasks() - stats.getCompletedOnTime());
        
        // Calculate completion rate
        if (stats.getAssignedTasks() > 0) {
            double completionRate = ((double) stats.getCompletedTasks() / stats.getAssignedTasks()) * 100;
            stats.setCompletionRate(Math.round(completionRate));
        }
        
        return stats;
    }
    
    /**
     * Get project statistics
     */
    public ProjectStats getProjectStats(Integer projectId) throws SQLException {
        ProjectStats stats = new ProjectStats();
        Project project = projectDAO.findById(projectId);
        
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }
        
        stats.setProjectId(projectId);
        stats.setProjectName(project.getName());
        stats.setTotalTasks(taskDAO.countTasksByProject(projectId));
        stats.setCompletedTasks(taskDAO.countCompletedTasksByProject(projectId));
        stats.setOverdueTasks(taskDAO.countOverdueTasksByProject(projectId));
        
        // Calculate completion percentage
        if (stats.getTotalTasks() > 0) {
            double completionPercentage = ((double) stats.getCompletedTasks() / stats.getTotalTasks()) * 100;
            stats.setCompletionPercentage(Math.round(completionPercentage));
        }
        
        // Task distribution by status
        Map<String, Integer> statusDistribution = new HashMap<>();
        List<Task> tasks = taskDAO.findByProjectId(projectId);
        for (Task task : tasks) {
            String status = task.getStatus();
            statusDistribution.put(status, statusDistribution.getOrDefault(status, 0) + 1);
        }
        stats.setStatusDistribution(statusDistribution);
        
        // Task distribution by priority
        Map<String, Integer> priorityDistribution = new HashMap<>();
        for (Task task : tasks) {
            String priority = task.getPriority();
            priorityDistribution.put(priority, priorityDistribution.getOrDefault(priority, 0) + 1);
        }
        stats.setPriorityDistribution(priorityDistribution);
        
        return stats;
    }
    
    /**
     * Get team performance statistics
     */
    public List<TeamMemberPerformance> getTeamPerformance(Integer projectId) throws SQLException {
        List<TeamMemberPerformance> teamPerformance = new ArrayList<>();
        List<User> teamMembers = projectDAO.findTeamMembers(projectId);
        
        for (User member : teamMembers) {
            TeamMemberPerformance performance = new TeamMemberPerformance();
            performance.setUserId(member.getId());
            performance.setUserName(member.getFullName());
            performance.setAssignedTasks(taskDAO.countTasksByAssigneeAndProject(member.getId(), projectId));
            performance.setCompletedTasks(taskDAO.countCompletedTasksByAssigneeAndProject(member.getId(), projectId));
            
            if (performance.getAssignedTasks() > 0) {
                double completionRate = ((double) performance.getCompletedTasks() / performance.getAssignedTasks()) * 100;
                performance.setCompletionRate(Math.round(completionRate));
            }
            
            teamPerformance.add(performance);
        }
        
        return teamPerformance;
    }
    
    /**
     * Get trend data for tasks over time
     */
    public TrendData getTaskTrends(Integer projectId, int numberOfDays) throws SQLException {
        TrendData trends = new TrendData();
        List<Date> dates = new ArrayList<>();
        List<Integer> createdCounts = new ArrayList<>();
        List<Integer> completedCounts = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, -numberOfDays);
        Date startDate = calendar.getTime();
        
        // Get task creation and completion trends
        for (int i = 0; i < numberOfDays; i++) {
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, i);
            Date currentDate = calendar.getTime();
            dates.add(currentDate);
            
            int tasksCreated = projectId == null ? 
                    taskDAO.countTasksCreatedOnDate(currentDate) : 
                    taskDAO.countTasksCreatedOnDateForProject(currentDate, projectId);
                    
            int tasksCompleted = projectId == null ? 
                    taskDAO.countTasksCompletedOnDate(currentDate) : 
                    taskDAO.countTasksCompletedOnDateForProject(currentDate, projectId);
                    
            createdCounts.add(tasksCreated);
            completedCounts.add(tasksCompleted);
        }
        
        trends.setDates(dates);
        trends.setCreatedCounts(createdCounts);
        trends.setCompletedCounts(completedCounts);
        
        return trends;
    }
    
    /**
     * Log user activity for analytics purposes
     * 
     * @param userId The ID of the user performing the action
     * @param activityType The type of activity (e.g., "LOGIN", "TASK_CREATED", "PROJECT_UPDATED")
     * @param description Additional details about the activity
     * @param entityId ID of the related entity (task ID, project ID, etc.) or 0 if not applicable
     * @throws SQLException if a database error occurs
     */
    public void logUserActivity(Integer userId, String activityType, String description, int entityId) throws SQLException {
        if (userId == null || activityType == null) {
            throw new IllegalArgumentException("User ID and activity type are required");
        }
        
        // Create an activity log entry in the database
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "INSERT INTO user_activity_log (user_id, activity_type, description, entity_id, activity_date) " +
                         "VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            stmt.setString(2, activityType);
            stmt.setString(3, description);
            stmt.setInt(4, entityId);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
        } finally {
            DatabaseConfig.closeResources(stmt, conn);
        }
    }
    
    /**
     * Get dashboard summary for a user
     * 
     * @param userId The ID of the user requesting the dashboard summary
     * @return Map containing dashboard summary data
     * @throws SQLException if a database error occurs
     */
    public Map<String, Object> getDashboardSummary(Integer userId) throws SQLException {
        Map<String, Object> summary = new HashMap<>();
        
        // Get user-specific data
        User user = userDAO.findById(userId);
        summary.put("user", user);
        
        // Get user's tasks statistics
        UserProductivityStats productivity = getUserProductivity(userId);
        summary.put("productivity", productivity);
        
        // Get recent activity
        List<Map<String, Object>> recentActivity = getUserRecentActivity(userId, 5);
        summary.put("recentActivity", recentActivity);
        
        // Get upcoming tasks
        List<Task> upcomingTasks = taskDAO.findUpcomingTasksByAssignee(userId, 5);
        summary.put("upcomingTasks", upcomingTasks);
        
        // Get overdue tasks
        List<Task> overdueTasks = taskDAO.findOverdueTasksByAssignee(userId, 5);
        summary.put("overdueTasks", overdueTasks);
        
        // Get user's projects
        List<Project> userProjects = projectDAO.findByUserId(userId);
        summary.put("projects", userProjects);
        
        return summary;
    }
    
    /**
     * Get detailed project analytics
     * 
     * @param projectId The ID of the project to get analytics for
     * @return Map containing project analytics data
     * @throws SQLException if a database error occurs
     */
    public Map<String, Object> getProjectAnalytics(Integer projectId) throws SQLException {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get basic project information
        Project project = projectDAO.findById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }
        analytics.put("project", project);
        
        // Get project stats
        ProjectStats stats = getProjectStats(projectId);
        analytics.put("stats", stats);
        
        // Get team performance
        List<TeamMemberPerformance> teamPerformance = getTeamPerformance(projectId);
        analytics.put("teamPerformance", teamPerformance);
        
        // Get task trends over the last 30 days
        TrendData trends = getTaskTrends(projectId, 30);
        analytics.put("trends", trends);
        
        // Get recent project activity
        List<Map<String, Object>> recentActivity = getProjectRecentActivity(projectId, 10);
        analytics.put("recentActivity", recentActivity);
        
        return analytics;
    }
    
    /**
     * Get detailed task analytics
     * 
     * @param taskId The ID of the task to get analytics for
     * @return Map containing task analytics data
     * @throws SQLException if a database error occurs
     */
    public Map<String, Object> getTaskAnalytics(Integer taskId) throws SQLException {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get task details
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        analytics.put("task", task);
        
        // Get task history (status changes, assignments, etc.)
        List<Map<String, Object>> taskHistory = getTaskHistory(taskId);
        analytics.put("history", taskHistory);
        
        // Get time tracking data if available
        Map<String, Object> timeTracking = getTaskTimeTracking(taskId);
        analytics.put("timeTracking", timeTracking);
        
        // Get related tasks
        List<Task> relatedTasks = taskDAO.findRelatedTasks(taskId);
        analytics.put("relatedTasks", relatedTasks);
        
        // Get comments count
        int commentsCount = taskDAO.countCommentsByTaskId(taskId);
        analytics.put("commentsCount", commentsCount);
        
        // Get file attachments count
        int attachmentsCount = taskDAO.countAttachmentsByTaskId(taskId);
        analytics.put("attachmentsCount", attachmentsCount);
        
        return analytics;
    }
    
    /**
     * Get team analytics data
     * 
     * @param teamId The ID of the team to get analytics for
     * @return Map containing team analytics data
     * @throws SQLException if a database error occurs
     */
    public Map<String, Object> getTeamAnalytics(Integer teamId) throws SQLException {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get team details
        Map<String, Object> teamDetails = getTeamDetails(teamId);
        analytics.put("teamDetails", teamDetails);
        
        // Get team members and their productivity stats
        List<Map<String, Object>> memberStats = new ArrayList<>();
        List<User> members = userDAO.findByTeamId(teamId);
        
        for (User member : members) {
            Map<String, Object> memberStat = new HashMap<>();
            memberStat.put("user", member);
            memberStat.put("productivity", getUserProductivity(member.getId()));
            memberStats.add(memberStat);
        }
        analytics.put("memberStats", memberStats);
        
        // Get projects assigned to team
        List<Project> teamProjects = projectDAO.findByTeamId(teamId);
        analytics.put("projects", teamProjects);
        
        // Get team workload distribution
        Map<String, Integer> workloadDistribution = getTeamWorkloadDistribution(teamId);
        analytics.put("workloadDistribution", workloadDistribution);
        
        // Get team performance over time (last 90 days)
        Map<String, Object> performanceTrend = getTeamPerformanceTrend(teamId, 90);
        analytics.put("performanceTrend", performanceTrend);
        
        return analytics;
    }
    
    /**
     * Get user's recent activity
     * 
     * @param userId User ID
     * @param limit Maximum number of records to retrieve
     * @return List of activity records
     * @throws SQLException if a database error occurs
     */
    private List<Map<String, Object>> getUserRecentActivity(Integer userId, int limit) throws SQLException {
        // This would be implemented to retrieve activity from user_activity_log table
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    /**
     * Get project's recent activity
     * 
     * @param projectId Project ID
     * @param limit Maximum number of records to retrieve
     * @return List of activity records
     * @throws SQLException if a database error occurs
     */
    private List<Map<String, Object>> getProjectRecentActivity(Integer projectId, int limit) throws SQLException {
        // This would be implemented to retrieve activity related to the project
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    /**
     * Get task history
     * 
     * @param taskId Task ID
     * @return List of task history records
     * @throws SQLException if a database error occurs
     */
    private List<Map<String, Object>> getTaskHistory(Integer taskId) throws SQLException {
        // This would be implemented to retrieve task history from task_history table
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    /**
     * Get task time tracking data
     * 
     * @param taskId Task ID
     * @return Map containing time tracking data
     * @throws SQLException if a database error occurs
     */
    private Map<String, Object> getTaskTimeTracking(Integer taskId) throws SQLException {
        // This would be implemented to retrieve time tracking data
        // Placeholder implementation
        return new HashMap<>();
    }
    
    /**
     * Get team details
     * 
     * @param teamId Team ID
     * @return Map containing team details
     * @throws SQLException if a database error occurs
     */
    private Map<String, Object> getTeamDetails(Integer teamId) throws SQLException {
        // This would be implemented to retrieve team details
        // Placeholder implementation
        return new HashMap<>();
    }
    
    /**
     * Get team workload distribution
     * 
     * @param teamId Team ID
     * @return Map of user IDs to task counts
     * @throws SQLException if a database error occurs
     */
    private Map<String, Integer> getTeamWorkloadDistribution(Integer teamId) throws SQLException {
        // This would be implemented to calculate workload distribution among team members
        // Placeholder implementation
        return new HashMap<>();
    }
    
    /**
     * Get team performance trend over time
     * 
     * @param teamId Team ID
     * @param days Number of days to analyze
     * @return Map containing performance trend data
     * @throws SQLException if a database error occurs
     */
    private Map<String, Object> getTeamPerformanceTrend(Integer teamId, int days) throws SQLException {
        // This would be implemented to calculate team performance over time
        // Placeholder implementation
        return new HashMap<>();
    }
    
    // Inner classes to encapsulate analytic results
    
    /**
     * System-wide statistics
     */
    public static class SystemStats {
        private int totalUsers;
        private int totalProjects;
        private int totalTasks;
        private int completedTasks;
        private int overdueTasks;
        private long completionRate;
        
        // Getters and setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getTotalProjects() { return totalProjects; }
        public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public int getOverdueTasks() { return overdueTasks; }
        public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
        
        public long getCompletionRate() { return completionRate; }
        public void setCompletionRate(long completionRate) { this.completionRate = completionRate; }
    }
    
    /**
     * User productivity statistics
     */
    public static class UserProductivityStats {
        private Integer userId;
        private int assignedTasks;
        private int completedTasks;
        private int overdueTasks;
        private int completedLast7Days;
        private int completedOnTime;
        private int completedLate;
        private long completionRate;
        
        // Getters and setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public int getAssignedTasks() { return assignedTasks; }
        public void setAssignedTasks(int assignedTasks) { this.assignedTasks = assignedTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public int getOverdueTasks() { return overdueTasks; }
        public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
        
        public int getCompletedLast7Days() { return completedLast7Days; }
        public void setCompletedLast7Days(int completedLast7Days) { this.completedLast7Days = completedLast7Days; }
        
        public int getCompletedOnTime() { return completedOnTime; }
        public void setCompletedOnTime(int completedOnTime) { this.completedOnTime = completedOnTime; }
        
        public int getCompletedLate() { return completedLate; }
        public void setCompletedLate(int completedLate) { this.completedLate = completedLate; }
        
        public long getCompletionRate() { return completionRate; }
        public void setCompletionRate(long completionRate) { this.completionRate = completionRate; }
    }
    
    /**
     * Project statistics
     */
    public static class ProjectStats {
        private Integer projectId;
        private String projectName;
        private int totalTasks;
        private int completedTasks;
        private int overdueTasks;
        private long completionPercentage;
        private Map<String, Integer> statusDistribution;
        private Map<String, Integer> priorityDistribution;
        
        // Getters and setters
        public Integer getProjectId() { return projectId; }
        public void setProjectId(Integer projectId) { this.projectId = projectId; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public int getOverdueTasks() { return overdueTasks; }
        public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
        
        public long getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(long completionPercentage) { this.completionPercentage = completionPercentage; }
        
        public Map<String, Integer> getStatusDistribution() { return statusDistribution; }
        public void setStatusDistribution(Map<String, Integer> statusDistribution) { this.statusDistribution = statusDistribution; }
        
        public Map<String, Integer> getPriorityDistribution() { return priorityDistribution; }
        public void setPriorityDistribution(Map<String, Integer> priorityDistribution) { this.priorityDistribution = priorityDistribution; }
    }
    
    /**
     * Individual team member performance
     */
    public static class TeamMemberPerformance {
        private Integer userId;
        private String userName;
        private int assignedTasks;
        private int completedTasks;
        private long completionRate;
        
        // Getters and setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public int getAssignedTasks() { return assignedTasks; }
        public void setAssignedTasks(int assignedTasks) { this.assignedTasks = assignedTasks; }
        
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        
        public long getCompletionRate() { return completionRate; }
        public void setCompletionRate(long completionRate) { this.completionRate = completionRate; }
    }
    
    /**
     * Trend data for time-series analysis
     */
    public static class TrendData {
        private List<Date> dates;
        private List<Integer> createdCounts;
        private List<Integer> completedCounts;
        
        // Getters and setters
        public List<Date> getDates() { return dates; }
        public void setDates(List<Date> dates) { this.dates = dates; }
        
        public List<Integer> getCreatedCounts() { return createdCounts; }
        public void setCreatedCounts(List<Integer> createdCounts) { this.createdCounts = createdCounts; }
        
        public List<Integer> getCompletedCounts() { return completedCounts; }
        public void setCompletedCounts(List<Integer> completedCounts) { this.completedCounts = completedCounts; }
    }
}