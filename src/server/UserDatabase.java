package server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import merrimackutil.json.types.JSONObject;

public class UserDatabase {
    private final File file;
    private final Map<String, JSONObject> users;

    public UserDatabase(String filePath) {
        this.file = new File(filePath);
        this.users = new HashMap<>();
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}