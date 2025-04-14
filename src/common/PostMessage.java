package common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a client request to post an encrypted message to the bulletin board
 * includes the encrypted message ciphertext, a wrapped aes key, and the encryption iv
 */
public class PostMessage implements JSONSerializable {

    private static final String TYPE = "Post";
    
    private String user;
    private String message;
    private String wrappedkey;
    private String iv;

    public PostMessage(String user, String message, String wrappedkey, String iv) {
        this.user = user;
        this.message = message;
        this.wrappedkey = wrappedkey;
        this.iv = iv;
    }

    public PostMessage() {
        // used for deserialization
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public String getWrappedKey() {
        return wrappedkey;
    }

    public String getIv() {
        return iv;
    }

    @Override
    /**
     * converts this message into a json object to send to the server
     * includes the required type field and all encrypted post components
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE);
        obj.put("user", user);
        obj.put("message", message);
        obj.put("wrappedkey", wrappedkey);
        obj.put("iv", iv);
        return obj;
    }

    @Override
    /**
     * populates this message from a received json object
     * validates the type field and extracts the post fields
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
        this.message = (String) json.get("message");
        this.wrappedkey = (String) json.get("wrappedkey");
        this.iv = (String) json.get("iv");
    }
}
