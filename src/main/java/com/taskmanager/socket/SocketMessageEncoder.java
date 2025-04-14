package com.taskmanager.socket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import org.json.JSONObject;

/**
 * Encoder for WebSocket messages.
 * Converts message objects to JSON strings for transmission.
 */
public class SocketMessageEncoder implements Encoder.Text<SocketMessage> {

    @Override
    public String encode(SocketMessage message) throws EncodeException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", message.getType());
        jsonObj.put("content", message.getContent());
        jsonObj.put("sender", message.getSender());
        jsonObj.put("timestamp", message.getTimestamp());
        
        if (message.getRecipient() != null) {
            jsonObj.put("recipient", message.getRecipient());
        }
        
        if (message.getObjectId() != null) {
            jsonObj.put("objectId", message.getObjectId());
        }
        
        return jsonObj.toString();
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