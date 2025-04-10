package common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a client request to fetch the public key of another user
 * used before posting a message to encrypt the aes key with the receiver's elgamal key
 */
public class PubKeyRequestMessage implements JSONSerializable {

    private static final String TYPE = "PubKeyRequest";

    private String user;

    public PubKeyRequestMessage(String user) {
        this.user = user;
    }

    public PubKeyRequestMessage() {
        // empty constructor for deserialization
    }

    public String getUser() {
        return user;
    }

    @Override
    /**
     * converts this message into a json object to send to the server
     * includes the required type field and the requested username
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
     * validates the type field and extracts the requested username
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
