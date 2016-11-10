package connector;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public abstract class Connector implements ConnectionDefaultConfig {
    protected CloseableHttpClient browser;
    protected RequestConfig config;
    protected HttpClientContext context;
    protected BasicCookieStore cookieStore;


    public HttpResponse getResp(HttpHost target, HttpRequest request) throws IOException {
        HttpResponse resp = browser.execute(target, request, context);
        return resp;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

}
