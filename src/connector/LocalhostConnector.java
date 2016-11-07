package connector;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
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
		context.setAttribute("Checker", false);
	}

	@Override
	public CloseableHttpResponse getResp(HttpHost target, HttpRequest request) throws IOException {
		return executeRequest(target, request);
	}
}
