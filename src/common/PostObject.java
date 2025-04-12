package common;

import com.fasterxml.jackson.annotation.JsonProperty;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

import java.io.InvalidObjectException;

/**
 * represents a single stored post in the bulletin board
 * not a message type and should not include a type field
 */
public class PostObject implements JSONSerializable {

    @JsonProperty("user")
    private String user;

    @JsonProperty("message")
    private String message;

    @JsonProperty("wrappedkey")
    private String wrappedkey;

    @JsonProperty("iv")
    private String iv;

    // default constructor for Jackson and deserialization
    public PostObject() {}

    // constructor with @JsonProperty to prevent camelCase issues
    public PostObject(
        @JsonProperty("user") String user,
        @JsonProperty("message") String message,
        @JsonProperty("wrappedkey") String wrappedkey,
        @JsonProperty("iv") String iv
    ) {
        this.user = user;
        this.message = message;
        this.wrappedkey = wrappedkey;
        this.iv = iv;
    }

    // getters
    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    @JsonProperty("wrappedkey")
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
