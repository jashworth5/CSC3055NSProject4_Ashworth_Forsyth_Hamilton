package src.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.common.Post;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;


public class BulletinBoard {
    private final File boardFile;
    private final Map<String, List<Post>> userPosts; // Maps usernames to their posts

    /**
     * Constructs a new BulletinBoard object.
     * 
     * @param boardFile The file to store the bulletin board data.
     */
    public BulletinBoard(String filePath) {
        this.boardFile = new File(filePath);
        this.userPosts = new HashMap<>();

        // Load existing posts from the file if it exists
        if (boardFile.exists()) {
            try {
                JSONObject root = JsonIO.readObject(boardFile);
                JSONArray postsArray = (JSONArray) root.get("posts");

                for (int i = 0; i < postsArray.size(); i++) {
                    JSONObject postObj = (JSONObject) postsArray.get(i);
                    Post post = new Post(postObj);
                    addToMemory(post);
                }

                System.out.println("[+] Loaded " + postsArray.size() + " posts from bulletin board.");
            } catch (Exception e) {
                System.err.println("[!] Failed to load bulletin board: " + e.getMessage());
            }
        } else {
            // Create empty bulletin board
            saveBulletinBoard();
        }
    }

    private void addToMemory(Post post) {
        String username = post.getUser();
        if(!userPosts.containsKey(username)) {
            userPosts.put(username, new ArrayList<>());
        }
        userPosts.get(username).add(post);
    }

    /**
     * Adds a new post to the bulletin board.
     * 
     * @param post The post to add.
     */
    public boolean addPost(Post post) {
        addToMemory(post);
        return saveBulletinBoard();
    }

    /**
     * Get all posts from the user
     * 
     * @param username The username of the user whose posts to retrieve
     * @return List of posts from the user
     */
     public Post[] getPosts(String username) {
        List<Post> posts = userPosts.getOrDefault(username, new ArrayList<>());
        return posts.toArray(new Post[0]);
    }

    private boolean saveBulletinBoard() {
        try {
            JSONObject root = new JSONObject();
            JSONArray postsArray = new JSONArray();

            // Add all posts to the array
            for (List<Post> posts : userPosts.values()) {
                for (Post post : posts) {
                    postsArray.add(post.toJSONObject());
                }
            }

            root.put("posts", postsArray);

            // Create parent directories if they don't exist
            boardFile.getParentFile().mkdirs();

            // Write to file
            try (FileWriter writer = new FileWriter(boardFile)) {
                writer.write(root.toString());
            }

            return true;
        } catch (IOException e) {
            System.err.println("[!] Failed to save bulletin board: " + e.getMessage());
            return false;
        }
    }
}
