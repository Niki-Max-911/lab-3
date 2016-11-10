package test;

import api.InstagramAgent;
import api.MobileUtils;
import org.apache.http.Header;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by niki.max on 10.11.2016.
 */
public class MobileUtilsTest {

    @Test
    public void testSign() {
        String targetString = "Hello KPI!";
        String expectedSig = "259c2801ef955f4e1152a466fdaf66f7f6d9d5775dc5df83ae0f1f8507942a51";

        String sig = MobileUtils.getSig(targetString);
        Assert.assertEquals("Sign key form target text", expectedSig, sig);
    }

    @Test
    public void testHeaders() {
        List<Header> getHeaders = MobileUtils.getHeaders("test-user-agent");
        List<Header> postHeaders = MobileUtils.postHeaders("test-user-agent");


        List<String> getHeaderKeys = getHeaders.stream()
                .map(Header::getName)
                .collect(Collectors.toList());


        List<String> postHeaderKeys = postHeaders.stream()
                .map(Header::getName)
                .collect(Collectors.toList());


        Assert.assertTrue(postHeaderKeys.containsAll(getHeaderKeys));
        Assert.assertFalse(getHeaders.size() == postHeaders.size());
    }

    @Test
    public void testIGAgent() {
        InstagramAgent instagramAgent = MobileUtils.randUserAgent();
        Assert.assertFalse(instagramAgent.toString().isEmpty());
    }
}
