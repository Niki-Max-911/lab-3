import api.Mobile;
import api.model.FeedPost;
import api.model.ListWithCursor;
import api.model.User;
import entity.Donor;
import org.junit.*;
import streem.DownloadStream;
import streem.UploadPhotoStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


/**
 * Created by niki.max on 10.11.2016.
 */
public class ThreadsMockTest {

    static int countTestPhotos = 3;

    static String userName = "natgeo";
    static Long userId = 123l;

    @BeforeClass
    public static void initStaticData() {
        Donor.donor.setCountPhotoCopy(countTestPhotos);
        Donor.donor.setCopyOldDescription(false);
        Donor.donor.setLink(String.format("https://www.instagram.com/%s/", userName));
    }

    private Mobile mobileMock;
    private BlockingQueue<FeedPost> photoQueue;

    @Before
    public void initData() {
        photoQueue = new LinkedBlockingDeque<>();
    }

    @Before
    public void initMockObjects() {
        mobileMock = mock(Mobile.class);
        doNothing().when(mobileMock).firstRequest();
        when(mobileMock.signIn()).thenReturn(true);
        when(mobileMock.uploadPhotography(any(), anyString())).thenReturn(true);


        User userMock = mock(User.class);
        when(userMock.getPk()).thenReturn(userId);

        ListWithCursor<User> foundedUserList = new ListWithCursor<>();
        foundedUserList.add(userMock);
        when(mobileMock.searchByUserName(eq(userName))).thenReturn(foundedUserList);


        FeedPost photoMock = mock(FeedPost.class);
        when(photoMock.getCaption()).thenReturn("Random caption");
        when(photoMock.getId()).thenReturn("123456798");
        String realPhotoLink = "https://instagram.fjnb2-1.fna.fbcdn.net/t51.2885-19/11906329_960233084022564_1448528159_a.jpg";
        when(photoMock.getDisplaySrc()).thenReturn(realPhotoLink);

        ListWithCursor<FeedPost> feedPosts = new ListWithCursor<>();
        IntStream.range(0, countTestPhotos)
                .forEach(value -> feedPosts.add(photoMock));

        when(mobileMock.getUserFeed(eq(userId.toString()))).thenReturn(feedPosts);
    }

    @Ignore
    @Test
    public void photoCloningThreadsTest() throws InterruptedException {
        Assert.assertEquals("Photo queue should be empty", 0, photoQueue.size());

        UploadPhotoStream uploadPhotoStream = new UploadPhotoStream(mobileMock, photoQueue);
        Assert.assertEquals("Count uploaded photo before starting appropriate thread", 0, uploadPhotoStream.getCountUploaded());
        Assert.assertFalse("Uploading task already ended before starting thread", uploadPhotoStream.isPhotoUploaded());

        DownloadStream downloadPhotoStream = new DownloadStream(mobileMock, uploadPhotoStream, photoQueue);
        Assert.assertEquals("Count downloaded photos before start thread", 0, downloadPhotoStream.getCountDownloaded());

//        start threads
        Stream.of(uploadPhotoStream, downloadPhotoStream)
                .map(Thread::new)
                .peek(Thread::start);


        synchronized (photoQueue) {
            photoQueue.wait();
            Assert.assertFalse("Photo queue isn't empty!", photoQueue.isEmpty());
        }

        Assert.assertEquals("Count downloaded photos", countTestPhotos, downloadPhotoStream.getCountDownloaded());
        Assert.assertEquals("Count photo needs for uploading", countTestPhotos, downloadPhotoStream.getCountNeed());

        //wait for uploading all photos
        TimeUnit.SECONDS.sleep(10);

        Assert.assertEquals("Count photo have already uploaded", countTestPhotos, uploadPhotoStream.getCountUploaded());
        Assert.assertTrue("Is uploading task done", uploadPhotoStream.isPhotoUploaded());
    }

}
