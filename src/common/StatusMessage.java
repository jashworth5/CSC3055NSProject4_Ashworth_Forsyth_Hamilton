package src.common;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Status message for responses from the server.
 */
public class StatusMessage extends Message{
    private boolean status;
    private String payload;

    /**
     * Create a new status message.
     * 
     * @param status  Success or failure status
     * @param payload Additional information or response data
     */
    public StatusMessage(boolean status, String payload) {
        super("Status");
        this.status = status;
        this.payload = payload;
    }

    /**
     * Create a status message from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public StatusMessage(JSONObject obj) {
        super("Status");
        this.status = (boolean) obj.get("status");
        this.payload = (String) obj.get("payload");
    }

    /**
     * Get the status (success/failure)
     * 
     * @return true if success, false if failure
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Get the payload message
     * 
     * @return The payload message
     */
    public String getPayload() {
        return payload;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("status", status);
        obj.put("payload", payload);
        return obj;
    }

    @Override
    public void deserialize(JSONType jsonType) throws IllegalArgumentException {
        if (jsonType instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonType;
            this.status = (boolean) obj.get("status");
            this.payload = (String) obj.get("payload");
        } else {
            throw new IllegalArgumentException("Invalid JSONType for deserialization");
        }
    }

    @Override
    public JSONType toJSONType() {
        throw new UnsupportedOperationException("Unimplemented method 'toJSONType'");
    }
}