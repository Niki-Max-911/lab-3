package streems;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import entitys.Donor;
import exceptions.AuthenticationException;
import instagramApi.InstagramWeb;
import instagramApi.ListWithCursor;
import types.Photo;

public class DownloadStream implements Runnable {
	private InstagramWeb web;
	private BlockingQueue<Photo> photoQueue;

	private boolean photoDownloaded;

	private int countNeed;
	private int countDownloaded;
	private UploadPhotoStream loadPhotoStream;

	public DownloadStream(InstagramWeb web, BlockingQueue<Photo> photoQueue, UploadPhotoStream loadPhotoStream) {
		this.web = web;
		this.photoQueue = photoQueue;
		this.loadPhotoStream = loadPhotoStream;

		int needCount = Donor.donor.getCountPhotoCopy();
		this.countNeed = needCount == 0 ? Integer.MAX_VALUE : needCount;

		photoDownloaded = false;
		countDownloaded = 0;
	}

	@Override
	public void run() {
		String donorUsename = Donor.donor.getUsername();
		String donorId;
		try {
			donorId = web.loadPageByUserName(donorUsename).getId();
		} catch (Exception e) {
			finish();
			return;
		}

		try {
			ListWithCursor<Photo> photos = web.loadPagePhotos(donorId);
			do {
				for (Iterator<Photo> itr = photos.iterator(); itr.hasNext() && (getCountDownloaded() < countNeed);) {
					photoQueue.add(itr.next());
					countDownloaded++;
				}
				photos = web.loadMorePagePhotos(donorId, photos.getNextCursor());
			} while (!photos.getNextCursor().isEmpty() || !(getCountDownloaded() >= countNeed));

		} catch (AuthenticationException | IOException e) {
			finish();
		}
		finish();
	}

	public boolean isPhotoDownloaded() {
		return photoDownloaded;
	}

	public int getCountDownloaded() {
		return countDownloaded;
	}

	private void finish() {
		photoDownloaded = true;
		loadPhotoStream.setReceivingComplete();
	}
}
