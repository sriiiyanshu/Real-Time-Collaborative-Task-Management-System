package com.taskmanager.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import com.taskmanager.model.Comment;

/**
 * Utility class for handling JSON operations.
 */
public class JsonUtil {

    /**
     * Converts a ResultSet to a JSONArray.
     * 
     * @param rs The ResultSet to convert
     * @return A JSONArray containing the ResultSet data
     * @throws SQLException If a database access error occurs
     */
    public static JSONArray resultSetToJsonArray(ResultSet rs) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            JSONObject jsonObject = new JSONObject();
            
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = rs.getObject(i);
                
                // Handle null values
                if (value == null) {
                    jsonObject.put(columnName, JSONObject.NULL);
                } else {
                    jsonObject.put(columnName, value);
                }
            }
            
            jsonArray.put(jsonObject);
        }
        
        return jsonArray;
    }
    
    /**
     * Safely gets a string value from a JSONObject.
     * 
     * @param json The JSONObject to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted string value or the default
     */
    public static String getString(JSONObject json, String key, String defaultValue) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getString(key);
            }
        } catch (JSONException e) {
            LogUtil.error("Error getting string value for key: " + key, e);
        }
        return defaultValue;
    }
    
    /**
     * Safely gets an int value from a JSONObject.
     * 
     * @param json The JSONObject to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted int value or the default
     */
    public static int getInt(JSONObject json, String key, int defaultValue) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getInt(key);
            }
        } catch (JSONException e) {
            LogUtil.error("Error getting int value for key: " + key, e);
        }
        return defaultValue;
    }
    
    /**
     * Safely gets a boolean value from a JSONObject.
     * 
     * @param json The JSONObject to extract from
     * @param key The key to look for
     * @param defaultValue The default value if the key isn't found
     * @return The extracted boolean value or the default
     */
    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
        try {
            if (json.has(key) && !json.isNull(key)) {
                return json.getBoolean(key);
            }
        } catch (JSONException e) {
            LogUtil.error("Error getting boolean value for key: " + key, e);
        }
        return defaultValue;
    }
    
    /**
     * Converts a List of Maps to a JSONArray.
     * 
     * @param list The List of Maps to convert
     * @return A JSONArray representation of the list
     */
    public static JSONArray listToJsonArray(List<Map<String, Object>> list) {
        JSONArray jsonArray = new JSONArray();
        
        for (Map<String, Object> map : list) {
            JSONObject jsonObject = new JSONObject();
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null) {
                    jsonObject.put(key, JSONObject.NULL);
                } else {
                    jsonObject.put(key, value);
                }
            }
            
            jsonArray.put(jsonObject);
        }
        
        return jsonArray;
    }
    
    /**
     * Merges two JSONObjects, with values from the second object overriding values from the first.
     * 
     * @param json1 The first JSONObject
     * @param json2 The second JSONObject (takes precedence)
     * @return The merged JSONObject
     */
    public static JSONObject mergeJsonObjects(JSONObject json1, JSONObject json2) {
        JSONObject merged = new JSONObject();
        
        // Copy all keys from json1
        Iterator<String> keys = json1.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            merged.put(key, json1.get(key));
        }
        
        // Copy and override with keys from json2
        keys = json2.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            merged.put(key, json2.get(key));
        }
        
        return merged;
    }
    
    /**
     * Converts a Comment object to a JSON string.
     * 
     * @param comment The Comment object to convert
     * @return A JSON string representation of the Comment
     */
    public static String toJson(Comment comment) {
        if (comment == null) {
            return "{}";
        }
        
        JSONObject json = new JSONObject();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        
        json.put("id", comment.getId());
        json.put("taskId", comment.getTaskId());
        json.put("userId", comment.getUserId());
        json.put("content", comment.getContent());
        
        if (comment.getCreationDate() != null) {
            json.put("creationDate", dateFormat.format(comment.getCreationDate()));
        }
        
        if (comment.getLastModified() != null) {
            json.put("lastModified", dateFormat.format(comment.getLastModified()));
        }
        
        return json.toString();
    }
    
    /**
     * Creates a JSON error response.
     * 
     * @param message The error message
     * @param errorCode The error code
     * @return A JSONObject representing the error
     */
    public static JSONObject createErrorResponse(String message, int errorCode) {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("message", message);
        error.put("errorCode", errorCode);
        return error;
    }
    
    /**
     * Creates a JSON success response.
     * 
     * @param message The success message
     * @param data The data to include (can be JSONObject or JSONArray)
     * @return A JSONObject representing the success response
     */
    public static JSONObject createSuccessResponse(String message, Object data) {
        JSONObject response = new JSONObject();
        response.put("success", true);
        response.put("message", message);
        
        if (data != null) {
            response.put("data", data);
        }
        
        return response;
    }
}