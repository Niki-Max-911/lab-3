package types;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NotifiComment {
	private String text;
	private String userNameOfOwner;
	private String mediaId;
	private String when;
	private String imageUrl;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserNameOfOwner() {
		return userNameOfOwner;
	}

	public void setUserNameOfOwner(String userNameOfOwner) {
		this.userNameOfOwner = userNameOfOwner;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public static List<NotifiComment> getAllComments(String html) {
		List<NotifiComment> listOfNotifi = new LinkedList<NotifiComment>();
		Document doc = Jsoup.parse(html);

		Elements comments = doc.getElementsByAttributeValueMatching("class",
				Pattern.compile(".*comment-activity.*"));
		for (Element el : comments) {
			try {
				listOfNotifi.add(parseComment(el));
			} catch (Exception e) {
			}
		}
		return listOfNotifi;
	}

	private static NotifiComment parseComment(Element comment) {
		NotifiComment notifiComment = new NotifiComment();
		String mediaUrl = comment.child(0).attr("href");
		if (mediaUrl != null && !mediaUrl.isEmpty()) {
			mediaUrl = mediaUrl.replaceAll("instagram://media\\?id=", "")
					.trim();
		}
		notifiComment.setMediaId(mediaUrl);

		String ownerNickName = comment.child(1).attr("href");
		if (ownerNickName != null && !ownerNickName.isEmpty()) {
			ownerNickName = ownerNickName.replaceAll(
					"instagram://user\\?username=", "").trim();
		}
		notifiComment.setUserNameOfOwner(ownerNickName);

		String text = comment.getElementsByTag("p").get(0).text();
		text = text.replaceFirst("left a comment on your photo:", "")
				.replaceFirst(ownerNickName, "").trim();
		notifiComment.setText(text);

		String when = comment.getElementsByTag("p").get(1).text();
		notifiComment.setWhen(when);

		String imageUrl = comment.getElementsByTag("img").get(0).attr("src");
		notifiComment.setImageUrl(imageUrl);
		return notifiComment;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
