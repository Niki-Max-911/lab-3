package connector;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public abstract class Connector implements ConnectionDefaultConfig {
	protected CloseableHttpClient browser;
	protected RequestConfig config;
	protected HttpClientContext context;
	protected BasicCookieStore cookieStore;

	public boolean checkConnection() {
		context.setAttribute("Checker", true);
		CloseableHttpResponse resp;
		try {
			resp = getResp(TEST_REQUEST);
			context.setAttribute("Checker", false);

			int statusCode = resp.getStatusLine().getStatusCode();
			String ans;
			ans = EntityUtils.toString(resp.getEntity());
			if (statusCode == 200
					&& ans.indexOf("About Us &bull; Instagram") > 0)
				return true;

			resp.close();
			HttpClientUtils.closeQuietly(resp);
			return false;

		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
	}

	public void closeBrowser() {
		if (cookieStore != null) {
			cookieStore.clear();
		}

		if (browser != null) {
			HttpClientUtils.closeQuietly(browser);

			try {
				browser.close();
			} catch (IOException e) {
				return;
			}
		}
	}

	protected CloseableHttpResponse executeRequest(HttpHost target,
			HttpRequest request) throws IOException {
		CloseableHttpResponse resp = null;
		resp = browser.execute(target, request, context);
		return resp;
	}

	public <T> T getContextAtribute(String idAtribute, Class<T> type) {
		try {
			return type.cast(context.getAttribute(idAtribute));
		} catch (ClassCastException e) {
			return null;
		}
	}

	public <T> void setContextAtribute(String idAtribute, T value) {
		context.setAttribute(idAtribute, value);
	}

	public abstract CloseableHttpResponse getResp(HttpHost target,
			HttpRequest request) throws IOException;

	public CloseableHttpResponse getResp(HttpRequest request)
			throws IOException {
		return getResp(TARGET, request);
	}

	public BasicCookieStore getCookieStore() {
		return cookieStore;
	}

	protected CloseableHttpResponse getRespThroughMonitor(HttpHost target,
			HttpRequest request) throws IOException {
		return ConnectMonitor.connet.execute(this, target, request);
	}

	protected static void showHeaders(HttpResponse resp) {
		for (Header h : resp.getAllHeaders()) {
			System.out.println(h.getName() + " : " + h.getValue());
		}
		System.out.println(resp.getStatusLine());
		System.out.println("====================");
	}

	protected static void showProxyCountry(String ans) {
		Pattern p = Pattern.compile("\"country_code\":\"[a-zA-Z]*\"");
		Matcher m = p.matcher(ans);
		System.out.println(m.find() ? m.group() : "Fail");
	}

	public void printToken() {
		for (Cookie c : cookieStore.getCookies()) {
			if (c.getName().equals("csrftoken")) {
				System.out.println(c.getValue());
				return;
			}
		}
	}

	public CloseableHttpClient getBrowser() {
		return browser;
	}
}
