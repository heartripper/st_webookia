package it.webookia.backend.utils.foreignws.facebook;

import com.restfb.Facebook;
import com.restfb.types.NamedFacebookType;

/**
 * Class to handle response to queries to retrieve customer picture from
 * Facebook.
 */
public class Picture extends NamedFacebookType {

    private static final long serialVersionUID = -2854932488997969939L;

    @Facebook("data")
    private ImageData data;

    /**
     * This class manages responses from Facebook about user's profile images.
     * 
     */
    public static class ImageData {
        @Facebook
        private String url;

        @Facebook
        private int width;

        @Facebook
        private int height;

        @Facebook("is_silhouette")
        private boolean silhouette;
    }

    public String getUrl() {
        return data.url;
    }

    public int getWidth() {
        return data.width;
    }

    public int getHeight() {
        return data.height;
    }

    public boolean isSilhouette() {
        return data.silhouette;
    }
}
