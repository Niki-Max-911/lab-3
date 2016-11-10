package api.model;

import org.json.JSONObject;

/**
 * Created by Max on 20.02.2016.
 */
public class WebFullUser extends User {

    public WebFullUser(JSONObject jsonEntity) {
        super(jsonEntity);
    }

    @Override
    public long getPk() {
        String idStr = jsonEntity.getString("id");
        long pk = Long.valueOf(idStr);
        return pk;
    }
}
