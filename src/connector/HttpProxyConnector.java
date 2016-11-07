package connector;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;

class HttpProxyConnector extends Connector {
	public HttpProxyConnector(HttpHost proxy, String userName, String password) {

		if (userName == null)
			userName = "";
		if (password == null)
			password = "";

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY),
				new UsernamePasswordCredentials(userName, password));

		config = RequestConfig.copy(DEF_REQUEST_CONFIG).setProxy(proxy).build();
		cookieStore = new BasicCookieStore();
		browser = HttpClients.custom().setDefaultRequestConfig(config)
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(credsProvider)
				.setUserAgent(USER_AGENT).setDefaultSocketConfig(SOCKET_CONFIG)
				.setRetryHandler(MY_RETRY_HANDLER).build();

		context = HttpClientContext.create();
		context.setAttribute("Checker", false);
	}

	@Override
	public CloseableHttpResponse getResp(HttpHost target, HttpRequest request) throws IOException {
		return executeRequest(target, request);
	}
}
