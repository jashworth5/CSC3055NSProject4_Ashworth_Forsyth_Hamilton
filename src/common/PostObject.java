package src.common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a single stored post in the bulletin board
 * not a message type and should not include a type field
 */
public class PostObject implements JSONSerializable {

    private String user;
    private String message;
    private String wrappedkey;
    private String iv;

    public PostObject(String user, String message, String wrappedkey, String iv) {
        this.user = user;
        this.message = message;
        this.wrappedkey = wrappedkey;
        this.iv = iv;
    }

    public PostObject() {
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
     * converts this post to a json object for storage in board.json
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("user", user);
        obj.put("message", message);
        obj.put("wrappedkey", wrappedkey);
        obj.put("iv", iv);
        return obj;
    }

    @Override
    /**
     * populates this post from a json object stored in board.json
     */
    public void deserialize(JSONType obj) throws InvalidObjectException {
        if (!(obj instanceof JSONObject)) {
            throw new InvalidObjectException("expected json object");
        }

        JSONObject json = (JSONObject) obj;

        this.user = (String) json.get("user");
        this.message = (String) json.get("message");
        this.wrappedkey = (String) json.get("wrappedkey");
        this.iv = (String) json.get("iv");
    }
}