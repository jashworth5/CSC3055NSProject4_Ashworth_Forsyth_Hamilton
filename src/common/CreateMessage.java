package src.common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a client request to create a new user account
 * includes a username, plaintext password, and base64-encoded public key
 */
public class CreateMessage implements JSONSerializable {

    private static final String TYPE = "Create";

    private String user;
    private String pass;
    private String pubkey;

    public CreateMessage(String user, String pass, String pubkey) {
        this.user = user;
        this.pass = pass;
        this.pubkey = pubkey;
    }

    public CreateMessage() {
        // used for deserialization
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getPubkey() {
        return pubkey;
    }

    @Override
    /**
     * converts this message into a json object that can be sent to the server
     * includes the required type field along with username, password, and public key
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE);
        obj.put("user", user);
        obj.put("pass", pass);
        obj.put("pubkey", pubkey);
        return obj;
    }

    @Override
    /**
     * populates this message from a received json object
     * validates the type field and extracts the username, password, and public key fields
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
        this.pass = (String) json.get("pass");
        this.pubkey = (String) json.get("pubkey");
    }
}
