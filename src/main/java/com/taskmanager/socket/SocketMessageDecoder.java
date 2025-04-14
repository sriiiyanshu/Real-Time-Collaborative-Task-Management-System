package com.taskmanager.socket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import org.json.JSONObject;

/**
 * Decoder for WebSocket messages.
 * Converts JSON strings to message objects.
 */
public class SocketMessageDecoder implements Decoder.Text<SocketMessage> {

    @Override
    public SocketMessage decode(String s) throws DecodeException {
        JSONObject jsonObject = new JSONObject(s);
        
        SocketMessage message = new SocketMessage();
        message.setType(jsonObject.getString("type"));
        message.setContent(jsonObject.getString("content"));
        message.setSender(jsonObject.getString("sender"));
        
        if (jsonObject.has("timestamp")) {
            message.setTimestamp(jsonObject.getLong("timestamp"));
        } else {
            message.setTimestamp(System.currentTimeMillis());
        }
        
        if (jsonObject.has("recipient") && !jsonObject.isNull("recipient")) {
            message.setRecipient(jsonObject.getString("recipient"));
        }
        
        if (jsonObject.has("objectId") && !jsonObject.isNull("objectId")) {
            message.setObjectId(jsonObject.getInt("objectId"));
        }
        
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        try {
            // Check if the string is valid JSON with required fields
            JSONObject jsonObject = new JSONObject(s);
            return jsonObject.has("type") && 
                   jsonObject.has("content") && 
                   jsonObject.has("sender");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void init(EndpointConfig config) {
        // Initialization logic if needed
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}