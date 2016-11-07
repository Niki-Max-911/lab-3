package types;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

public class Page {
	public static String INSTA_URL = "https://instagram.com/";
	private JSONObject jsonUser;

	public Page(JSONObject jsonAns) {
		this.jsonUser = jsonAns;
		removeMeadiaArr();
	}

	public Page(String jsonStrAns) {
		this.jsonUser = new JSONObject(jsonStrAns);
		removeMeadiaArr();
	}

	public String getUserName() {
		return jsonUser.getString("username");
	}

	public String getUserUrl() {
		StringBuffer sb = new StringBuffer(INSTA_URL);
		sb.append(getUserName());
		sb.append('/');

		return sb.toString();
	}

	public String getId() {
		return jsonUser.getString("id");
	}

	public String getBiography() {
		if (jsonUser.isNull("biography") || !jsonUser.has("biography")) {
			return "";
		}
		return jsonUser.getString("biography");
	}

	public String getFullBioName() {
		if (jsonUser.isNull("full_name") || !jsonUser.has("full_name")) {
			return "";
		}
		return jsonUser.getString("full_name");
	}

	public String getProfilePicUrl() {
		return jsonUser.getString("profile_pic_url");
	}

	public boolean isPrivate() {
		return jsonUser.getBoolean("is_private");
	}

	public boolean isFolloewdByMe() {
		return jsonUser.getBoolean("followed_by_viewer");
	}

	public boolean isFollowsMe() {
		return jsonUser.getBoolean("follows_viewer");
	}

	public boolean isRequestedByMe() {
		return jsonUser.getBoolean("requested_by_viewer");

	}

	public boolean isNeedFollowBy() {
		boolean b1 = isFollowsMe();
		boolean b2 = isFolloewdByMe();
		boolean b3 = isRequestedByMe();

		return !(b1 || b2 || b3);
	}

	public boolean isNoMutual() {
		boolean b1 = isFollowsMe();
		boolean b2 = isFolloewdByMe();
		boolean b3 = isRequestedByMe();

		return (!b1) && (b2 || b3);
	}

	public static <T extends LinkedList<Page>> T filterNotFollowedList(
			T pagesList) {
		Iterator<Page> pageItr = pagesList.iterator();

		while (pageItr.hasNext()) {
			Page currentPage = pageItr.next();

			if (currentPage.isNeedFollowBy()) {
				pageItr.remove();
			}
		}
		return pagesList;
	}

	private void removeMeadiaArr() {
		if (jsonUser.has("media")) {
			jsonUser.remove("media");
		}
	}

	@Override
	public boolean equals(Object obj) {

		String id = this.getId();
		if (obj instanceof Page) {
			return ((Page) obj).getId().equals(id);
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
		return new InstaObject(getId(), getUserName());
	}
	
	public static List<InstaObject> getInstaObjectList(List<Page> pages) {
		List<InstaObject> outList = new LinkedList<InstaObject>();

		for (Page page : pages) {
			outList.add(page.getInstaObject());
		}
		return outList;
	}
}