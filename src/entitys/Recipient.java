package entitys;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import connector.ConnectionFactory;
import connector.Connector;
import exceptions.AuthenticationException;
import instagramApi.InstagramWeb;
import instagramApi.Mobile;
import instagramApi.Token;
import streems.DownloadStream;
import streems.UploadPhotoStream;
import types.Photo;

public class Recipient {

	private String login;
	private String pass;

	private boolean successAuth;
	private BlockingDeque<Photo> photoQueue = new LinkedBlockingDeque<Photo>();

	private Mobile mobApi;
	private InstagramWeb webApi;
	private UploadPhotoStream uploadStream;
	private DownloadStream downloadStream;

	public Recipient(String login, String pass) {
		this.login = login;
		this.pass = pass;
		successAuth = false;
	}

	public void auth() throws AuthenticationException {
		mobApi = new Mobile(login, pass);
		boolean authSuccess = mobApi.signInMobileApp();

		if (!authSuccess) {
			throw new AuthenticationException("Bad mobile auth!");
		}
		Connector connector = ConnectionFactory.getConnector();
		String s = "410a7a87b1264650a8cfa68d4f5faba1";

		Token token = new Token(login, pass, s, connector);
		try {
			token.authentication();
		} catch (IOException e1) {
			this.successAuth = false;
		}

		webApi = new InstagramWeb(token, connector);

		try {
			webApi.activateApi();
		} catch (Exception e) {
			System.exit(1);
		}
		this.successAuth = true;
		uploadStream = new UploadPhotoStream(mobApi, webApi, photoQueue);
		downloadStream = new DownloadStream(webApi, photoQueue, uploadStream);
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

	public String getUserName() {
		return login;
	}

	public boolean isSuccessAuth() {
		return successAuth;
	}

	public boolean isUploaded() {
		try {
			return uploadStream.isPhotoUploaded();
		} catch (Exception e) {
			return false;
		}
	}
}
