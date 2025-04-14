package com.taskmanager.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.taskmanager.dao.CommentDAO;
import com.taskmanager.dao.FileDAO;
import com.taskmanager.dao.ProjectDAO;
import com.taskmanager.dao.TaskDAO;
import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.Comment;
import com.taskmanager.model.File;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;

/**
 * Service class for search operations across different entities
 */
public class SearchService {
    
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private CommentDAO commentDAO;
    private FileDAO fileDAO;
    private UserDAO userDAO;
    
    // Relevance scores for different types of matches
    private static final int EXACT_MATCH_SCORE = 100;
    private static final int PARTIAL_MATCH_SCORE = 50;
    private static final int TAG_MATCH_SCORE = 80;
    private static final int DESCRIPTION_MATCH_SCORE = 40;
    private static final int COMMENT_MATCH_SCORE = 30;
    
    public SearchService() {
        taskDAO = new TaskDAO();
        projectDAO = new ProjectDAO();
        commentDAO = new CommentDAO();
        fileDAO = new FileDAO();
        userDAO = new UserDAO();
    }
    
    /**
     * Class representing a generic search result with relevance score
     */
    public static class SearchResult {
        private String type;         // Type of entity (Task, Project, etc.)
        private Object entity;       // The actual entity object
        private int relevanceScore;  // Relevance score for ranking results
        
        public SearchResult(String type, Object entity, int relevanceScore) {
            this.type = type;
            this.entity = entity;
            this.relevanceScore = relevanceScore;
        }
        
        public String getType() {
            return type;
        }
        
        public Object getEntity() {
            return entity;
        }
        
        public int getRelevanceScore() {
            return relevanceScore;
        }
    }
    
    /**
     * Search across all entities (tasks, projects, comments, files) with a given query
     * @param query The search query string
     * @param userId The ID of the user performing the search (for access control)
     * @return List of search results sorted by relevance
     */
    public List<SearchResult> globalSearch(String query, Integer userId) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Normalize query
        String normalizedQuery = query.toLowerCase().trim();
        
        List<SearchResult> results = new ArrayList<>();
        
        // Search tasks
        results.addAll(searchTasks(normalizedQuery, userId));
        
        // Search projects
        results.addAll(searchProjects(normalizedQuery, userId));
        
        // Search comments
        results.addAll(searchComments(normalizedQuery, userId));
        
        // Search files
        results.addAll(searchFiles(normalizedQuery, userId));
        
        // Sort by relevance score (descending)
        Collections.sort(results, Comparator.comparing(SearchResult::getRelevanceScore).reversed());
        
        return results;
    }
    
    /**
     * Search for tasks matching the query
     */
    public List<SearchResult> searchTasks(String normalizedQuery, Integer userId) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        List<Task> accessibleTasks = taskDAO.findTasksAccessibleByUser(userId);
        
        for (Task task : accessibleTasks) {
            int score = calculateTaskRelevanceScore(task, normalizedQuery);
            
            if (score > 0) {
                results.add(new SearchResult("TASK", task, score));
            }
        }
        
        return results;
    }
    
    /**
     * Search for projects matching the query
     */
    public List<SearchResult> searchProjects(String normalizedQuery, Integer userId) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        List<Project> accessibleProjects = projectDAO.findProjectsAccessibleByUser(userId);
        
        for (Project project : accessibleProjects) {
            int score = calculateProjectRelevanceScore(project, normalizedQuery);
            
            if (score > 0) {
                results.add(new SearchResult("PROJECT", project, score));
            }
        }
        
        return results;
    }
    
    /**
     * Search for comments matching the query
     */
    public List<SearchResult> searchComments(String normalizedQuery, Integer userId) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        
        // Get comments from tasks accessible to the user
        List<Task> accessibleTasks = taskDAO.findTasksAccessibleByUser(userId);
        List<Integer> accessibleTaskIds = accessibleTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        if (!accessibleTaskIds.isEmpty()) {
            Map<Integer, List<Comment>> commentMap = commentDAO.findByTaskIds(accessibleTaskIds);
            List<Comment> comments = commentMap.values().stream()
                                               .flatMap(List::stream)
                                               .collect(Collectors.toList());
            
            for (Comment comment : comments) {
                int score = calculateCommentRelevanceScore(comment, normalizedQuery);
                
                if (score > 0) {
                    results.add(new SearchResult("COMMENT", comment, score));
                }
            }
        }
        
        return results;
    }
    
    /**
     * Search for files matching the query
     */
    public List<SearchResult> searchFiles(String normalizedQuery, Integer userId) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        
        // Get files from tasks and projects accessible to the user
        List<Task> accessibleTasks = taskDAO.findTasksAccessibleByUser(userId);
        List<Integer> accessibleTaskIds = accessibleTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        List<Project> accessibleProjects = projectDAO.findProjectsAccessibleByUser(userId);
        List<Integer> accessibleProjectIds = accessibleProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());
        
        List<File> files = new ArrayList<>();
        
        if (!accessibleTaskIds.isEmpty()) {
            fileDAO.findByTaskIds(accessibleTaskIds).values().forEach(files::addAll);
        }
        
        if (!accessibleProjectIds.isEmpty()) {
            fileDAO.findByProjectIds(accessibleProjectIds).values().forEach(files::addAll);
        }
        
        for (File file : files) {
            int score = calculateFileRelevanceScore(file, normalizedQuery);
            
            if (score > 0) {
                results.add(new SearchResult("FILE", file, score));
            }
        }
        
        return results;
    }
    
    /**
     * Search for users matching the query (for admin or team management purposes)
     */
    public List<User> searchUsers(String query, boolean isAdmin) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        
        // If admin, search all users
        if (isAdmin) {
            return Collections.singletonList(userDAO.findByNameOrEmail(normalizedQuery));
        } else {
            // Non-admin users can only search for active users
            User user = userDAO.findActiveByNameOrEmail(normalizedQuery);
            return user != null ? Collections.singletonList(user) : Collections.emptyList();
        }
    }
    
    /**
     * Calculate relevance score for a task based on how well it matches the query
     */
    private int calculateTaskRelevanceScore(Task task, String normalizedQuery) {
        int score = 0;
        
        // Check for exact title match
        if (task.getTitle() != null && task.getTitle().toLowerCase().equals(normalizedQuery)) {
            score += EXACT_MATCH_SCORE;
        }
        // Check for partial title match
        else if (task.getTitle() != null && task.getTitle().toLowerCase().contains(normalizedQuery)) {
            score += PARTIAL_MATCH_SCORE;
        }
        
        // Check description
        if (task.getDescription() != null && task.getDescription().toLowerCase().contains(normalizedQuery)) {
            score += DESCRIPTION_MATCH_SCORE;
        }
        
        // Check tags (if implemented)
        if (task.getTags() != null) {
            List<String> tags = task.getTags();
            for (String tag : tags) {
                if (tag.trim().toLowerCase().equals(normalizedQuery)) {
                    score += TAG_MATCH_SCORE;
                    break;
                }
            }
        }
        
        // Priority-based boost
        if (task.getPriority() != null) {
            switch (task.getPriority().toLowerCase()) {
                case "high":
                    score += 20;
                    break;
                case "medium":
                    score += 10;
                    break;
                default:
                    break;
            }
        }
        
        return score;
    }
    
    /**
     * Calculate relevance score for a project based on how well it matches the query
     */
    private int calculateProjectRelevanceScore(Project project, String normalizedQuery) {
        int score = 0;
        
        // Check for exact name match
        if (project.getName() != null && project.getName().toLowerCase().equals(normalizedQuery)) {
            score += EXACT_MATCH_SCORE;
        }
        // Check for partial name match
        else if (project.getName() != null && project.getName().toLowerCase().contains(normalizedQuery)) {
            score += PARTIAL_MATCH_SCORE;
        }
        
        // Check description
        if (project.getDescription() != null && project.getDescription().toLowerCase().contains(normalizedQuery)) {
            score += DESCRIPTION_MATCH_SCORE;
        }
        
        return score;
    }
    
    /**
     * Calculate relevance score for a comment based on how well it matches the query
     */
    private int calculateCommentRelevanceScore(Comment comment, String normalizedQuery) {
        int score = 0;
        
        // Check content
        if (comment.getContent() != null && comment.getContent().toLowerCase().contains(normalizedQuery)) {
            score += COMMENT_MATCH_SCORE;
            
            // Boost score if it's an exact match
            if (comment.getContent().toLowerCase().equals(normalizedQuery)) {
                score += 20;
            }
        }
        
        return score;
    }
    
    /**
     * Calculate relevance score for a file based on how well it matches the query
     */
    private int calculateFileRelevanceScore(File file, String normalizedQuery) {
        int score = 0;
        
        // Check filename
        if (file.getFilename() != null) {
            if (file.getFilename().toLowerCase().equals(normalizedQuery)) {
                score += EXACT_MATCH_SCORE;
            } else if (file.getFilename().toLowerCase().contains(normalizedQuery)) {
                score += PARTIAL_MATCH_SCORE;
            }
        }
        
        return score;
    }
    
    /**
     * Advanced search with filters
     * @param filters Map of field names to filter values
     * @param userId The ID of the user performing the search
     * @return List of search results
     */
    public List<SearchResult> advancedSearch(Map<String, Object> filters, Integer userId) throws SQLException {
        // Initialize result lists
        List<Task> tasks = null;
        List<Project> projects = null;
        
        // Get accessible tasks and projects for the user
        List<Task> accessibleTasks = taskDAO.findTasksAccessibleByUser(userId);
        List<Project> accessibleProjects = projectDAO.findProjectsAccessibleByUser(userId);
        
        // Apply filters
        if (filters.containsKey("type")) {
            String type = (String) filters.get("type");
            if (type.equalsIgnoreCase("task")) {
                projects = Collections.emptyList();
            } else if (type.equalsIgnoreCase("project")) {
                tasks = Collections.emptyList();
            }
        }
        
        // Filter tasks
        if (tasks != accessibleTasks) {
            tasks = accessibleTasks.stream().filter(task -> {
                return matchesTaskFilters(task, filters);
            }).collect(Collectors.toList());
        }
        
        // Filter projects
        if (projects != accessibleProjects) {
            projects = accessibleProjects.stream().filter(project -> {
                return matchesProjectFilters(project, filters);
            }).collect(Collectors.toList());
        }
        
        // Convert to search results
        List<SearchResult> results = new ArrayList<>();
        
        // Add tasks with default relevance (since this is filter-based)
        if (tasks != null) {
            for (Task task : tasks) {
                results.add(new SearchResult("TASK", task, 50));
            }
        }
        
        // Add projects with default relevance
        if (projects != null) {
            for (Project project : projects) {
                results.add(new SearchResult("PROJECT", project, 50));
            }
        }
        
        return results;
    }
    
    /**
     * Check if a task matches the specified filters
     */
    private boolean matchesTaskFilters(Task task, Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            switch (key.toLowerCase()) {
                case "title":
                    if (!wildcardMatch(task.getTitle(), (String) value)) {
                        return false;
                    }
                    break;
                case "status":
                    if (!value.equals(task.getStatus())) {
                        return false;
                    }
                    break;
                case "priority":
                    if (!value.equals(task.getPriority())) {
                        return false;
                    }
                    break;
                case "assignee":
                    if (!value.equals(task.getAssigneeId())) {
                        return false;
                    }
                    break;
                case "created_after":
                    if (task.getCreationDate() == null || 
                            task.getCreationDate().before((java.util.Date) value)) {
                        return false;
                    }
                    break;
                case "created_before":
                    if (task.getCreationDate() == null || 
                            task.getCreationDate().after((java.util.Date) value)) {
                        return false;
                    }
                    break;
                case "due_after":
                    if (task.getDueDate() == null || 
                            task.getDueDate().before((java.util.Date) value)) {
                        return false;
                    }
                    break;
                case "due_before":
                    if (task.getDueDate() == null || 
                            task.getDueDate().after((java.util.Date) value)) {
                        return false;
                    }
                    break;
                case "tags":
                    if (task.getTags() == null || !containsTag(task.getTags(), (String) value)) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
    
    /**
     * Check if a project matches the specified filters
     */
    private boolean matchesProjectFilters(Project project, Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            switch (key.toLowerCase()) {
                case "name":
                    if (!wildcardMatch(project.getName(), (String) value)) {
                        return false;
                    }
                    break;
                case "status":
                    if (!value.equals(project.getStatus())) {
                        return false;
                    }
                    break;
                case "created_after":
                    if (project.getCreationDate() == null || 
                            project.getCreationDate().before((java.util.Date) value)) {
                        return false;
                    }
                    break;
                case "created_before":
                    if (project.getCreationDate() == null || 
                            project.getCreationDate().after((java.util.Date) value)) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
    
    /**
     * Check if a set of comma-separated tags contains a specific tag
     */
    private boolean containsTag(List<String> tags, String targetTag) {
        if (tags == null || targetTag == null) {
            return false;
        }
        
        targetTag = targetTag.trim().toLowerCase();
        
        for (String tag : tags) {
            if (tag.trim().toLowerCase().equals(targetTag)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Simple wildcard pattern matching (supports * and ? wildcards)
     */
    private boolean wildcardMatch(String text, String pattern) {
        if (text == null || pattern == null) {
            return false;
        }
        
        // Convert wildcard pattern to regex
        String regex = Pattern.quote(pattern)
                .replace("*", "\\E.*\\Q")
                .replace("?", "\\E.\\Q");
        
        // Remove any leftover quote markers at the start/end
        regex = regex.replace("\\Q\\E", "");
        if (regex.startsWith("\\Q")) {
            regex = regex.substring(2);
        }
        if (regex.endsWith("\\E")) {
            regex = regex.substring(0, regex.length() - 2);
        }
        
        return text.matches(regex);
    }
    
    /**
     * Search and provide suggestions while typing (autocomplete functionality)
     * @param partialQuery The partial query typed by the user
     * @param userId The ID of the user performing the search
     * @param limit Maximum number of suggestions to return
     * @return Map of suggestion text to entity type
     */
    public Map<String, String> getSuggestions(String partialQuery, Integer userId, int limit) throws SQLException {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        
        String normalizedQuery = partialQuery.toLowerCase().trim();
        Map<String, String> suggestions = new HashMap<>();
        
        // Get task titles that match the query
        List<Task> matchingTasks = taskDAO.findTasksWithTitleContaining(normalizedQuery, userId, limit);
        for (Task task : matchingTasks) {
            suggestions.put(task.getTitle(), "TASK");
            
            if (suggestions.size() >= limit) {
                return suggestions;
            }
        }
        
        // Get project names that match the query
        List<Project> matchingProjects = projectDAO.findProjectsWithNameContaining(normalizedQuery, userId, limit);
        for (Project project : matchingProjects) {
            suggestions.put(project.getName(), "PROJECT");
            
            if (suggestions.size() >= limit) {
                return suggestions;
            }
        }
        
        // Get tags that match the query (if implemented)
        List<String> matchingTags = taskDAO.findTagsContaining(normalizedQuery, userId, limit);
        for (String tag : matchingTags) {
            suggestions.put(tag, "TAG");
            
            if (suggestions.size() >= limit) {
                return suggestions;
            }
        }
        
        return suggestions;
    }
    
    /**
     * Search across all entities and return results as a JSONObject
     * @param query The search query string
     * @param userId The ID of the user performing the search (for access control)
     * @return JSONObject containing search results categorized by entity type
     */
    public org.json.JSONObject searchAll(String query, Integer userId) throws SQLException {
        List<SearchResult> results = globalSearch(query, userId);
        
        org.json.JSONObject jsonResults = new org.json.JSONObject();
        org.json.JSONArray tasksArray = new org.json.JSONArray();
        org.json.JSONArray projectsArray = new org.json.JSONArray();
        org.json.JSONArray commentsArray = new org.json.JSONArray();
        org.json.JSONArray filesArray = new org.json.JSONArray();
        
        // Group results by type
        for (SearchResult result : results) {
            org.json.JSONObject item = new org.json.JSONObject();
            
            switch (result.getType()) {
                case "TASK":
                    Task task = (Task) result.getEntity();
                    item.put("id", task.getId());
                    item.put("title", task.getTitle());
                    item.put("status", task.getStatus());
                    item.put("priority", task.getPriority());
                    item.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);
                    item.put("relevanceScore", result.getRelevanceScore());
                    tasksArray.put(item);
                    break;
                    
                case "PROJECT":
                    Project project = (Project) result.getEntity();
                    item.put("id", project.getId());
                    item.put("name", project.getName());
                    item.put("status", project.getStatus());
                    item.put("creationDate", project.getCreationDate() != null ? project.getCreationDate().toString() : null);
                    item.put("relevanceScore", result.getRelevanceScore());
                    projectsArray.put(item);
                    break;
                    
                case "COMMENT":
                    Comment comment = (Comment) result.getEntity();
                    item.put("id", comment.getId());
                    item.put("content", comment.getContent());
                    item.put("taskId", comment.getTaskId());
                    item.put("authorId", comment.getUserId());
                    item.put("creationDate", comment.getCreationDate() != null ? comment.getCreationDate().toString() : null);
                    item.put("relevanceScore", result.getRelevanceScore());
                    commentsArray.put(item);
                    break;
                    
                case "FILE":
                    File file = (File) result.getEntity();
                    item.put("id", file.getId());
                    item.put("filename", file.getFilename());
                    item.put("fileSize", file.getFileSize());
                    item.put("fileType", file.getFileType());
                    item.put("uploadDate", file.getUploadDate() != null ? file.getUploadDate().toString() : null);
                    item.put("relevanceScore", result.getRelevanceScore());
                    filesArray.put(item);
                    break;
            }
        }
        
        // Add arrays to result object
        jsonResults.put("tasks", tasksArray);
        jsonResults.put("projects", projectsArray);
        jsonResults.put("comments", commentsArray);
        jsonResults.put("files", filesArray);
        
        return jsonResults;
    }
    
    /**
     * Log search queries for analytics purposes
     */
    public void logSearchQuery(String query, Integer userId, int resultCount) throws SQLException {
        // Implementation would depend on the analytics tracking system
        AnalyticsService analyticsService = new AnalyticsService();
        analyticsService.logUserActivity(userId, "SEARCH", query, resultCount);
    }
}