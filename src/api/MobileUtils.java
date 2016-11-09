package api;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.*;

class MobileUtils {
    static String[] resolutions = {"720x1280", "320x480", "480x800", "1024x768", "1280x720", "768x1024",
            "480x320"};
    static String[] models = {"GT-N7000", "SM-N9000", "GT-I9220", "GT-I9100"};
    static String[] dpis = {"120", "160", "320", "240"};

    static Map<String, String> headers = new HashMap<String, String>();

    static {
        headers.put("Connection", "Keep-Alive");
        headers.put("X-IG-Connection-Type", "WIFI");
        headers.put("X-IG-Capabilities", "HQ==");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Host", "i.instagram.com");
    }

    static String signEnity(Map<String, String> params) {
        JSONObject jsonEntity = new JSONObject(params);
        String jsonStr = jsonEntity.toString();
        return signEnity(jsonStr);
    }

    public static String encode(String scr) {
        try {
            return URLEncoder.encode(scr, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            return "";
        }
    }

    public static String signEnity(String src) {
        String signum = getSig(src);

        StringBuffer entStrBuilder = new StringBuffer();
        entStrBuilder.append("signed_body=");
        entStrBuilder.append(signum);
        entStrBuilder.append(".");
//        String signEntity = encode(src);

        entStrBuilder.append(src);
        entStrBuilder.append("&ig_sig_key_version=4");

        return entStrBuilder.toString();
    }

    static String getSig(String text) {
        String textUTF8 = toUTF8(text);
        String secretKey = "25eace5393646842f0d0c3fb2ac7d3cfa15c052436ee86b5406a8433f54d24a5";
        String HASH_ALGORITHM = "HmacSHA256";
        try {
            Key sk = new SecretKeySpec(getBytes(secretKey), HASH_ALGORITHM);
            Mac mac = Mac.getInstance(sk.getAlgorithm());
            mac.init(sk);
            byte[] hmac = mac.doFinal(getBytes(textUTF8));
            return toHexString(hmac);
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] getBytes(String value) {
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }

    private static String toUTF8(String value) {
        try {
            return new String(value.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            new Formatter(sb).format("%02x", b);
        }
        return sb.toString();
    }

    static String getGuid() {
        return UUID.randomUUID().toString().toLowerCase();
    }

    static List<Header> getHeaders(String userAgent) {
        List<Header> headersLocal = new ArrayList<Header>();
        headers.forEach((name, value) -> headersLocal.add(new BasicHeader(name, value)));
        headersLocal.add(new BasicHeader("User-Agent", userAgent));
        return headersLocal;
    }

    static List<Header> postHeaders(String userAgent) {
        List<Header> headers = getHeaders(userAgent);
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
        return headers;
    }

    static InstagramAgent randUserAgent() {
        Random rand = new Random();
        InstagramAgent agent = new InstagramAgent();

        int idxR = new Random().nextInt(resolutions.length);
        agent.setResolution(resolutions[idxR]);

        agent.setManufacturer("samsung");

        int idxV = new Random().nextInt(models.length);
        agent.setModel(models[idxV]);

        int idxD = new Random().nextInt(dpis.length);
        agent.setDpi(resolutions[idxD]);

        agent.setAndroidVersion(rand.nextInt(2) + 10);
        agent.setAndroidRelease((rand.nextInt(3) + 1) + "." + (rand.nextInt(3) + 3) + "." + rand.nextInt(6));
        agent.setInstaVer("6.21.2");
        return agent;
    }

    static String getTimesmap() {
        return String.valueOf(System.currentTimeMillis());
    }
}
