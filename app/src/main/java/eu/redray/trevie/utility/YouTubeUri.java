package eu.redray.trevie.utility;

import android.net.Uri;

/**
 * Created by frano on 16/01/2016.
 */
public class YouTubeUri {

    final static String YT_BASE_URL = "https://www.youtube.com";
    final static String WATCH_PATH = "watch";
    final static String VIDEO_PARAM = "v";

    public static Uri create(String key) {
        Uri uri = Uri.parse(YT_BASE_URL).buildUpon()
                .appendPath(WATCH_PATH)
                .appendQueryParameter(VIDEO_PARAM, key)
                .build();

        return uri;
    }
}
