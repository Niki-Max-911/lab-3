package instagramApi;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class Mail {

	public static synchronized String getLastInstagramLink(String username,
			String password) throws AuthenticationFailedException {
		String host = getPopHost(username);
		String link = check(host, username, password);
		return link;
	}

	private static String getPopHost(String username) {
		Pattern p = Pattern.compile(".*@");
		Matcher m = p.matcher(username);

		StringBuilder sb = new StringBuilder("pop.");

		if (m.find()) {
			sb.append(username.replaceFirst(m.group(0), ""));
		}

		return sb.toString();
	}

	private static String check(String host, String user, String password)
			throws AuthenticationFailedException {
		String lastLink = new String();

		try {
			// create properties field
			Properties properties = new Properties();

			properties.put("mail.pop3.host", host);
			properties.put("mail.pop3.port", "995");
			properties.put("mail.pop3s.auth", true);
			properties.put("mail.pop3.ssl.enable", true);
			properties.put("mail.pop3.starttls.enable", true);
			properties.put("mail.pop3.starttls.required", true);
			Session emailSession = Session.getDefaultInstance(properties);

			// create the POP3 store object and connect with the pop server
			Store store = emailSession.getStore("pop3");

			store.connect(host, user, password);

			// create the folder object and open it
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_ONLY);

			Pattern p = Pattern
					.compile("https:\\/\\/instagram.com\\/accounts\\/password\\/reset\\/confirm\\/.*?\\/.*?\\/");
			System.out.println("start");
			for (Message message : emailFolder.getMessages()) {
				
				String from = "";
				try {
					Address[] adr = message.getFrom();
					from = adr[0].toString() + (adr.length > 1 ? adr[1] : "");
				} catch (Exception e) {
					continue;
				}

				System.out.println(from);
				if (from.indexOf("Instagram") >= 0) {
					try {
						Multipart mp = (Multipart) message.getContent();
						int count = mp.getCount();

						for (int i = 0; i < count; i++) {
							String strPart = mp.getBodyPart(i).getContent()
									.toString();
							System.out.println(strPart);
							Matcher mc = p.matcher(strPart);
							if (mc.find()) {
								lastLink = mc.group();
							}
						}
					} catch (Exception e) {
						continue;
					}

				}
			}

			// close the store and folder objects
			emailFolder.close(false);
			store.close();

		} catch (NoSuchProviderException e) {
			System.out.println("1");
		} catch (MessagingException e) {
			System.out.println("2");
		}

		return lastLink.toString();
	}
}
