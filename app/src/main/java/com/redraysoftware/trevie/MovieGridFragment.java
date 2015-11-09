package com.redraysoftware.trevie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
import butterknife.OnItemClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {
    public final String SORT_POPULARITY = "popularity.desc";
    public final String SORT_RATING = "vote_average.desc";
    public final String VOTE_COUNT = "100";
    @Bind(R.id.movie_grid)
    GridView gridView;
    private MovieDetailsAdapter mMovieDetailsAdapter;
    private SharedPreferences mSharedPreferences;

    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        mSharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grid, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            createSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createSortDialog() {
        String preferredSort = mSharedPreferences.getString(getString(R.string.pref_sort_key),
                    SORT_POPULARITY);
        int defaultChoice = -1;
        if (preferredSort.equals(SORT_POPULARITY)) defaultChoice = 0;
        if (preferredSort.equals(SORT_RATING)) defaultChoice = 1;

        final AlertDialog.Builder sortDialog = new AlertDialog.Builder(getActivity());
        sortDialog.setTitle(R.string.sort_by)
                .setSingleChoiceItems(R.array.sort_type, defaultChoice, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();

                        if (which == 0) {
                            sharedPreferencesEditor.putString(getString(R.string.pref_sort_key),
                                    SORT_POPULARITY).apply();
                        }
                        if (which == 1) {
                            sharedPreferencesEditor.putString(getString(R.string.pref_sort_key),
                                    SORT_RATING).apply();
                        }
                        sharedPreferencesEditor.apply();
                        dialog.dismiss();
                        updateGrid();
                    }
                });
        sortDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    private void updateGrid() {
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute(sortType);

        // Change subtitle in toolbar according to search type
        String subTitle = "";
        if (sortType.equals(SORT_POPULARITY)) {
            subTitle = getResources().getString(R.string.most_popular);
        }
        if (sortType.equals(SORT_RATING)) {
            subTitle = getResources().getString(R.string.highest_rated);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieDetailsAdapter = new MovieDetailsAdapter(getActivity(), R.layout.movie_grid_item, new ArrayList<Movie>());
        View view = inflater.inflate(R.layout.movie_grid_fragment, container, false);
        ButterKnife.bind(this, view);
        gridView.setAdapter(mMovieDetailsAdapter);

        return view;
    }

    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("Movie", mMovieDetailsAdapter.getItem(position));
        startActivity(intent);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
        private final String TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... params) {
            //Verify size of parameters to ensure there's something to look up
            if (params.length == 0) {
                return null;
            }

            // Will retrieve data
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will store raw JSON response
            String moviesJsonString = null;

            try {
                // Construct query URL
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
                final String DISCOVER_PATH = "discover";
                final String MOVIE_PATH = "movie";
                final String SORT_PARAM = "sort_by";
                final String VOTE_COUNT_PARAM = "vote_count.gte";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(DISCOVER_PATH)
                        .appendPath(MOVIE_PATH)
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(VOTE_COUNT_PARAM, VOTE_COUNT)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.OPEN_THE_MOVIEDB_API_KEY)
                        .build();

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
                moviesJsonString = buffer.toString();
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

        private Movie[] getMovieDataFromJsonString(String moviesJsonString) throws JSONException {

            // Names of JSON objects to be extracted
            final String TMDB_RESULTS = "results";
            final String TMDB_TITLE = "title";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_AVG_RATING = "vote_average";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POSTER_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonString);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            Movie[] movies = new Movie[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);

                // Get appropriate data
                String title = movie.getString(TMDB_TITLE);
                String releaseDate = movie.getString(TMDB_RELEASE_DATE);
                String avgRating = movie.getString(TMDB_AVG_RATING);
                String overview = movie.getString(TMDB_OVERVIEW);
                String posterPath = "http://image.tmdb.org/t/p/w185" + movie.getString(TMDB_POSTER_PATH);

                movies[i] = new Movie(title, releaseDate, avgRating, overview, posterPath);
            }

            return movies;
        }
    }
}
