package api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstagramAgent {
    private String resolution;
    private String manufacturer;
    private String model;
    private String dpi;
    private int androidVersion;
    private String androidRelease;
    private String instaVer;

    @Override
    public String toString() {
        return String.format("Instagram %s Android (%d/%s; %s; %s; %s; %s; %s; smdkc210; en_UA)", instaVer,
                androidVersion, androidRelease, dpi, resolution, manufacturer, model, model);
    }
}
