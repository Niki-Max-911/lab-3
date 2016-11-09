package product;

import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 * Created by niki.max on 09.11.2016.
 */
public class Version {

    @Getter
    private static String productVersion = "1.0.1";

    private static final String MANIFEST_ATTRIBUTE = "X-Git-Last-Tag";

    static {
        URLClassLoader cl = (URLClassLoader) Version.class.getClassLoader();
        try {
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());
            manifest.getMainAttributes().forEach((o, o2) -> System.out.println(o + " : " + o2));
            String value = manifest.getMainAttributes().getValue(MANIFEST_ATTRIBUTE);
            if (value != null) {
                productVersion = value;
            }
        } catch (IOException E) {
            productVersion = "1.0.1";
        }
    }
}
