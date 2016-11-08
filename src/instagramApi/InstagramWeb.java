package instagramApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import types.Page;
import types.PageInfo;
import types.Photo;
//import accountManager.PageInfo;
import connector.Connector;
import exceptions.AuthenticationException;
import exceptions.ExceededLimitsApi;
import exceptions.NoPageException;

public class InstagramWeb {
	private Token instaToken;
	private Connector connector;
	private InstagramApi api;

	public InstagramWeb(Token token, Connector connector) {
		this.instaToken = token;
		this.connector = connector;
	}

	public void activateApi() throws AuthenticationException, IOException {
		instaToken.activateApiToken();
		this.api = new InstagramApi(instaToken, connector);
	}


	public synchronized boolean setComentar(String photoId, String comment_text)
			throws AuthenticationException, ExceededLimitsApi, IOException {
		StringEntity strEntity = new StringEntity(new String("comment_text=" + comment_text), "UTF-8");

		HttpUriRequest post = RequestBuilder.post().setUri("/web/comments/" + photoId + "/add/")
				.addHeader("Accept", "*/*").addHeader("Accept-Encoding", "gzip,deflate")
				.addHeader("Content-Type", "application/x-www-form-urlencoded charset=UTF-8")
				.addHeader("Accept-Language", "uk-UA,ukq=0.8,ruq=0.6,en-USq=0.4,enq=0.2")
				.addHeader("Connection", "keep-alive").addHeader("Host", "instagram.com")
				.addHeader("Referer", "https://instagram.com/").addHeader("Origin", "https://instagram.com")
				.addHeader("X-CSRFToken", this.instaToken.getActivatedToken()).addHeader("X-Instagram-AJAX", "1")
				.addHeader("X-Requested-With", "XMLHttpRequest").setEntity(strEntity).build();

		CloseableHttpResponse response = connector.getResp(post);
		if (response == null)
			return false;
		HttpEntity entity = response.getEntity();

		try {
			String ans = EntityUtils.toString(entity);
			checkExceededLimits(response, ans);
			response.close();
			return isAnswerOk(ans);
		} catch (IOException e) {
			return false;
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	public static InputStream loadPhoto(String way) throws IOException {
		URL connection = new URL(way);
		HttpURLConnection urlconn;
		urlconn = (HttpURLConnection) connection.openConnection();
		urlconn.setRequestMethod("GET");
		urlconn.connect();
		InputStream in = urlconn.getInputStream();
		return in;
	}

	public String loadPageEdit(String myUserName) throws IOException {
		HttpUriRequest get = RequestBuilder.get().setUri("/accounts/edit/")
				.addHeader("Accept", "text/html, application/xhtml+xml,application/xmlq=0.9,image/webp,*/*q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch").addHeader("Connection", "keep-alive")
				// .addHeader("Accept-Charset", "utf-8")
				.addHeader("Referer", "https://instagram.com/" + myUserName + "/").addHeader("Host", "instagram.com")
				.build();

		connector.setContextAtribute("TagResearch", true);
		CloseableHttpResponse response = connector.getResp(get);
		connector.setContextAtribute("TagResearch", false);
		HttpEntity entity = response.getEntity();
		if (response.getStatusLine().getStatusCode() == 200) {
			String strAns = EntityUtils.toString(entity);
			response.close();
			HttpClientUtils.closeQuietly(response);
			return strAns;
		}
		return "";
	}

	public static Page loadPageByUserName(Connector connector, String userName) throws NoPageException, IOException {
		HttpUriRequest get = RequestBuilder.get().setUri("/" + userName + "/?__a=1")
				.addHeader("Accept", "text/html,application/xhtml+xml,application/xmlq=0.9,image/webp,*/*q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch")
				// .addHeader("Connection", "keep-alive")
				.addHeader("Connection", "close").addHeader("Referer", "https://instagram.com/" + userName + "/")
				.addHeader("Host", "instagram.com").build();

		CloseableHttpResponse response = connector.getResp(get);
		if (response == null)
			throw new NoPageException();
		HttpEntity entity = response.getEntity();

		if (response.getStatusLine().getStatusCode() == 200) {
			try {
				String jsonAns = EntityUtils.toString(entity);
				response.close();
				HttpClientUtils.closeQuietly(response);

				JSONObject ansObjJson = new JSONObject(jsonAns);
				JSONObject userJson = ansObjJson.getJSONObject("user");

				return new Page(userJson);
			} catch (IOException | JSONException e) {
				throw new NoPageException();
			}
		} else {
			throw new NoPageException();
		}
	}

	public Page loadPageByUserName(String userName) throws NoPageException, IOException {
		return loadPageByUserName(connector, userName);
	}

	public ListWithCursor<Photo> loadPagePhotos(String userId) throws AuthenticationException, IOException {
		String methodEntity = "ig_user(" + userId + ") { media.first(35) {" + "	   nodes {" + "	     code,display_src,"
				+ "	     caption,date," + "	     id," + "	     location {id}," + "	     likes {"
				+ "	 	  viewer_has_liked" + "	     }," + "	     owner {" + "	       id," + "	       username"
				+ "	     }" + "	   }," + "	   page_info" + "	 }" + " }";

		String strAns = executeInternalQuery(methodEntity, "users::show");

		ListWithCursor<Photo> photoList = new ListWithCursor<Photo>();
		JSONObject jsonOnj;
		try {
			jsonOnj = new JSONObject(strAns);
			JSONObject jsonMedia = jsonOnj.getJSONObject("media");

			boolean hasNext = jsonMedia.getJSONObject("page_info").getBoolean("has_next_page");
			if (hasNext) {
				photoList.setNextCursor(jsonMedia.getJSONObject("page_info").getString("end_cursor"));
			}

			JSONArray mediaArrJson = jsonMedia.getJSONArray("nodes");
			for (int i = 0; i < mediaArrJson.length(); i++) {
				JSONObject photoJson = mediaArrJson.getJSONObject(i);
				photoList.add(new Photo(photoJson));
			} // розпарсити масив медіа
		} catch (JSONException e) {
			return photoList;
		}

		return photoList;
	}

	public ListWithCursor<Photo> loadMorePagePhotos(String userId, String lastPhotoId)
			throws AuthenticationException, IOException {

		String methodEntity = "ig_user(" + userId + ") { media.after(" + lastPhotoId + ", 35) {" + "	  nodes {"
				+ "	    code, display_src," + "	    caption,date," + "	    id," + "	    location {id}," + "	    likes {"
				+ "		  viewer_has_liked" + "	    }," + "	    owner {" + "	      id," + "	      username"
				+ "	    }" + "	  }," + "	  page_info" + "	}" + " }";
		String strAns = executeInternalQuery(methodEntity, "users::show");
		ListWithCursor<Photo> photoList = new ListWithCursor<Photo>();

		JSONObject jsonOnj;
		try {
			jsonOnj = new JSONObject(strAns);
			JSONObject jsonMedia = jsonOnj.getJSONObject("media");

			boolean hasNext = jsonMedia.getJSONObject("page_info").getBoolean("has_next_page");
			if (hasNext) {
				photoList.setNextCursor(jsonMedia.getJSONObject("page_info").getString("end_cursor"));
			}

			JSONArray mediaArrJson = jsonMedia.getJSONArray("nodes");
			for (int i = 0; i < mediaArrJson.length(); i++) {
				JSONObject photoJson = mediaArrJson.getJSONObject(i);
				photoList.add(new Photo(photoJson));
			} // розпарсити масив медіа
		} catch (JSONException e) {
			return photoList;
		}

		return photoList;
	}

	public ListWithCursor<Photo> loadFeedLine() throws AuthenticationException, IOException {

		String methodEntity = "ig_me() {" + "  feed {" + "    media.first(100){" + "      nodes {" + "        id,"
				+ "        caption," + "        code,display_src," + "        date," + "        likes {" + "          count,"
				+ "          viewer_has_liked" + "        }," + "        location {" + "          id" + "        },"
				+ "        owner {" + "          id," + "          username" + "        }" + "      },"
				+ "      page_info" + "    }" + "  }" + "}";

		String strAns = executeInternalQuery(methodEntity, "feed::show");
		ListWithCursor<Photo> photoList = new ListWithCursor<Photo>();

		JSONObject jsonOnj;
		try {
			jsonOnj = new JSONObject(strAns);

			JSONObject jsonMedia = jsonOnj.getJSONObject("feed").getJSONObject("media");

			boolean hasNext = jsonMedia.getJSONObject("page_info").getBoolean("has_next_page");
			if (hasNext) {
				photoList.setNextCursor(jsonMedia.getJSONObject("page_info").getString("end_cursor"));
			}

			JSONArray mediaArrJson = jsonMedia.getJSONArray("nodes");
			for (int i = 0; i < mediaArrJson.length(); i++) {
				Photo currentPhoto = new Photo(mediaArrJson.getJSONObject(i));
				photoList.add(currentPhoto);
			} // розпарсити масив медіа
		} catch (JSONException e) {
			return photoList;
		}

		return photoList;
	}

	public ListWithCursor<Photo> loadFeedLineMore(String afterPhotoId) throws AuthenticationException, IOException {

		String methodEntity = "ig_me() {" + "  feed {" + "    media.after(" + afterPhotoId + ", 100){" + "      nodes {"
				+ "        id," + "        caption," + "        code, display_src," + "        date," + "        likes {"
				+ "          count," + "          viewer_has_liked" + "        }," + "        location {"
				+ "          id" + "        }," + "        owner {" + "          id," + "          username"
				+ "        }" + "      }," + "      page_info" + "    }" + "  }" + "}";

		String strAns = executeInternalQuery(methodEntity, "feed::show");
		ListWithCursor<Photo> photoList = new ListWithCursor<Photo>();

		JSONObject jsonOnj;

		try {
			jsonOnj = new JSONObject(strAns);

			JSONObject jsonMedia = jsonOnj.getJSONObject("feed").getJSONObject("media");

			boolean hasNext = jsonMedia.getJSONObject("page_info").getBoolean("has_next_page");
			if (hasNext) {
				photoList.setNextCursor(jsonMedia.getJSONObject("page_info").getString("end_cursor"));
			}

			JSONArray mediaArrJson = jsonMedia.getJSONArray("nodes");
			for (int i = 0; i < mediaArrJson.length(); i++) {
				Photo currentPhoto = new Photo(mediaArrJson.getJSONObject(i));
				photoList.add(currentPhoto);
			} // розпарсити масив медіа
		} catch (JSONException e) {
			return photoList;
		}
		return photoList;
	}

	private String executeInternalQuery(String methodQuery, String ref) throws AuthenticationException, IOException {
		LinkedList<NameValuePair> entLst = new LinkedList<NameValuePair>();
		entLst.add(new BasicNameValuePair("q", methodQuery));
		entLst.add(new BasicNameValuePair("ref", ref));
		HttpEntity requstEntity = EntityBuilder.create().setParameters(entLst).build();

		HttpUriRequest post = RequestBuilder.post().setUri("/query/")
				.addHeader("Accept", "text/html,application/xhtml+xml,application/xmlq=0.9,image/webp,*/*q=0.8")
				.addHeader("Accept-Encoding", "gzip,deflate,sdch").addHeader("Connection", "keep-alive")
				.addHeader("Host", "instagram.com").addHeader("Referer", "https://instagram.com/")
				.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").setEntity(requstEntity)
				.addHeader("X-CSRFToken", this.instaToken.getActivatedToken()).addHeader("X-Instagram-AJAX", "1")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();

		CloseableHttpResponse response = connector.getResp(post);
		if (response == null) {
			return "";
		}

		HttpEntity entity = response.getEntity();
		try {
			String strAns = EntityUtils.toString(entity);
			// System.out.println(strAns);
			response.close();
			// System.out.println("InternalQuery success");
			return strAns;
		} catch (IOException e) {
			System.out.println("InternalQuery faild");
			return "";
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	public synchronized InstagramApi api() {
		// затримка перед запитом Api
		try {
			Thread.sleep(700);
		} catch (InterruptedException e) {
		}
		// ProxyThread.sleepProxyThread(750l);
		return this.api;
	}

	public void getAllHeaders(HttpResponse response) {
		System.out.println("Code: " + response.getStatusLine().getStatusCode());

		for (Header h : response.getAllHeaders()) {
			System.out.println(h.getName() + " : " + h.getValue());
		}
	}

	private void checkExceededLimits(HttpResponse response, String ans) throws ExceededLimitsApi {
		if (ans.indexOf("too many requests") > 0 || response.getStatusLine().getStatusCode() == 403) {
			throw new ExceededLimitsApi();
		}
	}

	private boolean isAnswerOk(String ansJson) {
		return ansJson.equals("{\"status\":\"ok\"}");
	}
}
