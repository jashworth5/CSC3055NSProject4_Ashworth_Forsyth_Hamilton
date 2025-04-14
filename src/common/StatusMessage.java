package src.common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents the server's response message with a boolean status and a payload string
 */
public class StatusMessage implements JSONSerializable {

    private static final String TYPE = "Status"; 

    private boolean status;
    private String payload;

    public StatusMessage(boolean status, String payload) {
        this.status = status;
        this.payload = payload;
    }

    public StatusMessage() {
        // used during deserialization
    }

    public boolean getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    
    @Override
    /**
     * converts this message into a json object that can be sent over the network
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE); 
        obj.put("status", status);
        obj.put("payload", payload);
        return obj;
    }
    

    @Override
    /**
     * populates this message from a received json object
     * validates the type field and extracts the status and payload fields
     */
    public void deserialize(JSONType obj) throws InvalidObjectException {
        if (!(obj instanceof JSONObject)) {
            throw new InvalidObjectException("expected json object");
        }

        JSONObject json = (JSONObject) obj;

        Object type = json.get("type");
        if (type == null || !TYPE.equals(type)) {
            throw new InvalidObjectException("invalid or missing type field");
        }

        this.status = (Boolean) json.get("status");
        this.payload = (String) json.get("payload");
    }
}
