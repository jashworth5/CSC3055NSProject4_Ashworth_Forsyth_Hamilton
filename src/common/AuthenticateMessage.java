package common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * represents a client request to authenticate with username password and otp
 * includes a plaintext password and time-based one time password
 */
public class AuthenticateMessage implements JSONSerializable {

    private static final String TYPE = "authenticate";

    private String user;
    private String pass;
    private String otp;

    public AuthenticateMessage(String user, String pass, String otp) {
        this.user = user;
        this.pass = pass;
        this.otp = otp;
    }

    public AuthenticateMessage() {
        // used for deserialization
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getOtp() {
        return otp;
    }

    @Override
    /**
     * converts this message into a json object to send to the server
     * includes the required type field along with username password and otp
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE);
        obj.put("user", user);
        obj.put("pass", pass);
        obj.put("otp", otp);
        return obj;
    }

    @Override
    /**
     * populates this message from a received json object
     * validates the type field and extracts username password and otp
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
        this.otp = (String) json.get("otp");
    }
}
