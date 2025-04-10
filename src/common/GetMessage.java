package common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a client request to retrieve all messages addressed to a specific user
 */
public class GetMessage implements JSONSerializable {

    private static final String TYPE = "GetMessage";

    private String user;

    public GetMessage(String user) {
        this.user = user;
    }

    public GetMessage() {
        // used for deserialization
    }

    public String getUser() {
        return user;
    }

    @Override
    /**
     * converts this message into a json object to send to the server
     * includes the required type field and the username field
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE);
        obj.put("user", user);
        return obj;
    }

    @Override
    /**
     * populates this message from a received json object
     * validates the type field and extracts the username field
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

        this.user = (String) json.get("user");
    }
}
