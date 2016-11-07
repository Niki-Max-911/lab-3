package types;

public class InstaObject {
	protected String id;
	protected String userName;

	public InstaObject(String id, String userName) {
		this.id = id;
		this.userName = userName;
	}

	public String getId() {
		return id;
	}

	public String getScrinName() {
		return userName;
	}

	public String getURL() {
		StringBuffer sb = new StringBuffer(Page.INSTA_URL);
		sb.append(userName);
		sb.append('/');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InstaObject) {
			return ((InstaObject) obj).id.equals(this.id);
		}

		if (obj instanceof Page) {
			return ((Page) obj).getId().equals(this.id);
		}

		if (obj instanceof Photo) {
			return ((Photo) obj).getId().equals(this.id);
		}
		return false;
	}
}
