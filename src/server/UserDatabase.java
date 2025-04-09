package server;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// imports added later

//import src.util.TotpVerifier; 
//import src.util.CryptoUtils; 

public class UserDatabase {
    private final File userFile;
    private final Map<String, JSONObject> users = new HashMap<>();

    public UserDatabase(String userFilePath) throws IOException {
        this.userFile = new File(userFilePath);
        loadUsers();
    }

    /**
     * Loads the user JSON database from disk into memory.
     */
    private void loadUsers() throws IOException {
        if (!userFile.exists()) {
            userFile.createNewFile();
            JsonIO.writeObject(userFile, new JSONArray());  // empty list of users
        }

        JSONArray userArray = JsonIO.readArray(userFile);
        for (int i = 0; i < userArray.size(); i++) {
            JSONObject userObj = (JSONObject) userArray.get(i);
            users.put((String) userObj.get("user"), userObj);
        }
    }

    /**
     * Writes the in-memory users map back to disk.
     */
    private void saveUsers() throws IOException {
        JSONArray array = new JSONArray();
        for (JSONObject obj : users.values()) {
            array.add(obj);
        }
        JsonIO.writeArray(userFile, array);
    }

    /**
     * Checks if a user already exists.
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Creates a new user with a hashed password, salt, pubkey, and TOTP key.
     */
    public boolean createUser(String username, String password, String base64PublicKey) {
        if (userExists(username)) return false;

        try {
            // === TODO: generate random 128-bit salt
            byte[] salt = CryptoUtils.generateRandomBytes(16);

            // === TODO: hash password with SCRYPT using parameters from spec
            byte[] hashedPassword = CryptoUtils.scryptHash(password, salt);

            // === TODO: generate random TOTP key (e.g., 160 bits)
            byte[] totpKey = CryptoUtils.generateRandomBytes(20);

            JSONObject userObj = new JSONObject();
            userObj.put("user", username);
            userObj.put("pass", Base64.getEncoder().encodeToString(hashedPassword));
            userObj.put("salt", Base64.getEncoder().encodeToString(salt));
            userObj.put("totp-key", Base64.getEncoder().encodeToString(totpKey));
            userObj.put("pubkey", base64PublicKey);

            users.put(username, userObj);
            saveUsers();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the user's public key (base64).
     */
    public String getPublicKey(String username) {
        if (!userExists(username)) return null;
        return (String) users.get(username).get("pubkey");
    }

    /**
     * Verifies password against stored SCRYPT hash.
     */
    public boolean validatePassword(String username, String passwordAttempt) {
        if (!userExists(username)) return false;

        try {
            JSONObject user = users.get(username);
            byte[] salt = Base64.getDecoder().decode((String) user.get("salt"));
            byte[] storedHash = Base64.getDecoder().decode((String) user.get("pass"));

            byte[] computed = CryptoUtils.scryptHash(passwordAttempt, salt);

            return java.util.Arrays.equals(storedHash, computed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates a TOTP token using stored TOTP key.
     */
    public boolean validateTOTP(String username, String otpCode) {
        if (!userExists(username)) return false;

        String base64TotpKey = (String) users.get(username).get("totp-key");
        byte[] totpKey = Base64.getDecoder().decode(base64TotpKey);

        return TotpVerifier.isValidCode(totpKey, otpCode);
    }

    /**
     * Returns the base64-encoded TOTP key for user creation response.
     */
    public String getTotpKey(String username) {
        if (!userExists(username)) return null;
        return (String) users.get(username).get("totp-key");
    }
}
