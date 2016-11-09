package api;

import api.models.ListWithCursor;
import api.models.WebFullUser;
import connector.Connector;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Max on 20.02.2016.
 */
public interface WebApi {

    static ListWithCursor<WebFullUser> getWebFullUser(Connector connector, String userName) {

        HttpUriRequest get = RequestBuilder
                .get()
                .setUri("/" + userName + "/?__a=1")
                .addHeader("Accept",
                        "text/html,application/xhtml+xml,application/xmlq=0.9,image/webp,*/*q=0.8")
                .addHeader("Accept-Encoding", "gzip,deflate,sdch")
                .addHeader("Connection", "keep-alive")
                .addHeader("Connection", "close")
                .addHeader("Referer", "https://www.instagram.com/" + userName + "/")
                .addHeader("Host", "www.instagram.com").build();

        try {
            HttpResponse response = connector.getResp(Environment.TARGET_WEB, get);

            HttpEntity entity = response.getEntity();
            String entStr = EntityUtils.toString(entity);

            JSONObject jsonObject = new JSONObject(entStr);
            JSONObject user = jsonObject.getJSONObject("user");

            ListWithCursor<WebFullUser> userLst = new ListWithCursor<>();
            if (user != null)
                userLst.add(new WebFullUser(user));

            return userLst;
        } catch (IOException e) {
            return new ListWithCursor<WebFullUser>();
        }
    }

    static InputStream loadPhoto(String way) throws IOException {
        URL connection = new URL(way);
        HttpURLConnection urlconn;
        urlconn = (HttpURLConnection) connection.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.connect();
        InputStream in = urlconn.getInputStream();
        return in;
    }
}
