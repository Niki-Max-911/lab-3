package connector;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.client.cache.ExponentialBackOffSchedulingStrategy;

public class LocalhostConnector extends Connector {

    public LocalhostConnector() {
        CacheConfig cc = CacheConfig.DEFAULT;
        ExponentialBackOffSchedulingStrategy expopencialBackoffStartegy = new ExponentialBackOffSchedulingStrategy(cc);

        config = DEF_REQUEST_CONFIG;
        cookieStore = new BasicCookieStore();
//		browser = HttpClients.custom().setDefaultRequestConfig(config)
//				.setDefaultCookieStore(cookieStore).setUserAgent(USER_AGENT)
//				.setRetryHandler(MY_RETRY_HANDLER)
//				.setDefaultSocketConfig(SOCKET_CONFIG).build();

        browser = CachingHttpClients.custom()
                .setSchedulingStrategy(expopencialBackoffStartegy)
                .setDefaultRequestConfig(config)
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(USER_AGENT)
//				.setRetryHandler(MY_RETRY_HANDLER)
                .setDefaultSocketConfig(SOCKET_CONFIG).build();
        context = HttpClientContext.create();
    }
}
