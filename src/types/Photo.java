package types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class Photo {
	static String INSTA_PHOTO_URL = "https://instagram.com/p/";
	static String INSTA_URL = "https://instagram.com/";
	private JSONObject photoJson;

	public Photo(String strJson) {
		this.photoJson = new JSONObject(strJson);
	}

	public Photo(JSONObject photoJson) {
		this.photoJson = photoJson;
	}

	public String getId() {
		return photoJson.getString("id");
	}

	public String getCaption() {
		if (photoJson.has("caption") && !photoJson.isNull("caption")) {
			return photoJson.getString("caption");
		}
		return "";
	}

	public String getCode() {
		return photoJson.getString("code");
	}

	public String getUrl() {
		StringBuilder sb = new StringBuilder(INSTA_PHOTO_URL);
		sb.append(getCode());
		sb.append('/');

		return sb.toString();
	}
	
	public String getDisplaySrc(){
		return photoJson.getString("display_src");
	}

	public boolean isUserHasLiked() {
		JSONObject jsonLikes = photoJson.getJSONObject("likes");
		return jsonLikes.getBoolean("viewer_has_liked");
	}

	public String getDate() {
		return String.valueOf(photoJson.getLong("date"));
	}

	public String getOwnerId() {
		JSONObject jsonOwner = photoJson.getJSONObject("owner");
		return jsonOwner.getString("id");
	}

	public String getOwnerUrl() {
		JSONObject jsonOwner = photoJson.getJSONObject("owner");
		StringBuilder sb = new StringBuilder(INSTA_URL);
		sb.append(jsonOwner.getString("username"));
		sb.append('/');
		return sb.toString();
	}

	public String getOwnerUserName() {
		JSONObject jsonOwner = photoJson.getJSONObject("owner");
		return jsonOwner.getString("username");
	}

	public String getLocatinId() {
		if (!photoJson.isNull("location") && photoJson.has("location")) {
			JSONObject locationJson = photoJson.getJSONObject("location");
			return String.valueOf(locationJson.getLong("id"));
		}
		return "-1";
	}

	public LinkedList<String> getAllTags() {
		Pattern tagReg = Pattern.compile("#.[^#]*");
		LinkedList<String> tagsList = new LinkedList<String>();

		Matcher m = tagReg.matcher(getCaption());
		while (m.find()) {
			tagsList.add(m.group());
		}

		return tagsList;
	}

	/**
	 * Залишає лише не лайкнувші фото поточним юзером
	 */
	public static <T extends LinkedList<Photo>> T filterUnlikedList(T phtoList) {
		Iterator<Photo> phtItr = phtoList.iterator();

		while (phtItr.hasNext()) {
			Photo currentPh = phtItr.next();

			if (currentPh.isUserHasLiked()) {
				phtItr.remove();
			}
		}
		return phtoList;
	}

	@Override
	public boolean equals(Object obj) {

		String id = this.getId();
		if (obj instanceof Photo) {
			return ((Photo) obj).getId().equals(id);
		}

		if (obj instanceof InstaObject) {
			return ((InstaObject) obj).getId().equals(id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	public InstaObject getInstaObject() {
		StringBuffer sb = new StringBuffer("p/");
		sb.append(getCode());
		return new InstaObject(getId(), sb.toString());
	}
	
	public static List<InstaObject> getInstaObjectList(List<Photo> photos) {
		List<InstaObject> outList = new LinkedList<InstaObject>();

		for (Photo photo : photos) {
			outList.add(photo.getInstaObject());
		}
		return outList;
	}
}
