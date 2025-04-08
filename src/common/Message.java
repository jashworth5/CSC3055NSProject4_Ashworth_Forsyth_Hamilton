package src.common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;


public  abstract class Message implements JSONSerializable {
    
    protected String type;

    /**
     * Create a new message with the specified type.
     * 
     * @param type The type of message
     */
    public Message(String type) {
        this.type = type;
    }

    /**
     * Get the type of message.
     * 
     * @return The message type
     */
    public String getType() {
        return type;
    }

    /**
     * Convert the message to a JSONObject for serialization.
     * 
     * @return JSONObject representation of the message
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        return obj;
    }
}

    

