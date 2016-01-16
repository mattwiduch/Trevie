package eu.redray.trevie;

import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.ArrayList;

import eu.redray.trevie.utility.YouTubeUri;

/**
 * Fetches movie data from the remote database in the background thread.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
    private final String TAG = FetchMoviesTask.class.getSimpleName();
    public final String VOTE_COUNT = "100";
    private MovieDetailsAdapter mMovieDetailsAdapter;

    // Construct query URL
    final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    final String DISCOVER_PATH = "discover";
    final String MOVIE_PATH = "movie";
    final String TRAILERS_PATH = "videos";
    final String REVIEWS_PATH = "reviews";
    final String SORT_PARAM = "sort_by";
    final String VOTE_COUNT_PARAM = "vote_count.gte";
    final String API_KEY_PARAM = "api_key";

    public FetchMoviesTask(MovieDetailsAdapter movieDetailsAdapter) {
        super();
        mMovieDetailsAdapter = movieDetailsAdapter;
    }

    @Override
    protected Movie[] doInBackground(String... params) {
        //Verify size of parameters to ensure there's something to look up
        if (params.length == 0) {
            return null;
        }

        Uri moviesUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(DISCOVER_PATH)
                .appendPath(MOVIE_PATH)
                .appendQueryParameter(SORT_PARAM, params[0])
                .appendQueryParameter(VOTE_COUNT_PARAM, VOTE_COUNT)
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

    @Override
    protected void onPostExecute(Movie[] results) {
        if (results != null) {
            mMovieDetailsAdapter.clear();
            for (Movie movie : results) {
                mMovieDetailsAdapter.add(movie);
            }
            // New data is back from the server.  Hooray!
        }
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
            String id = movie.getString(TMDB_ID);
            String title = movie.getString(TMDB_TITLE);
            String releaseDate = movie.getString(TMDB_RELEASE_DATE);
            String avgRating = movie.getString(TMDB_AVG_RATING);
            String overview = movie.getString(TMDB_OVERVIEW);
            String posterPath = "http://image.tmdb.org/t/p/w185" + movie.getString(TMDB_POSTER_PATH);

            /*
            // Construct query URL
            final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
            final String DISCOVER_PATH = "discover";
            final String MOVIE_PATH = "movie";
            final String TRAILERS_PATH = "videos";
            final String REVIEWS_PATH = "reviews";
            final String SORT_PARAM = "sort_by";
            final String VOTE_COUNT_PARAM = "vote_count.gte";
            final String API_KEY_PARAM = "api_key";

                Uri trailersUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(MOVIE_PATH)
                        .appendPath(id)
                        .appendPath(TRAILERS_PATH)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                        .build();
            String trailersJsonString = getJsonString(trailersUri);
            JSONObject trailersJson = new JSONObject(trailersJsonString);
            JSONArray trailersArray = trailersJson.getJSONArray(TMDB_RESULTS);
            ArrayList<Uri> trailersLinks = new ArrayList<>();

            for (int j = 0; j < trailersArray.length(); j++) {
                JSONObject trailer = trailersArray.getJSONObject(j);
                trailersLinks.add(YouTubeUri.create(trailer.getString(TMDB_KEY)));
            }

                Uri reviewsUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(MOVIE_PATH)
                        .appendPath(id)
                        .appendPath(REVIEWS_PATH)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                        .build();
                //String reviewsJsonString = getJsonString(reviewsUri);*/

            movies[i] = new Movie(id, title, releaseDate, avgRating, overview, posterPath,
                    "", "", "", null, null);
            UpdateDetailsTask updateDetailsTask = new UpdateDetailsTask();
            updateDetailsTask.execute(movies[i]);
            UpdateTrailersTask updateTrailersTask = new UpdateTrailersTask();
            updateTrailersTask.execute(movies[i]);

        }

        return movies;
    }

    private class UpdateDetailsTask extends AsyncTask<Movie, Void, Void> {
        final String TMDB_RUNTIME = "runtime";
        final String TMDB_GENRES = "genres";
        final String TMDB_COUNTRIES = "production_countries";
        final String TMDB_NAME = "name";

        @Override
        protected Void doInBackground(Movie... params) {
            //Verify size of parameters to ensure there's something to look up
            if (params.length == 0) {
                return null;
            }

            Uri detailsUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(params[0].getId())
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            String moviesJsonString = getJsonString(detailsUri);
            if (moviesJsonString == null) return null;

            try {
                updateMovieDetailsFromJsonData(params[0], getJsonString(detailsUri));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private void updateMovieDetailsFromJsonData(Movie movie, String detailsJsonString) throws JSONException{
            JSONObject detailsJson = new JSONObject(detailsJsonString);
            String runtime = detailsJson.getString(TMDB_RUNTIME) + " minutes";

            JSONArray genresArray = detailsJson.getJSONArray(TMDB_GENRES);
            String genres = "";
            String delim = "";
            for (int j = 0; j < genresArray.length(); j++) {
                JSONObject genre = genresArray.getJSONObject(j);
                genres = genres + delim + genre.getString(TMDB_NAME);
                delim = ", ";
            }

            JSONArray countriesArray = detailsJson.getJSONArray(TMDB_COUNTRIES);
            String countries = "";
            delim = "";
            for (int j = 0; j < countriesArray.length(); j++) {
                JSONObject country = countriesArray.getJSONObject(j);
                countries = countries + delim + country.getString(TMDB_NAME);
                delim = ", ";
            }

            movie.setRuntime(runtime);
            movie.setGenres(genres);
            movie.setCountries(countries);
        }
    }

    private class UpdateTrailersTask extends AsyncTask<Movie, Void, Void> {
        final String TMDB_RESULTS = "results";
        final String TMDB_KEY = "key";

        @Override
        protected Void doInBackground(Movie... params) {
            //Verify size of parameters to ensure there's something to look up
            if (params.length == 0) {
                return null;
            }

            Uri trailersUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(params[0].getId())
                    .appendPath(TRAILERS_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            String moviesJsonString = getJsonString(trailersUri);
            if (moviesJsonString == null) return null;

            try {
                updateMovieTrailersFromJsonData(params[0], getJsonString(trailersUri));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private void updateMovieTrailersFromJsonData(Movie movie, String trailersJsonString) throws JSONException{
            JSONObject trailersJson = new JSONObject(trailersJsonString);
            JSONArray trailersArray = trailersJson.getJSONArray(TMDB_RESULTS);
            ArrayList<Uri> trailersLinks = new ArrayList<>();

            for (int j = 0; j < trailersArray.length(); j++) {
                JSONObject trailer = trailersArray.getJSONObject(j);
                trailersLinks.add(YouTubeUri.create(trailer.getString(TMDB_KEY)));
            }

            movie.setTrailerLinks(trailersLinks);
        }
    }
}