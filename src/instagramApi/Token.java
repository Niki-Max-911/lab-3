package instagramApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import connector.Connector;
import exceptions.AuthenticationException;

/**
 * Для отримання токена при роботі з сервісом instagram.com
 * 
 * @author Maksim
 */
public class Token {
	private String login;
	private String pass;
	private String csrftoken;
	private String clientId;
	private String tokenApi;
	private Connector connector;
	private boolean successAuth;
	private boolean successApiToken;

	public Token(String log, String pas, Connector connector) {
		this.login = log;
		this.pass = pas;
		this.connector = connector;
		this.successAuth = false;
		this.successApiToken = false;
	}

	public Token(String log, String pas, String clntIdApi, Connector connector) {
		this(log, pas, connector);
		this.clientId = clntIdApi;
	}

	/**
	 * авторизація аккаунта в сервісі instagram.com
	 * 
	 * @throws IOException
	 */
	public void authentication() throws AuthenticationException, IOException {
		HttpGet get = new HttpGet("/accounts/login/ajax/");
		get.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		get.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		get.setHeader("Connection", "keep-alive");
		get.setHeader("Referer", "https://instagram.com/accounts/login/ajax/");
		get.setHeader("Host", "instagram.com");

		try {
			connector.getResp(get).close();
		} catch (IOException e1) {
			throw new AuthenticationException("Problems with the Internet!");
		}
		get.abort();

		List<Cookie> lst = connector.getCookieStore().getCookies();
		for (Cookie e : lst) {
			if (e.getName().equals("csrftoken")) {
				this.csrftoken = e.getValue();
			}
		}// шукаємо в куках токен для авторизації

		if (this.csrftoken == null) {
			throw new AuthenticationException(
					"Have problems with authorization.");
		}

		/* запит на авторизацію */
		HttpPost post = new HttpPost("/accounts/login/ajax/");
		post.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		post.setHeader("Accept-Language",
				"uk-UA,uk;q=0.8,ru;q=0.6,en-US;q=0.4,en;q=0.2");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Content-Type",
				"application/x-www-form-urlencoded; charset=UTF-8");
		post.setHeader("Host", "instagram.com");
		post.setHeader("Origin", "https://instagram.com");
		post.setHeader("Referer", "https://instagram.com/accounts/login/ajax/");
		// post.setHeader("X-IG-Connection-Type","WIFI");
		post.setHeader("X-CSRFToken", this.csrftoken);
		// post.setHeader("X-Instagram-AJAX","1");
		post.setHeader("X-Requested-With", "XMLHttpRequest");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", this.login));
		nvps.add(new BasicNameValuePair("password", this.pass));
		nvps.add(new BasicNameValuePair("intent", ""));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e1) {
		}

		CloseableHttpResponse response;
		response = connector.getResp(post);

		// парсимо відповідь
		String ans = "";
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			// =====================================================
			// System.out.println(ans);
		} catch (ParseException | IOException e2) {
		}

		JSONObject obj = new JSONObject(ans);

		// ===========авторизація успішна
		this.successAuth = obj.getBoolean("authenticated");
		if (!this.successAuth) {
			throw new AuthenticationException(
					"Bad data authorization in Instagram!");
		}

		lst = connector.getCookieStore().getCookies();
		for (Cookie e : lst) {
			if (e.getName().equals("csrftoken")) {
				this.csrftoken = e.getValue();
			}
		}// шукаємо в куках токен для авторизації
	}

	public String getActivatedToken() throws AuthenticationException {
		if (!this.successAuth) {
			throw new AuthenticationException(
					"This web is not authorized in Instagram!");
			// повертаю помилку, що не авторизований
		}
		return new String(this.csrftoken);
	}

	public void activateApiToken() throws AuthenticationException, IOException {
		if (this.clientId == null) {
			throw new AuthenticationException("No clientId for Api Instagram.");
		}

		if (!this.successAuth) {
			throw new AuthenticationException(
					"Can't get token Api Instagram. Not authorized.");
		}

		HttpPost post = new HttpPost("/oauth/authorize/?client_id="
				+ this.clientId + "&redirect_uri=http://instagram.com/"
				+ "&response_type=token"
				+ "&scope=relationships+likes+comments");
		post.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		post.setHeader("Accept-Encoding", "gzip,deflate");
		post.setHeader("Cache-Control", "max-age=0");
		post.setHeader("Accept-Language:",
				"uk-UA,uk;q=0.8,ru;q=0.6,en-US;q=0.4,en;q=0.2");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");

		post.setHeader(
				"Referer",
				"https://instagram.com/oauth/authorize/?client_id="
						+ this.clientId
						+ "&redirect_uri=https://instagram.com/&response_type=token&scope=relationships");
		post.setHeader("Host", "instagram.com");
		post.setHeader("Origin", "https://instagram.com");

		StringEntity ent;
		try {
			ent = new StringEntity("csrfmiddlewaretoken=" + this.csrftoken
					+ "&allow=Authorize");
			post.setEntity(ent);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		CloseableHttpResponse response;
		response = connector.getResp(post);

		if (response.getFirstHeader("Location") == null) {
			throw new AuthenticationException(
					"Unexpected server response! Haven't got Api token.");
		}
		String loc = response.getFirstHeader("Location").toString();

		try {
			response.close();
		} catch (IOException e) {
		}

		if (loc.indexOf("#access_token=") == -1) {
			throw new AuthenticationException(
					"Unexpected server response! Haven't got Api token.");
		}
		this.tokenApi = loc.substring(loc.indexOf("#access_token=")
				+ "#access_token=".length());
		this.successApiToken = true;
	}

	public String getAccessApiToken() throws AuthenticationException {
		if (!this.successApiToken) {
			throw new AuthenticationException("Haven't got Api token yet.");
		}
		return this.tokenApi;
	}

	public String getCsrftoken() {
		return csrftoken;
	}

	public void getInfo(HttpResponse response) throws ParseException,
			IOException {
		System.out.println(response.getStatusLine());
		System.out.println("----ЗАГОЛОВКИ ВІДПОВІДІ------------------");

		for (Header h : response.getAllHeaders()) {
			System.out.println(h.getName() + " : " + h.getValue());
		}

		System.out.println("-------------------------КУки: ");
		List<Cookie> lst = connector.getCookieStore().getCookies();
		for (Cookie e : lst) {
			System.out.println(e.getName() + " : " + e.getValue());
		}
		System.out.println("Контент:-------------------");
		System.out.println(EntityUtils.toString(response.getEntity()));
	}
}
