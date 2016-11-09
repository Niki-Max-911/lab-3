package api.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.NoSuchElementException;

public interface ModelParser {

    static ListWithCursor<User> parseUsers(JSONObject src) {
        ListWithCursor<User> users = new ListWithCursor<>();

        if (src.has("next_max_id")) {
            Long nextId = src.getLong("next_max_id");
            users.setNextCursor(String.valueOf(nextId));
        }

        if (!src.has("users"))
            return users;

        JSONArray jsonUsersArr = src.getJSONArray("users");
        for (int i = 0; i < jsonUsersArr.length(); i++) {
            JSONObject jsonUser = jsonUsersArr.getJSONObject(i);
            User user = new User(jsonUser);
            users.add(user);
        }

        return users;
    }

    static ListWithCursor<FeedPost> parsePosts(JSONObject jsonAns) {
        ListWithCursor<FeedPost> postLst = new ListWithCursor<>();
        JSONArray jsnArr = jsonAns.getJSONArray("items");
        for (int i = 0; i < jsnArr.length(); i++) {
            JSONObject post = jsnArr.getJSONObject(i);
            postLst.add(new FeedPost(post));
        }
        try {
            FeedPost feedPost = postLst.getLast();
            if (feedPost != null)
                postLst.setNextCursor(feedPost.getId());
        } catch (NoSuchElementException e) {
        }
        return postLst;
    }
}
