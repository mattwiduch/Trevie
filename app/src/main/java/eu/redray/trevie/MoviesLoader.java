/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Creates AsyncTaskLoader to fetch movie data from TheMovieDB.
 */
public class MoviesLoader extends AsyncTaskLoader<Movie[]> {
    private final String TAG = MoviesLoader.class.getSimpleName();
    public final String VOTE_COUNT = "100";
    private String mSortParameter;
    private int mPageToLoad;
    private boolean DEBUG = false;

    // We hold a reference to the Loader's data here.
    private Movie[] mMovies;

    // Construct query URL
    final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    final String DISCOVER_PATH = "discover";
    final String MOVIE_PATH = "movie";
    final String SORT_PARAM = "sort_by";
    final String VOTE_COUNT_PARAM = "vote_count.gte";
    final String PAGE_NUMBER = "page";
    final String API_KEY_PARAM = "api_key";

    public MoviesLoader(Context context, String sortParameter, int page) {
        super(context);
        mSortParameter = sortParameter;
        mPageToLoad = page;
    }

    @Override
    public Movie[] loadInBackground() {
        //Verify size of parameters to ensure there's something to look up
        if (mSortParameter == null) {
            return null;
        }

        Uri moviesUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(DISCOVER_PATH)
                .appendPath(MOVIE_PATH)
                .appendQueryParameter(SORT_PARAM, mSortParameter)
                .appendQueryParameter(VOTE_COUNT_PARAM, VOTE_COUNT)
                .appendQueryParameter(PAGE_NUMBER, String.valueOf(mPageToLoad))
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                .build();

        String moviesJsonString = getJsonString(moviesUri);
        if (moviesJsonString == null) return null;

        try {
            return getMovieDataFromJsonString(moviesJsonString);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the data
        return null;
    }

    /** Retrieves JSON string from provided Uri. */
    private String getJsonString(Uri builtUri) {
        // Will retrieve data
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will store raw JSON response
        String jsonString = null;

        try {
            URL url = new URL(builtUri.toString());
            // Create request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                //Nothing to do
                return null;
            }

            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty
                return null;
            }
            jsonString = buffer.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // Data retrieval failed so there is no point in going ahead
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream ", e);
                }
            }
        }
        return jsonString;
    }

    /**
     * Reads movie data from JSON string and creates Movie objects using that data.
     *
     * @param moviesJsonString JSON string that contains movie data
     * @return                 the array of movies loaded from JSON string
     * @throws                 JSONException
     */
    private Movie[] getMovieDataFromJsonString(String moviesJsonString) throws JSONException {
        // Names of JSON objects to be extracted
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_AVG_RATING = "vote_average";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_ID = "id";


        JSONObject moviesJson = new JSONObject(moviesJsonString);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        Movie[] movies = new Movie[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            // Get the JSON object representing the movie
            JSONObject movie = moviesArray.getJSONObject(i);

            // Get appropriate data
            int id = movie.getInt(TMDB_ID);
            String title = movie.getString(TMDB_TITLE);
            String releaseDate = movie.getString(TMDB_RELEASE_DATE);
            String avgRating = movie.getString(TMDB_AVG_RATING);
            String overview = movie.getString(TMDB_OVERVIEW);
            String posterPath = "http://image.tmdb.org/t/p/w185" + movie.getString(TMDB_POSTER_PATH);

            // Create new movie object
            movies[i] = new Movie(id, title, releaseDate, avgRating, overview, posterPath,
                    "", "", "", null, null);
        }
        return movies;
    }

    /**
     * Called when there is new data to deliver to the client. The superclass will
     * deliver it to the registered listener (i.e. the LoaderManager), which will
     * forward the results to the client through a call to onLoadFinished.
     */
    @Override
    public void deliverResult(Movie[] movies) {
        if (isReset()) {
            if (DEBUG)
                Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (movies != null) {
                releaseResources(movies);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        Movie[] oldMovies = mMovies;
        mMovies = movies;

        if (isStarted()) {
            if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager for" +
                    " the ListFragment to display! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(movies);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldMovies != null && oldMovies != movies) {
            if (DEBUG) Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            releaseResources(oldMovies);
        }
    }

    /*********************************************************/
    /** (3) Implement the Loader�s state-dependent behavior **/
    /*********************************************************/

    @Override
    protected void onStartLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

        if (mMovies != null) {
            // Deliver any previously loaded data immediately.
            if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(mMovies);
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStopLoading() called! +++");

        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is; Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        if (DEBUG) Log.i(TAG, "+++ onReset() called! +++");

        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'apps'.
        if (mMovies != null) {
            releaseResources(mMovies);
            mMovies = null;
        }
    }

    @Override
    public void onCanceled(Movie[] movies) {
        if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(movies);

        // The load has been canceled, so we should release the resources
        // associated with 'mApps'.
        releaseResources(movies);
    }

    @Override
    public void forceLoad() {
        if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
        super.forceLoad();
    }

    /**
     * Helper method to take care of releasing resources associated with an
     * actively loaded data set.
     */
    private void releaseResources(Movie[] movies) {
        // For a simple List, there is nothing to do. For something like a Cursor,
        // we would close it in this method. All resources associated with the
        // Loader should be released here.
    }
}
