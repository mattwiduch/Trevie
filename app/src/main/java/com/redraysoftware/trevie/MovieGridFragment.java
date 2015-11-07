package com.redraysoftware.trevie;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {
    @Bind(R.id.movie_grid) GridView gridView;
    private List<Movie> mMovieList;

    public MovieGridFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute("sort");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //placeholder items
        mMovieList = new ArrayList<Movie>();
        for (int i = 0; i < 33; i++) {
            Movie item = new Movie();
            mMovieList.add(item);
        }

        View view = inflater.inflate(R.layout.movie_grid_fragment, container, false);
        ButterKnife.bind(this, view);
        gridView.setAdapter(new MovieDetailsAdapter(getActivity(), R.layout.movie_grid_item, mMovieList));

        return view;
    }

    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("Movie", mMovieList.get(position));
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
                        .appendQueryParameter(SORT_PARAM, "vote_average.desc")
                        .appendQueryParameter(VOTE_COUNT_PARAM, "100")
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
            } catch (IOException e){
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

        private Movie[] getMovieDataFromJsonString(String moviesJsonString) {
            return new Movie[0];
        }
    }
}
