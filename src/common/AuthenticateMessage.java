package src.common;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;


/**
 * Message for user authentication.
 */
public class AuthenticateMessage extends Message {
    private String user;
    private String pass;
    private String otp;

    /**
     * Create a new authentication message.
     * 
     * @param user The username
     * @param pass The password
     * @param otp  The one-time password (TOTP)
     */
    public AuthenticateMessage(String user, String pass, String otp) {
        super("authenticate");
        this.user = user;
        this.pass = pass;
        this.otp = otp;
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
            this.otp = (String) obj.get("otp");
        } else {
            throw new IllegalArgumentException("Invalid JSONType for deserialization");
        }
    }

    /**
     * Create a message from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public AuthenticateMessage(JSONObject obj) {
        super("authenticate");
        this.user = (String) obj.get("user");
        this.pass = (String) obj.get("pass");
        this.otp = (String) obj.get("otp");
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
     * Get the one-time password
     * 
     * @return The OTP
     */
    public String getOtp() {
        return otp;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("user", user);
        obj.put("pass", pass);
        obj.put("otp", otp);
        return obj;
    }
}