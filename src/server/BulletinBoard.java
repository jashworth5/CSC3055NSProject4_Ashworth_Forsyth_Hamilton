package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import common.PostObject;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class BulletinBoard {
    private final File boardFile;
    private final Map<String, List<PostObject>> userPosts;

    public BulletinBoard(String filePath) {
        this.boardFile = new File(filePath);
        this.userPosts = new HashMap<>();

        if (boardFile.exists()) {
            try {
                JSONObject root = JsonIO.readObject(boardFile);
                JSONArray postsArray = (JSONArray) root.get("posts");

                for (int i = 0; i < postsArray.size(); i++) {
                    PostObject post = new PostObject();
                    post.deserialize((JSONType) postsArray.get(i));
                    addToMemory(post);
                }

                System.out.println("[+] Loaded " + postsArray.size() + " posts from bulletin board.");
            } catch (Exception e) {
                System.err.println("[!] Failed to load bulletin board: " + e.getMessage());
            }
        } else {
            saveBulletinBoard();
        }
    }

    private void addToMemory(PostObject post) {
        String username = post.getUser();
        userPosts.computeIfAbsent(username, k -> new ArrayList<>()).add(post);
    }

    public boolean addPost(PostObject post) {
        addToMemory(post);
        return saveBulletinBoard();
    }

    public List<PostObject> getPosts(String username) {
        return userPosts.getOrDefault(username, new ArrayList<>());
    }

    private boolean saveBulletinBoard() {
        try {
            JSONObject root = new JSONObject();
            JSONArray postsArray = new JSONArray();

            for (List<PostObject> posts : userPosts.values()) {
                for (PostObject post : posts) {
                    postsArray.add(post.toJSONType());
                }
            }

            root.put("posts", postsArray);

            try (FileWriter writer = new FileWriter(boardFile)) {
                writer.write(root.toString());
                System.out.println("[+] Saved bulletin board to disk.");
            }

            return true;
        } catch (IOException e) {
            System.err.println("[!] Failed to save bulletin board: " + e.getMessage());
            return false;
        }
    }
}
