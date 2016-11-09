package api;

import api.models.FeedPost;
import api.models.ListWithCursor;
import api.models.ModelParser;
import api.models.User;
import connector.Connector;
import lombok.extern.java.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Log
public class Mobile {
    private Connector connector;

    private List<Header> getHeaders = new ArrayList<Header>();
    private List<Header> postHeaders = new ArrayList<Header>();
    private String login;
    private String password;
    private InstagramAgent instagramAgent;
    private String csrftoken;
    private String uuid;

    {
        uuid = MobileUtils.getGuid();
        instagramAgent = MobileUtils.randUserAgent();
        getHeaders.addAll(MobileUtils.getHeaders(instagramAgent.toString()));
        postHeaders.addAll(MobileUtils.postHeaders(instagramAgent.toString()));
    }

    public Mobile(String login, String pass, Connector connector) {
        this.login = login;
        this.password = pass;
        this.connector = connector;
    }

    public void firstRequest() {
        HttpUriRequest request = RequestBuilder.get().setUri("/api/v1/si/fetch_headers/")
                .addParameter("guid", uuid.replaceAll("-", "")).addParameter("challenge_type", "signup").build();
        request.setHeaders(getHeaders.toArray(new Header[6]));

        try {
            getResp(request);
            HttpResponse resp = getResp(request);
            String ans = EntityUtils.toString(resp.getEntity());
            System.out.println(ans);
        } catch (IOException e) {
        }
    }

    public boolean signIn() {
        Map<String, String> params = new HashMap<>();
        params.put("_csrftoken", parseCsrftoken());
        params.put("username", login);
        params.put("password", password);
        params.put("device_id", "android-" + uuid);
        params.put("guid", uuid);
        String entityStr = MobileUtils.signEnity(params);
        StringEntity entity = new StringEntity(entityStr, Charset.forName("UTF-8"));

        HttpUriRequest request = RequestBuilder.post().setUri("/api/v1/accounts/login/").setEntity(entity).build();
        request.setHeaders(postHeaders.toArray(new Header[7]));

        try {
            HttpResponse resp = getResp(request);
            String ans = EntityUtils.toString(resp.getEntity());
            JSONObject ansJson = new JSONObject(ans);
            return ansJson.getString("status").equals("ok");
        } catch (IOException | JSONException e) {
            return false;
        } finally {
            parseCsrftoken();
        }
    }

    public ListWithCursor<User> searchByUserName(String userName) {
        log.info("start search BY USER-NAME;");
        HttpUriRequest request = RequestBuilder.get().setUri("/api/v1/users/search/").addParameter("q", userName)
                .build();
        request.setHeaders(getHeaders.toArray(new Header[6]));

        String ans = "";
        try {
            HttpResponse resp = getResp(request);
            ans = EntityUtils.toString(resp.getEntity());
            if (!isOk(resp, ans))
                return new ListWithCursor<User>();

            JSONObject jsonAns = new JSONObject(ans);
            return ModelParser.parseUsers(jsonAns);
        } catch (IOException e) {
            log.info("IOException " + e.getMessage());
            return new ListWithCursor<User>();
        } catch (JSONException e) {
            log.info("JSONException " + e.getMessage() + "; ans: " + ans);
            return new ListWithCursor<User>();
        }
    }

    public ListWithCursor<FeedPost> getUserFeed(String userId) {
        return getUserFeed(userId, null);
    }

    public ListWithCursor<FeedPost> getUserFeed(String userId, String maxId) {
        log.info("get USER FEED;");
        RequestBuilder builder = RequestBuilder.get().setUri("/api/v1/feed/user/" + userId + "/");
        if (maxId != null && !maxId.isEmpty())
            builder.addParameter("max_id", maxId);
        HttpUriRequest request = builder.build();

        request.setHeaders(getHeaders.toArray(new Header[6]));

        String ans = "";
        try {
            HttpResponse resp = getResp(request);
            ans = EntityUtils.toString(resp.getEntity());
            if (!isOk(resp, ans))
                return new ListWithCursor<FeedPost>();

            JSONObject jsonAns = new JSONObject(ans);
            return ModelParser.parsePosts(jsonAns);
        } catch (IOException e) {
            log.info("IOException " + e.getMessage());
            return new ListWithCursor<FeedPost>();
        } catch (JSONException e) {
            log.info("JSONException " + e.getMessage() + "; ans: " + ans);
            return new ListWithCursor<FeedPost>();
        }
    }

    public boolean uploadPhotography(InputStream photoStream, String caption) {
        File tempFile = null;
        try {
            log.info("convert photo;");
            tempFile = File.createTempFile("toload", ".jpg");
            tempFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(photoStream, out);
        } catch (IOException e) {
            log.info("IOException " + e.getMessage());
            return false;
        }
        String mediaId = uploadFile(tempFile);
        return setPhotoConfig(mediaId, caption);
    }

    private String uploadFile(File photo) {
        log.info("start UPLOADING PHOTO;");
        Map<String, String> params = new HashMap<>();
        params.put("lib_name", "jt");
        params.put("lib_version", "1.3.0");
        params.put("quality", "83");
        JSONObject jsnCompresion = new JSONObject(params);

        FileBody fileBodyToUpLoad = new FileBody(photo, ContentType.APPLICATION_OCTET_STREAM, photo.getName());

        HttpEntity entity = MultipartEntityBuilder.create().addTextBody("_csrftoken", parseCsrftoken())
                .addTextBody("_uuid", uuid).addTextBody("image_compression", jsnCompresion.toString())
                .addTextBody("upload_id", MobileUtils.getTimesmap()).addPart("photo", fileBodyToUpLoad).build();

        HttpUriRequest request = RequestBuilder.post().setUri("/api/v1/upload/photo/").setEntity(entity).build();
        request.setHeaders(getHeaders.toArray(new Header[6]));
        request.addHeader(entity.getContentType());
        try {
            HttpResponse resp = getResp(request);
            String ans = EntityUtils.toString(resp.getEntity());
            if (!isOk(resp, ans))
                return "";
            return new JSONObject(ans).getString("upload_id");
        } catch (IOException e) {
            log.info("IOException " + e.getMessage());
            return "";
        }
    }

    private boolean setPhotoConfig(String mediaId, String caption) {
        log.info("start set PHOTO CONFIG;");
        JSONObject params = new JSONObject();
        params.put("_csrftoken", parseCsrftoken());
        params.put("_uuid", uuid);
        String ds_user_id = getCookie("ds_user_id");
        params.put("_uid", ds_user_id);
        params.put("upload_id", mediaId);
        params.put("source_type", "4");
        params.put("usertags", "{}");
        params.put("crop_original_size", "{}");

        JSONObject extra = new JSONObject();
        extra.put("source_width", "640");
        extra.put("source_height", "640");
        params.put("extra", extra);

        JSONObject device = new JSONObject();
        device.put("android_release", instagramAgent.getAndroidRelease());
        device.put("model", instagramAgent.getModel());
        device.put("android_version", instagramAgent.getAndroidVersion());
        device.put("manufacturer", instagramAgent.getManufacturer());
        params.put("device", device);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String date = formater.format(calendar.getTime());
        params.put("date_time_digitalized", date);
        params.put("date_time_original", date);
        params.put("caption", caption);

        String entityStr = MobileUtils.signEnity(params.toString());
        StringEntity entity = new StringEntity(entityStr, Charset.forName("UTF-8"));

        HttpUriRequest request = RequestBuilder.post().setUri("/api/v1/media/configure/").setEntity(entity).build();
        request.setHeaders(postHeaders.toArray(new Header[7]));

        try {
            HttpResponse resp = getResp(request);
            String ans = EntityUtils.toString(resp.getEntity());
            return isOk(resp, ans);
        } catch (IOException e) {
            log.info("IOException " + e.getMessage());
            return false;
        }
    }

    private HttpResponse getResp(HttpRequest request) throws IOException {
        return connector.getResp(Environment.TARGET_API, request);
    }

    private boolean isOk(HttpResponse resp, String ansStr) {
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.info("status code not 200; status: " + resp.getStatusLine().getStatusCode() + "; ans: " + ansStr);
            return false;
        }

        if (!ansStr.contains("\"status\": \"ok\"") && !ansStr.contains("\"status\":\"ok\"") && !ansStr.contains("\"status\" :\"ok\"")) {
            log.info("entity status NOT OK; entity: " + ansStr);
            return false;
        } else {
            log.info("entity: " + ansStr);
        }
        return true;
    }

    private String parseCsrftoken() {
        if (csrftoken == null || csrftoken.isEmpty()) {
            List<Cookie> lst = connector.getCookieStore().getCookies();
            for (Cookie e : lst) {
                if (e.getName().equals("csrftoken")) {
                    csrftoken = e.getValue();
                    return csrftoken;
                }
            }
        }
        return csrftoken;
    }

    private String getCookie(String cookie) {
        List<Cookie> lst = connector.getCookieStore().getCookies();
        for (Cookie e : lst) {
            if (e.getName().equals(cookie))
                return e.getValue();
        }
        return "";
    }
}
