package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import merrimackutil.json.types.JSONObject;
import util.TotpVerifier;
import util.EncryptionUtil;

import com.fasterxml.jackson.core.type.TypeReference;
// could not achieve correct saving of user JSON objects onto file without this JACKSON
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


//import src.util.TotpVerifier; 
//import src.util.CryptoUtils; 

public class UserDatabase {
    private final File userFile;
    private final Map<String, Map<String, Object>> users = new HashMap<>();


    public UserDatabase(String userFilePath) throws IOException {
        this.userFile = new File(userFilePath);
        loadUsers();
    }

   
    /**
     * Loads the user JSON database from disk into memory using Jackson.
     */
    private void loadUsers() throws IOException {
        if (!userFile.exists()) {
            userFile.createNewFile();
            try (FileWriter writer = new FileWriter(userFile)) {
                writer.write("{\"entries\": []}");
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(userFile, new TypeReference<>() {});

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userList = (List<Map<String, Object>>) root.get("entries");

        for (Map<String, Object> userObj : userList) {
            String username = (String) userObj.get("user");
            users.put(username, userObj);
        }
    }   


    /**
     * Writes the in-memory users map back to disk.
     */
    private void saveUsers() throws IOException {
        // Create the wrapper object: { "entries": [...] }
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("entries", new ArrayList<>(users.values()));

        // Pretty print JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(userFile, wrapper);
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
            byte[] salt = EncryptionUtil.generateRandomBytes(16);

            // === TODO: hash password with SCRYPT using parameters from spec
            byte[] hashedPassword = EncryptionUtil.scryptHash(password, salt);

            // === TODO: generate random TOTP key (e.g., 160 bits)
            byte[] totpKey = EncryptionUtil.generateRandomBytes(20);

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
            Map<String, Object> user = users.get(username);
    
            String saltStr = String.valueOf(user.get("salt"));
            String passStr = String.valueOf(user.get("pass"));
            
            System.out.println("Raw stored password string: " + passStr);
            System.out.println("Raw salt string: " + saltStr);
            
            byte[] salt = Base64.getDecoder().decode(saltStr);
            byte[] storedHash = Base64.getDecoder().decode(passStr);
            byte[] computed = EncryptionUtil.scryptHash(passwordAttempt, salt);
            
            System.out.println("Stored hash: " + Base64.getEncoder().encodeToString(storedHash));
            System.out.println("Computed: " + Base64.getEncoder().encodeToString(computed));
            System.out.println("Match: " + java.util.Arrays.equals(storedHash, computed));
            
            return java.util.Arrays.equals(storedHash, computed);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * Validates a TOTP token using stored TOTP key.
     */
    public boolean validateTOTP(String username, String otpCode) {
        if (!userExists(username)) return false;
    
        try {
            String base64TotpKey = String.valueOf(users.get(username).get("totp-key"));
            byte[] totpKey = Base64.getDecoder().decode(base64TotpKey);
    
            String expected = TotpVerifier.getCurrentCode(totpKey);
            System.out.println("User OTP: " + otpCode);
            System.out.println("Expected OTP (now): " + expected);
    
            return TotpVerifier.isValidCode(totpKey, otpCode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * Returns the base64-encoded TOTP key for user creation response.
     */
    public String getTotpKey(String username) {
        if (!userExists(username)) return null;
        return (String) users.get(username).get("totp-key");
    }
}
