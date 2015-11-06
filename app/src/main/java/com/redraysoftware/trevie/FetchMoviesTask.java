package com.redraysoftware.trevie;

import android.os.AsyncTask;

/**
 * Created by frano on 06/11/2015.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
    @Override
    protected Movie[] doInBackground(String... params) {
        return new Movie[0];
    }
}
