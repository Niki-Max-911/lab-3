package streem;

import api.Mobile;
import api.model.FeedPost;
import api.model.ListWithCursor;
import api.model.User;
import entity.Donor;
import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Getter
public class DownloadStream implements Runnable {
    private Mobile mobile;
    private BlockingQueue<FeedPost> photoQueue;

    private boolean processFinished = false;

    private int countNeed = 1;
    private int countDownloaded = 0;

    {
        int needCount = Donor.donor.getCountPhotoCopy();
        this.countNeed = needCount > 20 || needCount == 0 ? 20 : needCount;
    }

    public DownloadStream(Mobile mobile, UploadPhotoStream loadPhotoStream, BlockingQueue<FeedPost> photoQueue) {
        this.mobile = mobile;
        this.photoQueue = photoQueue;
    }

    @Override
    public void run() {
        String donorId = parseDonorId();

        ListWithCursor<FeedPost> photos = mobile.getUserFeed(donorId).stream()
                .limit(countNeed)
                .collect(Collectors.toCollection(ListWithCursor::new));

        countDownloaded = photos.size();
        photoQueue.addAll(photos);
        finish();
    }

    private String parseDonorId() {
        String donorUsename = Donor.donor.getUsername();
        ListWithCursor<User> foundUsers = mobile.searchByUserName(donorUsename);
        if (foundUsers.isEmpty()) {
            finish();
        }
        Long donorId = foundUsers.get(0).getPk();
        return donorId.toString();
    }

    private void finish() {
        processFinished = true;
        synchronized (photoQueue) {
            photoQueue.notifyAll();
        }
    }
}
