package api.models;

import org.json.JSONObject;

public class User {
	protected JSONObject jsonEntity;

	public User(JSONObject jsonEntity) {
		this.jsonEntity = jsonEntity;
	}

	public long getPk() {
		return jsonEntity.getLong("pk");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		Long id = this.getPk();
		if (obj instanceof User) {
			return ((User) obj).getPk() == id;
		}
		return false;
	}
}
