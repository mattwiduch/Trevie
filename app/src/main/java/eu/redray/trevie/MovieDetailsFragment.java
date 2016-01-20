/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.redray.trevie.utility.YouTubeUri;

/**
 * Displays details of movie selected on the grid.
 */
public class MovieDetailsFragment extends Fragment {
    private final String TAG = MovieDetailsFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private Movie mMovie;

    @Bind(R.id.movie_details_layout)
    LinearLayout detailsLayout;
    @Bind(R.id.movie_details_title)
    TextView titleTextView;
    @Bind(R.id.movie_details_release_date)
    TextView releaseTextView;
    @Bind(R.id.movie_details_rating)
    TextView ratingTextView;
    @Bind(R.id.movie_details_synopsis)
    TextView synopsisTextView;
    @Bind(R.id.movie_details_duration)
    TextView runtimeTextView;
    @Bind(R.id.movie_details_genre)
    TextView genresTextView;
    @Bind(R.id.movie_details_country)
    TextView countriesTextView;
    @Bind(R.id.movie_details_poster)
    ImageView posterImageView;
    @Bind(R.id.movie_details_reviews)
    TextView reviewsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = arguments.getParcelable(Movie.EXTRA_DETAILS);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);
        ButterKnife.bind(this, rootView);

        // Populates views with basic data
        titleTextView.setText(mMovie.getTitle());
        releaseTextView.setText(mMovie.getReleaseDate());
        ratingTextView.setText(mMovie.getRating());
        synopsisTextView.setText(mMovie.getSynopsis());
        Picasso.with(getActivity()).load(mMovie.getPosterPath()).into(posterImageView);

        // Fetches additional movie details
        UpdateDetailsTask updateDetailsTask = new UpdateDetailsTask();
        updateDetailsTask.execute(mMovie);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share_trailer);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    /**
     * Creates share trailer intent
     */
    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        // Add movie title and trailer link to the intent
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mMovie.getTitle() + " Trailer");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getTrailerLinks().get(0).toString());
        return shareIntent;
    }

    /**
     * Creates dialog that lets user choose trailer to play.
     */
    @OnClick(R.id.trailer_button) void onTrailerClick() {
        final ArrayList<Uri> trailers = mMovie.getTrailerLinks();
        if (trailers.size() < 1) {
            // Show error message if there are no trailers to play
            Toast.makeText(getActivity(), getString(R.string.error_message_notrailers), Toast.LENGTH_LONG).show();
        }
        if (trailers.size() == 1) {
            // Launch trailer immediately if there is only one available
            launchTrailerIntent(trailers.get(0));
        } else {
            // Create dialog to help user choose which trailer to play if there is more than one
            final String[] trailersList = new String[trailers.size()];
            // Generate trailer names to display in the dialog
            for (int i = 0; i < trailersList.length; i++) {
                int index = i + 1;
                trailersList[i] = "Trailer #" + index;
            }

            // Create selection dialog
            final AlertDialog.Builder sortDialog = new AlertDialog.Builder(getActivity());
            sortDialog.setTitle(R.string.choose_trailer)
                    .setItems(trailersList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            launchTrailerIntent(trailers.get(which));
                            dialog.dismiss();
                        }
                    });
            sortDialog.show();
        }
    }

    /**
     * Fires intent to play trailer via native app.
     */
    private void launchTrailerIntent(Uri uri) {
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private class UpdateDetailsTask extends AsyncTask<Movie, Void, Void> {
        // Query URL values
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
        final String MOVIE_PATH = "movie";
        final String TRAILERS_PATH = "videos";
        final String REVIEWS_PATH = "reviews";
        final String API_KEY_PARAM = "api_key";

        // JSON fields to be fetched
        final String TMDB_RUNTIME = "runtime";
        final String TMDB_GENRES = "genres";
        final String TMDB_COUNTRIES = "production_countries";
        final String TMDB_NAME = "name";
        final String TMDB_RESULTS = "results";
        final String TMDB_CONTENT = "content";
        final String TMDB_KEY = "key";

        @Override
        protected Void doInBackground(Movie... params) {
            //Verify size of parameters to ensure there's something to look up
            if (params.length == 0) {
                return null;
            }

            // Movie details Uri
            Uri detailsUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(params[0].getId())
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            // Trailers Uri
            Uri trailersUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(params[0].getId())
                    .appendPath(TRAILERS_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            // Reviews Uri
            Uri reviewsUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(params[0].getId())
                    .appendPath(REVIEWS_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                    .build();

            // Retrieve JSON strings
            String detailsJsonString = getJsonString(detailsUri);
            String trailersJsonString = getJsonString(trailersUri);
            String reviewsJsonString = getJsonString(reviewsUri);
            if (detailsJsonString == null || trailersJsonString == null || reviewsJsonString == null)
                return null;

            try {
                updateMovieDetailsFromJsonData(params[0], detailsJsonString);
                updateMovieTrailersFromJsonData(params[0], trailersJsonString);
                updateMovieReviewsFromJsonData(params[0], reviewsJsonString);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Populates Fragment views with freshly fetched data
         * @param aVoid Not used
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runtimeTextView.setText(mMovie.getRuntime());
            genresTextView.setText(mMovie.getGenres());
            genresTextView.setSelected(true);
            countriesTextView.setText(mMovie.getCountries());
            countriesTextView.setSelected(true);
            if (mMovie.getUserReviews() == null || mMovie.getUserReviews().size() < 1) {
                reviewsTextView.setText(R.string.error_message_noreviews);
            } else if (mMovie.getUserReviews().size() == 1){
                reviewsTextView.setText((String)mMovie.getUserReviews().get(0));
            } else {
                reviewsTextView.setText((String)mMovie.getUserReviews().get(0));

                // Provides ratio for density pixels
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                reviewsTextView.setPadding(0, 0, 0, Math.round(16 * displayMetrics.density));

                for (int i = 1; i < mMovie.getUserReviews().size(); i++) {
                    //Add separator
                    View separator = new View(getActivity());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            Math.round(48 * displayMetrics.density),
                            Math.round(1 * displayMetrics.density));
                    layoutParams.gravity = Gravity.CENTER;
                    separator.setLayoutParams(layoutParams);
                    separator.setBackgroundColor(getResources().getColor(R.color.colorDivider));
                    detailsLayout.addView(separator);

                    // Add next review
                    TextView textView = new TextView(getActivity());
                    textView.setText((String)mMovie.getUserReviews().get(i));
                    textView.setPadding(0, Math.round(16 * displayMetrics.density),
                            0, Math.round(16 * displayMetrics.density));
                    detailsLayout.addView(textView);
                }
            }

            // Set share trailer intent if there is provider available
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareTrailerIntent());
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

        private void updateMovieReviewsFromJsonData(Movie movie, String reviewsJsonString) throws JSONException{
            JSONObject reviewsJson = new JSONObject(reviewsJsonString);
            JSONArray reviewsArray = reviewsJson.getJSONArray(TMDB_RESULTS);
            ArrayList<String> userReviews = new ArrayList<>();

            for (int j = 0; j < reviewsArray.length(); j++) {
                JSONObject review = reviewsArray.getJSONObject(j);
                userReviews.add(review.getString(TMDB_CONTENT));
            }

            movie.setUserReviews(userReviews);
        }
    }
}
