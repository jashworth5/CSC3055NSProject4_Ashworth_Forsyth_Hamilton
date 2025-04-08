package src.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

public class UserDatabase {
    private final File file;
    private final Map<String, JSONObject> users;

    public UserDatabase(String filePath) {
        this.file = new File(filePath);
        this.users = new HashMap<>();

        if (file.exists()) {
            try {
                JSONObject root = JsonIO.readObject(file);
                JSONArray userArray = (JSONArray) root.get("users");

                for (int i = 0; i < userArray.size(); i++) {
                    JSONObject userObj = (JSONObject) userArray.get(i);
                    String username = (String) userObj.get("user");
                    users.put(username, userObj);
                }

                System.out.println("[+] Loaded " + users.size() + " user(s) from database.");
            } catch (Exception e) {
                System.err.println("[!] Failed to load user database: " + e.getMessage());
            }
        }
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public JSONObject getUser(String username) {
        return users.get(username);
    }
}
