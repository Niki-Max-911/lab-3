package entity;

import api.Mobile;
import api.model.FeedPost;
import connector.ConnectionFactory;
import connector.Connector;
import exception.AuthenticationException;
import lombok.Getter;
import streem.DownloadStream;
import streem.UploadPhotoStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
public class Recipient {

	private String userName;
	private String pass;
	private boolean successAuth = false;

	private UploadPhotoStream uploadStream;
	private DownloadStream downloadStream;

	private Connector connector;
	private Mobile mobile;
	private BlockingQueue<FeedPost> photoQueue;
	{
		connector = ConnectionFactory.getConnector();
		photoQueue = new LinkedBlockingDeque<>();
	}

	public Recipient(String login, String pass) {
		this.userName = login;
		this.pass = pass;
		mobile = new Mobile(userName, pass, connector);
	}

	public void auth() throws AuthenticationException {
		mobile.firstRequest();
		successAuth = mobile.signIn();

		if (!successAuth) {
			throw new AuthenticationException("Bad mobile auth!");
		}

		uploadStream = new UploadPhotoStream(mobile, photoQueue);
		downloadStream = new DownloadStream(mobile, uploadStream, photoQueue);
	}

	public void startUploading() {
		if (!successAuth)
			return;
		Thread t = new Thread(uploadStream);
		t.start();
	}

	public void fillerPhotoQueue() {
		if (!successAuth)
			return;

		Thread t = new Thread(downloadStream);
		t.start();
	}

	public int getCountUpLoadedPhoto() {
		try {
			return uploadStream.getCountUploaded();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public int countInQueue() {
		return photoQueue.size();
	}

	public boolean isUploaded() {
		try {
			return uploadStream.isPhotoUploaded();
		} catch (Exception e) {
			return false;
		}
	}
}
