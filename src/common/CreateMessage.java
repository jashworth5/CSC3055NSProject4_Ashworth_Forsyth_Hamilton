package src.common;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;


/**
 * Message for creating a new user account.
 */
public class CreateMessage extends Message {

    private String user;
    private String pass;
    private String pubkey;

    /**
     * Create a new account creation message.
     * 
     * @param user   The username
     * @param pass   The password (plaintext, will be hashed on server)
     * @param pubkey The base64 encoded public key
     */
    public CreateMessage(String user, String pass, String pubkey) {
        super("Create");
        this.user = user;
        this.pass = pass;
        this.pubkey = pubkey;
    }

    /**
     * Create a message from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public CreateMessage(JSONObject obj) {
        super("Create");
        this.user = (String) obj.get("user");
        this.pass = (String) obj.get("pass");
        this.pubkey = (String) obj.get("pubkey");
    }

    /**
     * Get the username
     * 
     * @return The username
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the password
     * 
     * @return The password
     */
    public String getPass() {
        return pass;
    }

    /**
     * Get the public key
     * 
     * @return The public key
     */
    public String getPubkey() {
        return pubkey;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("user", user);
        obj.put("pass", pass);
        obj.put("pubkey", pubkey);
        return obj;
    }

    @Override
    public JSONType toJSONType() {
        return this.toJSONObject();
    }

    @Override
    public void deserialize(JSONType jsonType) {
        if (jsonType instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonType;
            this.user = (String) obj.get("user");
            this.pass = (String) obj.get("pass");
            this.pubkey = (String) obj.get("pubkey");
        } else {
            throw new IllegalArgumentException("Invalid JSONType for deserialization");
        }
    }

}
