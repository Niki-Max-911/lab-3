package streems;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import entitys.Donor;
import instagramApi.InstagramWeb;
import instagramApi.Mobile;
import types.Photo;

public class UploadPhotoStream implements Runnable {
	private Mobile mobApi;
	private InstagramWeb web;
	private BlockingQueue<Photo> photoQueue;

	private AtomicInteger countLoaded = new AtomicInteger(0);
	private AtomicBoolean photoUploaded;
	private boolean photoReceivingComplete;
	private List<Photo> list;

	public UploadPhotoStream(Mobile mob, InstagramWeb web, BlockingQueue<Photo> photoQueue) {
		this.mobApi = mob;
		this.web = web;
		this.photoQueue = photoQueue;
		photoUploaded = new AtomicBoolean(false);
		photoReceivingComplete = false;
	}

	@Override
	public void run() {
		while (!photoReceivingComplete) {
			try {
				Thread.sleep(500l);
			} catch (InterruptedException e) {
				photoUploaded.set(true);
				return;
			}
		}

		list = new LinkedList<Photo>(photoQueue);
		Collections.reverse(list);

		photoQueue.clear();
		photoQueue.addAll(list);

		while (!photoQueue.isEmpty()) {
			Photo photo = photoQueue.poll();
			if (photo == null)
				continue;

			// загружає фото в оперативку з сайту
			InputStream photoStream;
			try {
				photoStream = web.loadPhoto(photo.getDisplaySrc());
			} catch (IOException e1) {
				continue;
			}

			// 10 спроб залити фотку
			boolean uploadSuccess = false;
			
			String caption = Donor.donor.isCopyOldDescription() ? photo.getCaption() : "";
			caption = caption + Donor.donor.getAppendDescription();
			String mediaId = mobApi.uploadPhotography(photoStream);
			
			for (int i = 0; !uploadSuccess/** && i < 3**/; i++) {
				System.out.println("Trying: " + i);

				// заливка фото

				uploadSuccess = mobApi.setPhotoConfig(mediaId, caption);

				if (uploadSuccess)
					countLoaded.incrementAndGet();
				try {
					Thread.sleep(uploadSuccess ? 10000l : 30000l);
				} catch (InterruptedException e) {
					photoUploaded.set(true);
					return;
				}
			}
		}

		photoUploaded.set(true);
	}

	public int getCountUploaded() {
		return countLoaded.get();
	}

	public final boolean isPhotoUploaded() {
		return photoUploaded.get();
	}

	public void setReceivingComplete() {
		photoReceivingComplete = true;
	}
}
