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

	/**
	 * Get Followers of "userId" person
	 * 
	 * @param userId
	 * @param cursor
	 *            - link of next part
	 * @throws IOException
	 */
	public synchronized ListWithCursor<InstaObject> getFollowers(String userId,
			String cursor) throws IOException {
		ListWithCursor<InstaObject> followers = new ListWithCursor<InstaObject>();
		String tail = !cursor.equals("") ? "&cursor=" + cursor : "";

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/users/" + userId + "/followed-by?access_token="
						+ tokenApi + tail)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);
		if (response == null)
			return followers;

		// getAllHeaders(response);

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			ansJson = new JSONObject(ans);
			response.close();
			HttpClientUtils.closeQuietly(response);
		} catch (IOException | JSONException e2) {
			return followers;
		}

		String username;
		String id;

		if (!isAnswerSuccess(ansJson)) {
			return followers;
		}// чи не відмовлено в доступі

		try {
			String nextCursor = ansJson.getJSONObject("pagination").getString(
					"next_cursor");
			followers.setNextCursor(nextCursor);
		} catch (JSONException e) {
		}// чи є курсор на наступний кусок фоловерів

		JSONArray followersArr = ansJson.getJSONArray("data");
		int size = followersArr.length();

		for (int i = 0; i < size; i++) {
			id = followersArr.getJSONObject(i).getString("id");
			username = followersArr.getJSONObject(i).getString("username");
			followers.add(new InstaObject(id, username));
		}
		return followers;
	}

	/**
	 * метод апі, повертає ссилки на кого підписана дана особа
	 * 
	 * @param userId
	 *            - особа
	 * @param cursor
	 * @throws IOException
	 */
	public synchronized ListWithCursor<InstaObject> getFollowBy(String userId,
			String cursor) throws IOException {
		ListWithCursor<InstaObject> follows = new ListWithCursor<InstaObject>();

		String tail = !cursor.equals("") ? "&cursor=" + cursor : "";

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/users/" + userId + "/follows?access_token="
						+ this.tokenApi + tail)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);
		if (response == null)
			return follows;

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			HttpClientUtils.closeQuietly(response);
			ansJson = new JSONObject(ans);
		} catch (IOException | JSONException e1) {
			return follows;
		}

		String username;
		String id;

		if (!isAnswerSuccess(ansJson)) {
			return follows;
		}// чи не відмовлено в доступі

		try {
			String nextCursor = ansJson.getJSONObject("pagination").getString(
					"next_cursor");
			follows.setNextCursor(nextCursor);
		} catch (JSONException e) {
		}// чи є курсор на наступний кусок фоловерів

		JSONArray followersArr = ansJson.getJSONArray("data");
		int size = followersArr.length();

		for (int i = 0; i < size; i++) {
			id = followersArr.getJSONObject(i).getString("id");
			username = followersArr.getJSONObject(i).getString("username");
			follows.add(new InstaObject(id, username));
		}
		return follows;
	}

	public synchronized int tagSearch(String strTag) throws IOException {
		strTag = strTag.replaceAll("#", "").trim();

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/tags/" + strTag + "?access_token=" + tokenApi)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		connector.setContextAtribute("TagResearch", true);
		CloseableHttpResponse response = connector.getResp(TARGET_API, get);
		connector.setContextAtribute("TagResearch", false);

		if (response == null)
			return 0;

		String ans = "";
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			HttpClientUtils.closeQuietly(response);
		} catch (IOException e2) {
			return 0;
		}

		int count = 0;
		if (ans.contains("\"code\":200")) {
			try {
				JSONObject obj = new JSONObject(ans);
				count = obj.getJSONObject("data").getInt("media_count");
			} catch (JSONException e) {
				return 0;
			}
		}
		return count;
	}

	public synchronized ListWithCursor<InstaObject> tagRecent(String strTag,
			String cursor) throws IOException {
		strTag = strTag.replaceAll("#", "").trim();
		ListWithCursor<InstaObject> tagUsers = new ListWithCursor<InstaObject>();

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/tags/" + strTag + "/media/recent?access_token="
						+ this.tokenApi + "&max_tag_id=" + cursor)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);

		if (response == null)
			return tagUsers;

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			HttpClientUtils.closeQuietly(response);
			ansJson = new JSONObject(ans);
		} catch (IOException | JSONException e1) {
			return tagUsers;
		}

		if (!isAnswerSuccess(ansJson))
			return tagUsers;

		try {
			JSONObject paginationJsn = ansJson.getJSONObject("pagination");
			String nextCursor = paginationJsn.getString("next_max_tag_id");
			tagUsers.setNextCursor(nextCursor);
		} catch (JSONException e) {
		}

		JSONArray dataArr = ansJson.getJSONArray("data");
		int size = dataArr.length();

		for (int i = 0; i < size; i++) {
			JSONObject userJsn = dataArr.getJSONObject(i).getJSONObject("user");
			String userName = userJsn.get("username").toString();
			String id = userJsn.get("id").toString();
			tagUsers.add(new InstaObject(id, userName));
		}
		return tagUsers;
	}

	/**
	 * Повртає список людей, хто лайкнув фотку
	 * 
	 * @throws IOException
	 */
	public synchronized LinkedList<InstaObject> getOwnersOfLikes(String mediaId)
			throws IOException {
		LinkedList<InstaObject> userList = new LinkedList<InstaObject>();

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/media/" + mediaId + "/likes?access_token="
						+ tokenApi)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);
		if (response == null)
			return userList;

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			HttpClientUtils.closeQuietly(response);
			ansJson = new JSONObject(ans);
		} catch (ParseException | IOException | JSONException e1) {
			return userList;
		}

		String username;
		String id;

		if (!isAnswerSuccess(ansJson)) {
			return userList;
		}// чи не відмовлено в доступі

		JSONArray followersArr = ansJson.getJSONArray("data");
		int size = followersArr.length();

		for (int i = 0; i < size; i++) {
			id = followersArr.getJSONObject(i).getString("id");
			username = followersArr.getJSONObject(i).getString("username");
			userList.add(new InstaObject(id, username));
		}
		return userList;
	}

	public synchronized HashSet<InstaObject> getOwnersOfComments(String mediaId)
			throws IOException {
		HashSet<InstaObject> userList = new HashSet<InstaObject>();

		HttpUriRequest get = RequestBuilder
				.get()
				.setUri("/v1/media/" + mediaId + "/comments?access_token="
						+ tokenApi)
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Host", "api.instagram.com")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(TARGET_API, get);
		if (response == null)
			return userList;

		String ans = "";
		JSONObject ansJson;
		try {
			ans = EntityUtils.toString(response.getEntity());
			response.close();
			HttpClientUtils.closeQuietly(response);
			ansJson = new JSONObject(ans);
		} catch (ParseException | IOException | JSONException e1) {
			return userList;
		}

		String userName;
		String id;

		if (!isAnswerSuccess(ansJson)) {
			return userList;
		}// чи не відмовлено в доступі

		JSONArray dataArr = ansJson.getJSONArray("data");
		int size = dataArr.length();

		for (int i = 0; i < size; i++) {
			JSONObject fromJson = dataArr.getJSONObject(i)
					.getJSONObject("from");
			id = fromJson.get("id").toString();
			userName = fromJson.get("username").toString();
			userList.add(new InstaObject(id, userName));
		}
		return userList;
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
