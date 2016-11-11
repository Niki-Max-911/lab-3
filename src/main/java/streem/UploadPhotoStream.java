package streem;

import api.Mobile;
import api.WebApi;
import api.model.FeedPost;
import entity.Donor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadPhotoStream implements Runnable {
    private Mobile mobile;
    private BlockingQueue<FeedPost> photoQueue;

    private AtomicInteger countLoaded;
    private AtomicBoolean photoUploaded;
    private List<FeedPost> list;

    {
        countLoaded = new AtomicInteger(0);
        photoUploaded = new AtomicBoolean(false);
    }

    public UploadPhotoStream(Mobile mob, BlockingQueue<FeedPost> photoQueue) {
        this.mobile = mob;
        this.photoQueue = photoQueue;
    }

    @Override
    public void run() {
        synchronized (photoQueue) {
            try {
                photoQueue.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        list = new ArrayList<>(photoQueue);
        Collections.reverse(list);

        photoQueue.clear();
        photoQueue.addAll(list);

        while (!photoQueue.isEmpty()) {
            FeedPost photo = photoQueue.poll();
            if (photo == null)
                continue;

            // загружає фото в оперативку з сайту
            InputStream photoStream;
            try {
                photoStream = WebApi.loadPhoto(photo.getDisplaySrc());
            } catch (IOException e1) {
                continue;
            }

            // 3 count trying of uploading photo
            boolean uploadSuccess = false;

            String caption = Donor.donor.isCopyOldDescription() ? photo.getCaption() : "";
            caption = caption + Donor.donor.getAppendDescription();

            for (int i = 0; !uploadSuccess && i < 3; i++) {
                System.out.println("Trying upload photo: " + i);

                // заливка фото
                uploadSuccess = mobile.uploadPhotography(photoStream, caption);
                if (uploadSuccess)
                    countLoaded.incrementAndGet();
                try {
                    TimeUnit.SECONDS.sleep(uploadSuccess ? 1 : 10);
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

}
