package entitys;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Scanner;

public class PhotoCloner {
	private LinkedList<Recipient> recipients = new LinkedList<Recipient>();

	public LinkedList<Recipient> getRecipientList() {
		return this.recipients;
	}

	public void parseFile(String way) throws FileNotFoundException {
		Scanner in = new Scanner(new File(way));
		while (in.hasNext()) {
			String login;
			String pass;
			try {
				String logPas = ":";
				logPas = in.nextLine().trim();

				int lbl = logPas.indexOf("" + ':');
				login = logPas.substring(0, lbl);
				pass = logPas.substring(lbl + 1);
			} catch (Exception e) {
				continue;
			}

			try {
				login = URLEncoder.encode(login, "UTF8");
				pass = URLEncoder.encode(pass, "UTF8");
			} catch (Exception e) {
				continue;
			}

			this.recipients.add(new Recipient(login, pass));
		}
		in.close();
	}

	public boolean isCompleted() {
		boolean b = true;
		for (Recipient rec : recipients) {
			if (!rec.isUploaded()) {
				b = false;
			}
		}
		return b;
	}
}
