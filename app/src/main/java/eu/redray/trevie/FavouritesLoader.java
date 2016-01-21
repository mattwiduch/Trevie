package eu.redray.trevie;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by frano on 18/01/2016.
 */
public class FavouritesLoader extends AsyncTaskLoader<Movie[]> {
    private final String TAG = MoviesLoader.class.getSimpleName();
    public final String VOTE_COUNT = "100";
    private boolean DEBUG = false;

    // We hold a reference to the Loader's data here.
    private Movie[] mMovies;

    // Query URL values
    final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    final String MOVIE_PATH = "movie";
    final String API_KEY_PARAM = "api_key";

    public FavouritesLoader(Context context) {
        super(context);
    }

    @Override
    public Movie[] loadInBackground() {
        // Get ids of all favourite movies
        Set<String> favourites = getContext().getSharedPreferences(
                getContext().getString(R.string.preference_favourite_movies), Context.MODE_PRIVATE)
                .getStringSet(getContext().getString(R.string.preference_favourite_movies),
                        new HashSet<String>());

        ArrayList<Movie> movies = new ArrayList<>();

        // Fetch details for each movie
        for (String id : favourites) {
            // Movie details Uri
            Uri movieUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(id)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            String moviesJsonString = getJsonString(movieUri);
            if (moviesJsonString == null) return null;

            try {
                movies.add(getMovieDataFromJsonString(moviesJsonString));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return movies.toArray(new Movie[movies.size()]);
    }

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

            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
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
     * @throws JSONException
     */
    private Movie getMovieDataFromJsonString(String moviesJsonString) throws JSONException {
        // Names of JSON objects to be extracted
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_AVG_RATING = "vote_average";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_ID = "id";

        JSONObject movieJson = new JSONObject(moviesJsonString);

        // Get appropriate data
        String id = movieJson.getString(TMDB_ID);
        String title = movieJson.getString(TMDB_TITLE);
        String releaseDate = movieJson.getString(TMDB_RELEASE_DATE);
        String avgRating = movieJson.getString(TMDB_AVG_RATING);
        String overview = movieJson.getString(TMDB_OVERVIEW);
        String posterPath = "http://image.tmdb.org/t/p/w185" + movieJson.getString(TMDB_POSTER_PATH);

        return new Movie(id, title, releaseDate, avgRating, overview, posterPath,
                "", "", "", null, null);
    }

    /**
     * Called when there is new data to deliver to the client. The superclass will
     * deliver it to the registered listener (i.e. the LoaderManager), which will
     * forward the results to the client through a call to onLoadFinished.
     */
    @Override
    public void deliverResult(Movie[] movies) {
        if (isReset()) {
            if (DEBUG) Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
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
        if (mMovies!= null) {
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
