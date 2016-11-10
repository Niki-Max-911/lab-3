package entity;

import api.WebApi;
import api.model.ListWithCursor;
import api.model.WebFullUser;
import connector.ConnectionFactory;
import connector.Connector;
import exception.NoPageException;
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

    public final void setLink(String url) throws NoPageException {
        username = url.replaceAll("https?://(www)?.instagram\\.com/", "").replaceAll("/", "");
        checkUser();
    }

    public boolean checkUser() {
        Connector connector = ConnectionFactory.getConnector();
        ListWithCursor<WebFullUser> webFullUser = WebApi.getWebFullUser(connector, username);
        return !webFullUser.isEmpty();
    }
}
