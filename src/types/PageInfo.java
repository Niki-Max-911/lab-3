package types;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageInfo {
	public enum Sex {
		MALE, FEMALE, NONE;
	}

	private String loadedStr;
	private Sex sex;
	private String name;
	private String mail;
	private String userName;
	private String bio;
	private String webSite;
	private String phone;
	private boolean isCheckBoxSelected;
	private String msg;

	public PageInfo(String loadedStr) {
		this.loadedStr = loadedStr;
		msg = "";
	}

	public void load() {
		try {
			loadedStr = new String(loadedStr.getBytes("ISO8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		name = parseName();
		mail = parseEmail();
		userName = parseUsername();
		bio = parseBio();
		webSite = parseWebSite();
		phone = parsePhoneNumber();
		isCheckBoxSelected = parseChainingEnabled();
		sex = parseSex();
		msg = parseAnsMsgEdit();
	}

	private String parseAnsMsgEdit() {
		try {
			int startAllert = loadedStr.indexOf("class=\"alert-");
			startAllert = loadedStr.indexOf(">", startAllert) + 1;
			int endAllert = loadedStr.indexOf("<", startAllert);
			return new String(loadedStr.substring(startAllert, endAllert).trim());
		} catch (Exception e) {
			return "";
		}
	}

	private String parseName() {
		try {
			Pattern name = Pattern.compile("<input.*?name=\"first_name\".*?>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				int startName = localStr.indexOf("value=\"");
				if (startName != -1) {
					startName += "value=\"".length();
					int endName = localStr.indexOf("\"", startName);
					return new String(localStr.substring(startName, endName).trim());
				}
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String parseEmail() {
		try {
			Pattern name = Pattern.compile("<input.*?name=\"email\".*?>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				int startName = localStr.indexOf("value=\"");
				if (startName != -1) {
					startName += "value=\"".length();
					int endName = localStr.indexOf("\"", startName);
					return new String(localStr.substring(startName, endName).trim());
				}
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String parseUsername() {
		try {
			Pattern name = Pattern.compile("<input.*?name=\"username\".*?>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				int startName = localStr.indexOf("value=\"");
				if (startName != -1) {
					startName += "value=\"".length();
					int endName = localStr.indexOf("\"", startName);
					return new String(localStr.substring(startName, endName).trim());
				}
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String parsePhoneNumber() {
		try {
			Pattern name = Pattern.compile("<input.*?name=\"phone_number\".*?>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				int startName = localStr.indexOf("value=\"");
				if (startName != -1) {
					startName += "value=\"".length();
					int endName = localStr.indexOf("\"", startName);
					return new String(localStr.substring(startName, endName).trim());
				}
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String parseBio() {
		try {
			Pattern name = Pattern.compile("<textarea.*?name=\"biography\".*?>(.|^|$|\n)*?textarea>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				Pattern nameSecond = Pattern.compile(">(.|^|$|\n)*?<");
				Matcher mSecond = nameSecond.matcher(localStr);

				if (mSecond.find())
					return mSecond.group().replaceAll("<", "").replaceAll(">", "");
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private String parseWebSite() {
		try {
			Pattern name = Pattern.compile("<input.*?name=\"external_url\".*?>");
			Matcher m = name.matcher(loadedStr);

			if (m.find()) {
				String localStr = m.group();
				int startName = localStr.indexOf("value=\"");
				if (startName != -1) {
					startName += "value=\"".length();
					int endName = localStr.indexOf("\"", startName);
					return new String(localStr.substring(startName, endName).trim());
				}
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private boolean parseChainingEnabled() {
		try {
			int startName = loadedStr.indexOf("name=\"chaining_enabled\"");
			startName = loadedStr.indexOf("checked=\"checked\"", startName);

			return startName != -1;
		} catch (Exception e) {
			return false;
		}
	}

	private Sex parseSex() {
		try {
			int endName = loadedStr.indexOf("name=\"gender_section\"");
			endName = loadedStr.indexOf("name=\"gender\"", endName);
			endName = loadedStr.indexOf("selected=\"selected\"", endName);
			endName = loadedStr.lastIndexOf("\"", endName);
			int startName = loadedStr.lastIndexOf("value=\"", endName);

			String s = new String(loadedStr.substring(startName, endName)).replaceAll("\\D", "");
			int value = Integer.valueOf(s);

			switch (value) {
			case 1:
				return Sex.MALE;
			case 2:
				return Sex.FEMALE;
			default:
				return Sex.NONE;
			}
		} catch (Exception e) {
			return Sex.NONE;
		}
	}

	public String getSex() {
		if (sex == Sex.MALE)
			return "1";
		if (sex == Sex.FEMALE)
			return "2";
		return "3";
	}

	public String getName() {
		return name;
	}

	public String getMail() {
		return mail;
	}

	public String getUserName() {
		return userName;
	}

	public String getBio() {
		return bio;
	}

	public String getWebSite() {
		return webSite;
	}

	public String getPhone() {
		return phone;
	}

	public String getMessage() {
		return msg;
	}

	public boolean isCheckBoxSelected() {
		return isCheckBoxSelected;
	}

	public void setSex(int sex) {
		switch (sex) {
		case 1:
			this.sex = Sex.MALE;
			break;
		case 2:
			this.sex = Sex.FEMALE;
			break;
		default:
			this.sex = Sex.NONE;
			break;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setUserName(String username) {
		userName = username;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setCheckBoxSelected(boolean isCheckBoxSelected) {
		this.isCheckBoxSelected = isCheckBoxSelected;
	}
}