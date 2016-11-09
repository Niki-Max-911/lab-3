package api.models;

import org.json.JSONObject;

public class FeedPost {
    public static final String INSTA_URL = "https://www.instagram.com/";
    private JSONObject jsonEntity;

    public FeedPost(JSONObject jsnEntity) {
        this.jsonEntity = jsnEntity;
    }

    public String getId() {
        return jsonEntity.getString("id");
    }

    public String getCaption() {
        return jsonEntity.getJSONObject("caption").getString("text");
    }

    public String getDisplaySrc() {
        return jsonEntity.getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        String id = this.getId();
        if (obj instanceof FeedPost) {
            return ((FeedPost) obj).getId().equals(id);
        }
        return false;
    }

}
