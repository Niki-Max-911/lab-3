package entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum Donor {
    donor;
    private String username;
    @Setter
    private int countPhotoCopy;
    @Setter
    private boolean copyOldDescription;
    @Setter
    private String appendDescription;

    public final void setLink(String url) {
        username = url.replaceAll("https?://(www)?.instagram\\.com/", "").replaceAll("/", "");
    }
}
