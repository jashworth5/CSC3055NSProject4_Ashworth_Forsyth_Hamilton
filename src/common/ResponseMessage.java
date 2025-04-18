package common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a server response to a GetMessage
 * contains an array of PostObject messages for the requesting user
 */
public class ResponseMessage implements JSONSerializable {

    private static final String TYPE = "GetResponseMessage";
    private List<PostObject> posts;

    public ResponseMessage(List<PostObject> posts) {
        this.posts = posts;
    }

    public ResponseMessage() {
        this.posts = new ArrayList<>();
    }

    public List<PostObject> getPosts() {
        return posts;
    }

    @Override
    /**
     * converts this response into a json object with type and posts array
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", TYPE);

        JSONArray postArray = new JSONArray();
        for (PostObject post : posts) {
            postArray.add(post.toJSONType());
        }

        obj.put("posts", postArray);
        return obj;
    }

    @Override
    /**
     * populates this response from a json object
     * checks that type is GetResponseMessage and rebuilds the post list
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

        Object rawArray = json.get("posts");
        if (!(rawArray instanceof JSONArray)) {
            throw new InvalidObjectException("expected posts to be a json array");
        }

        JSONArray postArray = (JSONArray) rawArray;
        this.posts = new ArrayList<>();

        for (int i = 0; i < postArray.size(); i++) {
            PostObject post = new PostObject();
            post.deserialize((JSONType) postArray.get(i));
            posts.add(post);
        }
    }
}
