package entitys;

import java.io.IOException;

import connector.ConnectionFactory;
import connector.Connector;
import exceptions.NoPageException;
import instagramApi.InstagramWeb;

public enum Donor {
	donor;
	private String username;
	private int countPhotoCopy;
	private boolean copyOldDescription;
	private String appendDescription;

	public final String getUsername() {
		return username;
	}

	public final void setLink(String url) throws NoPageException {
		username = url.replaceAll("https?://instagram\\.com/", "").replaceAll("/", "");
		checkUser(username);
	}

	public final int getCountPhotoCopy() {
		return countPhotoCopy;
	}

	public final void setCountPhotoCopy(int countPhotoCopy) {
		this.countPhotoCopy = countPhotoCopy;
	}

	public final boolean isCopyOldDescription() {
		return copyOldDescription;
	}

	public final void setCopyOldDescription(boolean copyOldDescription) {
		this.copyOldDescription = copyOldDescription;
	}

	public final String getAppendDescription() {
		return appendDescription;
	}

	public final void setAppendDescription(String appendDescription) {
		this.appendDescription = appendDescription;
	}

	public boolean checkUser(String donor) throws NoPageException {
		Connector connector = ConnectionFactory.getConnector();
		try {
			InstagramWeb.loadPageByUserName(connector, donor);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
