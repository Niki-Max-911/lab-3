package connector;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public interface ConnectionDefaultConfig {
	int COUNT_REPEATS_PROXY_CHECK = 5;
	int COUNT_REPEATS_REQUEST = 7;
	int REQUEST_TIMEOUT = 5000;
	String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36";

	RequestConfig DEF_REQUEST_CONFIG = RequestConfig.custom()
			.setConnectTimeout(REQUEST_TIMEOUT)
			.setConnectionRequestTimeout(REQUEST_TIMEOUT)
			.setRedirectsEnabled(false)
			.setRelativeRedirectsAllowed(false)
			.setCircularRedirectsAllowed(false)
			.setSocketTimeout(REQUEST_TIMEOUT)
			.build();

	HttpRequestRetryHandler MY_RETRY_HANDLER = new HttpRequestRetryHandler() {
		@Override
		public boolean retryRequest(IOException exception, int count,
				HttpContext context) {

			System.out.println("Repeat : " + count);

			System.out.println(exception.getClass().toGenericString()+" : "+exception.getMessage());
			if (exception instanceof UnknownHostException) {
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
					return false;
				}
				return true;
			}// пропав інтернет

			exception.printStackTrace();
			Object isChecker = context.getAttribute("Checker");
			if (isChecker != null) {
				boolean isCheckerBool = (boolean) isChecker;
				if (isCheckerBool)
					return count < COUNT_REPEATS_PROXY_CHECK;
			}// при чеканні проксі контроль іншого значення

			Object isAuth = context.getAttribute("Authentification");
			if (isAuth != null) {
				boolean isAuthBool = (boolean) isAuth;
				if (isAuthBool)
					return count < 3;
			}// потрібно зупинити повторювання

			Object needStopRetry = context.getAttribute("StopRetrying");
			if (needStopRetry != null) {
				boolean isNeedStop = (boolean) needStopRetry;
				if (isNeedStop)
					return false;
			}// потрібно зупинити повторювання

			Object isTagResearch = context.getAttribute("TagResearch");
			if (isTagResearch != null) {
				boolean isTagResearchNeed = (boolean) isTagResearch;
				if (isTagResearchNeed)
					return count < 3;
			}// при пошуку тега повтор лише 3 рази

			// чекаємо за тайм-аутом
			return count < COUNT_REPEATS_REQUEST;
		}
	};

	SocketConfig SOCKET_CONFIG = SocketConfig.custom()
			.setSoKeepAlive(true).setSoTimeout(REQUEST_TIMEOUT).build();

}
