import api.Environment;
import connector.ConnectionFactory;
import connector.Connector;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ConnectorTester {

    static HttpHost igTargetHost;
    static Connector connector;

    private HttpResponse resp;

    @BeforeClass
    public static void init() {
        igTargetHost = Environment.TARGET_WEB;
        connector = ConnectionFactory.getConnector();
    }

    @Before
    public void executeRequest() {
        HttpUriRequest indexPageReference = RequestBuilder.get()
                .setUri("/about/us/")
                .addHeader("Accept",
                        "text/html,application/xhtml+xml,application/xmlq=0.9,image/webp,*/*q=0.8")
                .addHeader("Accept-Encoding", "gzip,deflate,sdch")
                .addHeader("Connection", "keep-alive")
                .addHeader("Connection", "close")
                .addHeader("Host", "www.instagram.com")
                .build();
        try {
            resp = connector.getResp(igTargetHost, indexPageReference);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSuccessRequest() {
        int statusCode = resp.getStatusLine().getStatusCode();
        Assert.assertEquals("Answer status code", 200, statusCode);
    }

    @Test
    public void testContent() {
        try {
            String ans = EntityUtils.toString(resp.getEntity());
            Assert.assertTrue("Page content contain brand captions", ans.indexOf("About Us &bull; Instagram") > 0);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCookieStorage() {
        boolean isContainCsrftokenCookie = connector.getCookieStore().getCookies().stream()
                .anyMatch(cookie -> cookie.getName().equals("csrftoken"));
        Assert.assertTrue("Have cookie storage contain \"csrftoken\" pair", isContainCsrftokenCookie);
    }
}
