package instagramApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Formatter;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Mobile {
	private BasicCookieStore cookieStore = new BasicCookieStore();
	private CloseableHttpClient browser = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	private String login = "";
	private String password = "";
	private String userAgent = "";
	private String guid = "";
	private static Formatter formatterGuid;

	public Mobile(String login, String pass) {
		this.login = login.trim();
		this.password = pass.trim();
		randUserAgent();
		randGuid();
	}

	public boolean signInMobileApp() {
		HttpPost post = new HttpPost("https://instagram.com/api/v1/accounts/login/");
		post.setHeader("Connection", "Keep-Alive");
		post.setHeader("Content-Type", "application/x-www-form-urlencoded;");
		post.setHeader("Keep-Alive", "300");
		post.setHeader("User-Agent", userAgent);

		JSONObject auth = new JSONObject();
		auth.put("username", login);
		auth.put("password", password);
		auth.put("device_id", "android-" + guid);
		auth.put("guid", guid);
		// auth.put("Content-Type",
		// "application/x-www-form-urlencoded; charset=UTF-8");

		String jsonString = auth.toString();
		String sigAuth = getSig(jsonString);

		StringBuffer authStringBuff = new StringBuffer();
		authStringBuff.append("signed_body=");
		authStringBuff.append(sigAuth);
		authStringBuff.append(".");
		authStringBuff.append(jsonString);
		authStringBuff.append("&ig_sig_key_version=4");

		StringEntity authEntity = new StringEntity(authStringBuff.toString(), Charset.forName("UTF-8"));

		post.setEntity(authEntity);
		String ans;
		try {
			ans = EntityUtils.toString(browser.execute(post).getEntity());
			JSONObject ansJson = new JSONObject(ans);
			return ansJson.getString("status").equals("ok");
		} catch (IOException | JSONException e) {
			return false;
		}
	}

	public String uploadPhotography(InputStream photoStream) {
		// toload.jpg
		File tempFile = null;
		try {
			tempFile = File.createTempFile("toload", ".jpg");
			tempFile.deleteOnExit();

			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(photoStream, out);

		} catch (IOException e) {
			return "";
		}
		return uploadFile(tempFile);
		// return setPhotoConfig(mediaId, caption);
	}

	public String uploadFile(String way) {
		File fileToUpLoad = new File(way);
		return uploadFile(fileToUpLoad);
	}

	public String uploadFile(File fileToUpLoad) {
		HttpPost post = new HttpPost("http://instagram.com/api/v1/media/upload/");
		post.setHeader("Content-Type", "multipart/form-data; boundary=--------102614091332811");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		post.setHeader("Connection", "Keep-Alive");
		post.setHeader("Accept-Encoding", "identity");
		post.setHeader("Content-Transfer-Encoding", "quoted-printable");
		post.setHeader("User-Agent", userAgent);

		FileBody fileBodyToUpLoad = new FileBody(fileToUpLoad, ContentType.create("image/jpeg"), "toload.jpg");

		String timesmap = getTimesmap();

		HttpEntity entity = MultipartEntityBuilder.create().setBoundary("--------102614091332811")
				.addTextBody("device_timestamp", timesmap, ContentType.create("text/plain", "UTF-8"))
				// ContentType.create("text/plain", "ISO8859-1"))
				.addPart("photo", fileBodyToUpLoad).build();

		post.setEntity(entity);

		HttpResponse resp;
		try {
			resp = browser.execute(post);
			String ans = EntityUtils.toString(resp.getEntity());
			System.out.println("upload: " + ans);

			JSONObject obj = new JSONObject(ans);
			String media_id = obj.getString("media_id");
			return media_id;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getNotifications() {
		HttpUriRequest post = RequestBuilder.post().setUri("https://instagram.com/api/v1/news/inbox/")
				.addHeader("Connection", "Keep-Alive").addHeader("Content-Type", "application/x-www-form-urlencoded;")
				.addHeader("Keep-Alive", "300").addHeader("User-Agent", userAgent).build();

		HttpResponse resp;
		try {
			resp = browser.execute(post);
			String ans = EntityUtils.toString(resp.getEntity());
			return ans;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public boolean setPhotoConfig(String mediaId, String caption) {
		HttpPost post = new HttpPost("http://instagram.com/api/v1/media/configure/");
		post.setHeader("Content-Type", "application/x-www-form-urlencoded;");
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Accept", "*/*");
		post.setHeader("Connection", "Keep-Alive");
		post.setHeader("Accept-Encoding", "gzip");
		post.setHeader("User-Agent", userAgent);

		String deviceId = "android-" + guid;
		String timesmap = getTimesmap();

		JSONObject extra = new JSONObject();
		extra.put("source_width", "640");
		extra.put("source_height", "640");

		JSONObject main = new JSONObject();
		main.put("device_id", deviceId);
		main.put("guid", guid);
		main.put("media_id", mediaId);
		main.put("caption", caption.replaceAll("&|/+", "").trim());
		main.put("device_timestamp", timesmap);
		main.put("source_type", "5");
		main.put("filter_type", "0");
		// main.put("extra", extra);
		// main.put("Content-Type",
		// "application/x-www-form-urlencoded; charset=UTF-8");

		String configJson = main.toString();
		String configSig = getSig(configJson);
		String configStr = "signed_body=" + configSig + "." + configJson + "&ig_sig_key_version=4";

		StringEntity configEntity = new StringEntity(configStr, Charset.forName("UTF-8"));
		post.setEntity(configEntity);

		try {
			HttpResponse response = browser.execute(post);
			String ans = EntityUtils.toString(response.getEntity());

			System.out.println("Aafter config: " + ans);

			JSONObject obj = new JSONObject(ans);
			String status = obj.getString("status");
			return status.equals("ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getTimesmap() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void randGuid() {
		formatterGuid = new Formatter();
		Random rand = new Random();

		formatterGuid = formatterGuid.format("%04x%04x-%04x-%04x-%04x-%04x%04x%04x", rand.nextInt(65536),
				rand.nextInt(65536), rand.nextInt(65536), (rand.nextInt(4096) + 16384), (rand.nextInt(16384) + 32768),
				rand.nextInt(65536), rand.nextInt(65536), rand.nextInt(65536));

		this.guid = formatterGuid.toString().toUpperCase();
	}

	private void randUserAgent() {
		Random rand = new Random();

		String[] resolutions = { "720x1280", "320x480", "480x800", "1024x768", "1280x720", "768x1024", "480x320" };
		String[] versions = { "GT-N7000", "SM-N9000", "GT-I9220", "GT-I9100" };
		String[] dpis = { "120", "160", "320", "240" };

		int idxR = new Random().nextInt(resolutions.length);
		String resolution = (resolutions[idxR]);

		int idxV = new Random().nextInt(versions.length);
		String version = (resolutions[idxV]);

		int idxD = new Random().nextInt(dpis.length);
		String dpi = (resolutions[idxD]);

		this.userAgent = "Instagram 4." + (rand.nextInt(2) + 1) + "." + rand.nextInt(3) + " Android ("
				+ (rand.nextInt(2) + 10) + "/" + (rand.nextInt(3) + 1) + "." + (rand.nextInt(3) + 3) + "."
				+ rand.nextInt(6) + "; " + dpi + "; " + resolution + "; samsung; " + version + "; " + version
				+ "; smdkc210; en_US)";
	}

	private String getSig(String text) {
		String secretKey = "b4a23f5e39b5929e0666ac5de94c89d1618a2916";
		String HASH_ALGORITHM = "HmacSHA256";
		try {
			Key sk = new SecretKeySpec(secretKey.getBytes(), HASH_ALGORITHM);
			Mac mac = Mac.getInstance(sk.getAlgorithm());
			mac.init(sk);
			final byte[] hmac = mac.doFinal(text.getBytes());
			return toHexString(hmac);
		} catch (Exception e) {
			return "";
		}
	}

	private String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);

		for (byte b : bytes) {
			new Formatter(sb).format("%02x", b);
		}
		return sb.toString();
	}
}
