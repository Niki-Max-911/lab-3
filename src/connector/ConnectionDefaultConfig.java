package connector;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.protocol.HttpContext;

public interface ConnectionDefaultConfig {
	final static int COUNT_REPEATS_PROXY_CHECK = 5;
	final static int COUNT_REPEATS_REQUEST = 7;
	final static int REQUEST_TIMEOUT = 5000;
	final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36";
	final static HttpHost TARGET = new HttpHost("instagram.com", 443, "https");

	final static RequestConfig DEF_REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT)
			.setConnectionRequestTimeout(REQUEST_TIMEOUT).setCircularRedirectsAllowed(true)
			.setSocketTimeout(REQUEST_TIMEOUT).setMaxRedirects(20).build();

	final static HttpRequestRetryHandler MY_RETRY_HANDLER = new HttpRequestRetryHandler() {
		public boolean retryRequest(IOException exception, int count, HttpContext context) {

			System.out.println("Repeat : " + count);

			System.out.println(exception.getClass().toGenericString() + " : " + exception.getMessage());
			if (exception instanceof UnknownHostException) {
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
					return false;
				}
				return true;
			} // пропав інтернет

			// java.net.NoRouteToHostException

			// if (exception instanceof SocketException
			// || exception.getMessage().equals("Connection reset")) {
			//
			// }

			exception.printStackTrace();
			Object isChecker = context.getAttribute("Checker");
			if (isChecker != null) {
				boolean isCheckerBool = (Boolean) isChecker;
				if (isCheckerBool)
					return count < COUNT_REPEATS_PROXY_CHECK;
			} // при чеканні проксі контроль іншого значення

			Object isAuth = context.getAttribute("Authentification");
			if (isAuth != null) {
				boolean isAuthBool = (Boolean) isAuth;
				if (isAuthBool)
					return count < 3;
			} // потрібно зупинити повторювання

			Object needStopRetry = context.getAttribute("StopRetrying");
			if (needStopRetry != null) {
				boolean isNeedStop = (Boolean) needStopRetry;
				if (isNeedStop)
					return false;
			} // потрібно зупинити повторювання

			Object isTagResearch = context.getAttribute("TagResearch");
			if (isTagResearch != null) {
				boolean isTagResearchNeed = (Boolean) isTagResearch;
				if (isTagResearchNeed)
					return count < 3;
			} // при пошуку тега повтор лише 3 рази

			// чекаємо за тайм-аутом
			return count < COUNT_REPEATS_REQUEST;
		}
	};

	final static SocketConfig SOCKET_CONFIG = SocketConfig.custom().setSoTimeout(REQUEST_TIMEOUT).build();

	final static HttpUriRequest TEST_REQUEST = RequestBuilder.get().setUri("/about/us/")
			.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			.addHeader("Accept-Encoding", "gzip, deflate, sdch").addHeader("Connection", "keep-alive")
			.addHeader("Host", "instagram.com").build();
}
