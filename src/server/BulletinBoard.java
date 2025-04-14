package server;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import common.PostObject;

public class BulletinBoard {
    private final File boardFile;
    private final Map<String, List<PostObject>> userPosts;

    public BulletinBoard(String filePath) {
        this.boardFile = new File(filePath);
        this.userPosts = new HashMap<>();
        loadBulletinBoard();
    }

    /**
     * Loads the posts from the board file using Jackson.
     */
    private void loadBulletinBoard() {
        if (!boardFile.exists()) {
            saveBulletinBoard(); // create empty file if missing
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            // read file into { "posts": [ ... ] }
            Map<String, List<PostObject>> root = mapper.readValue(
                boardFile, new TypeReference<Map<String, List<PostObject>>>() {}
            );

            List<PostObject> posts = root.get("posts");
            if (posts != null) {
                for (PostObject post : posts) {
                    addToMemory(post);
                }
            }

            System.out.println("[+] Loaded " + posts.size() + " posts from bulletin board.");
        } catch (Exception e) {
            System.err.println("[!] Failed to load bulletin board: " + e.getMessage());
        }
    }

    /**
     * Saves all posts to the board file using Jackson formatting.
     */
    private boolean saveBulletinBoard() {
        try {
            // collect all posts into a flat list
            List<PostObject> allPosts = new ArrayList<>();
            for (List<PostObject> posts : userPosts.values()) {
                allPosts.addAll(posts);
            }

            // wrap in a map for { "posts": [...] }
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("posts", allPosts);

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(boardFile, wrapper);

            return true;
        } catch (IOException e) {
            System.err.println("[!] Failed to save bulletin board: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a post to the in-memory map and writes to disk.
     */
    public boolean addPost(PostObject post) {
        addToMemory(post);
        return saveBulletinBoard();
    }

    /**
     * Gets all posts from a specific user.
     */
    public List<PostObject> getPosts(String username) {
        return userPosts.getOrDefault(username, new ArrayList<>());
    }

    /**
     * Adds a post to the in-memory map only.
     */
    private void addToMemory(PostObject post) {
        String username = post.getUser();
        userPosts.computeIfAbsent(username, k -> new ArrayList<>()).add(post);
    }
}
