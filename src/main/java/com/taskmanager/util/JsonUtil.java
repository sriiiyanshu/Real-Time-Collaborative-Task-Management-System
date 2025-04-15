package com.taskmanager.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.taskmanager.model.Comment;

/**
 * Utility class for handling JSON operations.
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Converts a ResultSet to a JSON array string.
     * 
     * @param rs The ResultSet to convert
     * @return A JSON array string containing the ResultSet data
     * @throws SQLException If a database access error occurs
     */
    public static String resultSetToJsonArray(ResultSet rs) throws SQLException, JsonProcessingException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            
            rows.add(row);
        }
        
        return objectMapper.writeValueAsString(rows);
    }
    
    /**
     * Safely gets a string value from a JsonNode.
     * 
     * @param json The JsonNode to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted string value or the default
     */
    public static String getString(JsonNode json, String key, String defaultValue) {
        if (json.has(key) && !json.get(key).isNull()) {
            return json.get(key).asText();
        }
        return defaultValue;
    }
    
    /**
     * Safely gets an int value from a JsonNode.
     * 
     * @param json The JsonNode to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted int value or the default
     */
    public static int getInt(JsonNode json, String key, int defaultValue) {
        if (json.has(key) && !json.get(key).isNull()) {
            return json.get(key).asInt();
        }
        return defaultValue;
    }
    
    /**
     * Safely gets a boolean value from a JsonNode.
     * 
     * @param json The JsonNode to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted boolean value or the default
     */
    public static boolean getBoolean(JsonNode json, String key, boolean defaultValue) {
        if (json.has(key) && !json.get(key).isNull()) {
            return json.get(key).asBoolean();
        }
        return defaultValue;
    }
    
    /**
     * Converts a List of Maps to a JSON string.
     * 
     * @param list The List of Maps to convert
     * @return A JSON string representation of the list
     */
    public static String listToJsonArray(List<Map<String, Object>> list) throws JsonProcessingException {
        return objectMapper.writeValueAsString(list);
    }
    
    /**
     * Merges two JsonNode objects, with values from the second object overriding values from the first.
     * 
     * @param json1 The first JsonNode
     * @param json2 The second JsonNode (takes precedence)
     * @return The merged JsonNode
     */
    public static JsonNode mergeJsonObjects(JsonNode json1, JsonNode json2) {
        if (!(json1 instanceof ObjectNode) || !(json2 instanceof ObjectNode)) {
            return json2;
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        json1.fieldNames().forEachRemaining(fieldName -> result.set(fieldName, json1.get(fieldName)));
        json2.fieldNames().forEachRemaining(fieldName -> result.set(fieldName, json2.get(fieldName)));
        
        return result;
    }
    
    /**
     * Converts a Comment object to a JSON string.
     * 
     * @param comment The Comment object to convert
     * @return A JSON string representation of the Comment
     */
    public static String toJson(Comment comment) throws JsonProcessingException {
        if (comment == null) {
            return "{}";
        }
        
        return objectMapper.writeValueAsString(comment);
    }
    
    /**
     * Creates a JSON error response.
     * 
     * @param message The error message
     * @param errorCode The error code
     * @return A JSON string representing the error
     */
    public static String createErrorResponse(String message, int errorCode) throws JsonProcessingException {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("success", false);
        error.put("message", message);
        error.put("errorCode", errorCode);
        return objectMapper.writeValueAsString(error);
    }
    
    /**
     * Creates a JSON success response.
     * 
     * @param message The success message
     * @param data The data to include
     * @return A JSON string representing the success response
     */
    public static String createSuccessResponse(String message, Object data) throws JsonProcessingException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.put("message", message);
        
        if (data != null) {
            response.set("data", objectMapper.valueToTree(data));
        }
        
        return objectMapper.writeValueAsString(response);
    }
    
    /**
     * Parses JSON from an HTTP request into an object of the specified class.
     *
     * @param <T> The type of object to return
     * @param request The HTTP request containing JSON data
     * @param clazz The class to parse the JSON into
     * @return An object of the specified class
     * @throws IOException If an I/O error occurs
     */
    public <T> T parseJsonRequest(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        String requestBody = sb.toString();
        if (requestBody == null || requestBody.trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is empty");
        }
        
        return objectMapper.readValue(requestBody, clazz);
    }
    
    /**
     * Writes a list of objects as JSON to an HTTP response.
     *
     * @param <T> The type of objects in the list
     * @param response The HTTP response to write to
     * @param list The list of objects to convert to JSON
     * @throws IOException If an I/O error occurs
     */
    public <T> void writeJsonResponse(HttpServletResponse response, List<T> list) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonString = objectMapper.writeValueAsString(list);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonString);
            writer.flush();
        }
    }
    
    /**
     * Writes a single object as JSON to an HTTP response.
     *
     * @param <T> The type of the object
     * @param response The HTTP response to write to
     * @param object The object to convert to JSON
     * @throws IOException If an I/O error occurs
     */
    public <T> void writeJsonResponse(HttpServletResponse response, T object) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonString = objectMapper.writeValueAsString(object);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonString);
            writer.flush();
        }
    }
}