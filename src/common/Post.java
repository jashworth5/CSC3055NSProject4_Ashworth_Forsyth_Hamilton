package src.common;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Message for posting to the bulletin board.
 */
public class Post extends Message {
    private String user;       // Receiver username
    private String message;    // Encrypted message content (base64)
    private String wrappedkey; // Wrapped key (encrypted with receiver's public key)
    private String iv;         // Initialization vector for AES/GCM (base64)

    /**
     * Create a new post message.
     * 
     * @param user       The receiver's username
     * @param message    The encrypted message content (base64)
     * @param wrappedkey The wrapped key (base64)
     * @param iv         The initialization vector (base64)
     */
    public Post(String user, String message, String wrappedkey, String iv) {
        super("post");
        this.user = user;
        this.message = message;
        this.wrappedkey = wrappedkey;
        this.iv = iv;
    }

    /**
     * Create a post from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public Post(JSONObject obj) {
        super("post");
        this.user = (String) obj.get("user");
        this.message = (String) obj.get("message");
        this.wrappedkey = (String) obj.get("wrappedkey");
        this.iv = (String) obj.get("iv");
    }

    /**
     * Get the receiver's username
     * 
     * @return The username
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the encrypted message content
     * 
     * @return The message content (base64)
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the wrapped key
     * 
     * @return The wrapped key (base64)
     */
    public String getWrappedKey() {
        return wrappedkey;
    }

    /**
     * Get the initialization vector
     * 
     * @return The IV (base64)
     */
    public String getIV() {
        return iv;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("user", user);
        obj.put("message", message);
        obj.put("wrappedkey", wrappedkey);
        obj.put("iv", iv);
        return obj;
    }
    
    @Override
    public JSONType toJSONType() {
        return this.toJSONObject();
    }
    
    @Override
    public void deserialize(JSONType json) {
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            this.user = (String) obj.get("user");
            this.message = (String) obj.get("message");
            this.wrappedkey = (String) obj.get("wrappedkey");
            this.iv = (String) obj.get("iv");
        }
    }
}

/**
 * Message for requesting a user's public key.
 */
class PubKeyRequestMessage extends Message {
    private String user;

    /**
     * Create a new public key request message.
     * 
     * @param user The username to get public key for
     */
    public PubKeyRequestMessage(String user) {
        super("PubKeyRequest");
        this.user = user;
    }

    /**
     * Create a message from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public PubKeyRequestMessage(JSONObject obj) {
        super("PubKeyRequest");
        this.user = (String) obj.get("user");
    }

    /**
     * Get the username
     * 
     * @return The username
     */
    public String getUser() {
        return user;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("user", user);
        return obj;
    }
    
    @Override
    public JSONType toJSONType() {
        return this.toJSONObject();
    }
    
    @Override
    public void deserialize(JSONType json) {
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            this.user = (String) obj.get("user");
        }
    }
}

/**
 * Message for retrieving posts for a user.
 */
class GetMessageRequest extends Message {
    private String user;

    /**
     * Create a new get message request.
     * 
     * @param user The username to get messages for
     */
    public GetMessageRequest(String user) {
        super("GetMessage");
        this.user = user;
    }

    /**
     * Create a message from a JSONObject
     * 
     * @param obj The JSONObject to parse
     */
    public GetMessageRequest(JSONObject obj) {
        super("GetMessage");
        this.user = (String) obj.get("user");
    }

    /**
     * Get the username
     * 
     * @return The username
     */
    public String getUser() {
        return user;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        obj.put("user", user);
        return obj;
    }

    @Override
    public JSONType toJSONType() {
        return this.toJSONObject();
    }
    
    @Override
    public void deserialize(JSONType json) {
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            this.user = (String) obj.get("user");
        }
    }
}

/**
 * Response with multiple posts for a user.
 */
class GetResponseMessage extends Message {
    private Post[] posts;

    /**
     * Create a new get response message.
     * 
     * @param posts Array of posts for the user
     */
    public GetResponseMessage(Post[] posts) {
        super("GetResponseMessage");
        this.posts = posts;
    }

    /**
     * Get the posts
     * 
     * @return Array of posts
     */
    public Post[] getPosts() {
        return posts;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = super.toJSONObject();
        
        // Convert posts to a JSON array
        merrimackutil.json.types.JSONArray postsArray = new merrimackutil.json.types.JSONArray();
        for (Post post : posts) {
            postsArray.add(post.toJSONObject());
        }
        
        obj.put("posts", postsArray);
        return obj;
    }
    
    @Override
    public JSONType toJSONType() {
        return this.toJSONObject();
    }
    
    @Override
    public void deserialize(JSONType json) {
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            merrimackutil.json.types.JSONArray postsArray = (merrimackutil.json.types.JSONArray) obj.get("posts");
            if (postsArray != null) {
                Post[] posts = new Post[postsArray.size()];
                for (int i = 0; i < postsArray.size(); i++) {
                    posts[i] = new Post((JSONObject) postsArray.get(i));
                }
                this.posts = posts;
            }
        }
    }
}
