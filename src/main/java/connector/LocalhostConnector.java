package connector;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;

public class LocalhostConnector extends Connector {

	public LocalhostConnector() {
		config = DEF_REQUEST_CONFIG;
		cookieStore = new BasicCookieStore();
		browser = HttpClients.custom().setDefaultRequestConfig(config)
				.setDefaultCookieStore(cookieStore).setUserAgent(USER_AGENT)
				.setRetryHandler(MY_RETRY_HANDLER)
				.setDefaultSocketConfig(SOCKET_CONFIG).build();

		context = HttpClientContext.create();
	}
}
