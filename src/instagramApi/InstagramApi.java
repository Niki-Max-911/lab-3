package instagramApi;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import connector.Connector;
import types.InstaObject;
import exceptions.AuthenticationException;
import exceptions.NoPageException;

public class InstagramApi {
	final static HttpHost TARGET_API = new HttpHost("api.instagram.com", 443,
			"https");
	private Connector connector;
	private Token instaToken;
	private String tokenApi;

	public InstagramApi(Token token, Connector connector)
			throws AuthenticationException {
		this.instaToken = token;
		this.connector = connector;
		this.tokenApi = "";
		/* ======АВТОРИЗАЦІЯ ЧЕРЕЗ АПІ======= */
		this.tokenApi = this.instaToken.getAccessApiToken();
	}

	public synchronized String getUserId(String userName) throws IOException,
			NoPageException {
		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/users/search?access_token=" + tokenApi + "&q="
						+ userName + "&count=100")
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);

		if (response.getStatusLine().getStatusCode() != 200)
			throw new NoPageException();

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			ansJson = new JSONObject(ans);
			response.close();
			HttpClientUtils.closeQuietly(response);

			JSONArray dataArray = ansJson.getJSONArray("data");

			for (int i = 0; i < dataArray.length(); i++) {
				String locUserName = dataArray.getJSONObject(i).getString(
						"username");
				if (locUserName.equals(userName))
					return dataArray.getJSONObject(i).getString("id");
			}
		} catch (IOException | JSONException e2) {
			throw new NoPageException();
		}

		throw new NoPageException();
	}

	public void getAllHeaders(HttpResponse response) {
		System.out.println("Code: " + response.getStatusLine().getStatusCode());

		for (Header h : response.getAllHeaders()) {
			System.out.println(h.getName() + " : " + h.getValue());
		}
	}

	private static boolean isAnswerSuccess(JSONObject obj) {
		return "200".equals(obj.getJSONObject("meta").get("code").toString());
	}
}
