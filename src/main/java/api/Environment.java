package api;

import org.apache.http.HttpHost;

/**
 * Created by Max on 20.02.2016.
 */
public enum Environment {
    INSTANCE;

    public final static HttpHost TARGET_WEB = new HttpHost("www.instagram.com", 443, "https");
    public final static HttpHost TARGET_API = new HttpHost("i.instagram.com", 443, "https");

}
