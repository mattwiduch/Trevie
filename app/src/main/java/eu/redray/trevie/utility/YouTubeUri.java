/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie.utility;

import android.net.Uri;

/**
 * Creates YouTube Uri from given key value
 */
public class YouTubeUri {
    private final static String YT_BASE_URL = "https://www.youtube.com";
    private final static String WATCH_PATH = "watch";
    private final static String VIDEO_PARAM = "v";

    public static Uri create(String key) {
        return Uri.parse(YT_BASE_URL).buildUpon()
                .appendPath(WATCH_PATH)
                .appendQueryParameter(VIDEO_PARAM, key)
                .build();
    }
}
