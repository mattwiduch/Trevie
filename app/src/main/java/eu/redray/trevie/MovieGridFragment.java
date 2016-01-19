/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * A fragment that contains movie grid view.
 */
public class MovieGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Movie[]> {
    public final String SORT_POPULARITY = "popularity.desc";
    public final String SORT_RATING = "vote_average.desc";
    private static final int MOVIES_LOADER_ID = 0;

    @Bind(R.id.movie_grid)
    GridView gridView;
    private MovieGridAdapter mMovieGridAdapter;
    private SharedPreferences mSharedPreferences;

    // Used to (re)store app state
    private int mPosition = GridView.INVALID_POSITION;
    //private int mOffset = 0;
    private static final String SELECTED_KEY = "selected_position";
    //private static final String OFFSET_KEY = "selected_offset";
    private Parcelable mGridState = null;

    public MovieGridFragment() {
        setArguments(new Bundle());
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        mSharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        Log.v("TREVIE-MGF", "onCreate");
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

    /**
     * Creates dialog that lets user choose sorting method for movie items in the grid.
     */
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
                        dialog.dismiss();

                        // reset to first position on sort
                        mPosition = 0;
                        //mOffset = 0;
                        // Update GridView
                        updateGrid();
                    }
                });
        sortDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
        Log.v("TREVIE-MGF", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.v("TREVIE-MGF", "onPause. Saving... " + mPosition);
        //getArguments().putInt(SELECTED_KEY, gridView.getLastVisiblePosition());
    }

    /**
     * Updates the movie grid according to chosen sorting method.
     */
    private void updateGrid() {
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
        //FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(mMovieGridAdapter);
        //fetchMoviesTask.execute(sortType);

        // Change subtitle in toolbar according to search type
        String subTitle = "";
        if (sortType.equals(SORT_POPULARITY)) {
            subTitle = getResources().getString(R.string.most_popular);
        }
        if (sortType.equals(SORT_RATING)) {
            subTitle = getResources().getString(R.string.highest_rated);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subTitle);
        Log.v("TREVIE-MGF", "UpdateGrid");
        getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieGridAdapter = new MovieGridAdapter(getActivity(), R.layout.movie_grid_item, new ArrayList<Movie>());
        View view = inflater.inflate(R.layout.movie_grid_fragment, container, false);
        ButterKnife.bind(this, view);
        gridView.setAdapter(mMovieGridAdapter);


        //Bundle mySavedInstanceState = getArguments();
        //int persistentVariable = mySavedInstanceState.getInt(SELECTED_KEY);
        //mPosition = persistentVariable;
        //Log.v("TREVIE-MGF", "onCreateView. Loading persistent..." + persistentVariable);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve app state
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            //mOffset = savedInstanceState.getInt(OFFSET_KEY);
            mGridState = savedInstanceState.getParcelable(SELECTED_KEY);
            Log.v("TREVIE-MGF", "State retrieved. Position: " + mPosition);// + " / Offset: " + mOffset);
        }

    }

    /**
     * Saves instance state
     *
     * @param outState Bundle containing all saved information
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Calculates offset from top of the view to selected item
        //View v = gridView.getChildAt(0);
        //int offset = (v == null) ? 0 : (v.getTop() - gridView.getPaddingTop());

        // Saves position of currently selected grid item, if any
        if (getActivity().findViewById(R.id.movie_detail_container) != null && mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
            //outState.putInt(OFFSET_KEY, offset);
            Log.v("TREVIE-MGF", "State Saved!! Position: " + mPosition);// + " / Offset: " + offset);
        } else {
            outState.putInt(SELECTED_KEY, gridView.getLastVisiblePosition());
            //outState.putInt(OFFSET_KEY, offset);
            Log.v("TREVIE-MGF", "First Visible Saved!! Position: " + gridView.getLastVisiblePosition());// + " / Offset: " + offset);
        }
        mGridState = gridView.onSaveInstanceState();
        Log.v("TREVIE-MGF", "Grid State Saved!! State: " + mGridState);
        outState.putParcelable(SELECTED_KEY, mGridState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Starts new activity that shows details of selected movie.
     *
     * @param position the location of the movie in the grid
     */
    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Log.v("TREVIE-MGF", "UpdateDetails " + position);
        mPosition = position;
        Movie movie = mMovieGridAdapter.getItem(position);
        if (movie != null) {
            ((Callback) getActivity())
                    .onItemSelected(movie);
        }
    }

    /**
     *  LOADER CALLBACKS
     */

    @Override
    public Loader<Movie[]> onCreateLoader(int id, Bundle args) {
        Log.v("TREVIE-MGF", "Create Loader");
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
        return new MoviesLoader(getActivity(), sortType);
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
        if (data != null) {
            mMovieGridAdapter.clear();
            for (Movie movie : data) {
                mMovieGridAdapter.add(movie);
            }
        }

        if (getActivity().findViewById(R.id.movie_detail_container) != null
                && mGridState == null) {//(mPosition == GridView.INVALID_POSITION || mPosition == 0)) {
            handler.sendEmptyMessage(0);
        }
        //gridView.smoothScrollToPosition(mPosition);
        Log.v("TREVIE-MGF", "Load Finished " + mPosition);
        if (mGridState != null) {
            Log.v("TREVIE-MGF", "Restoring GridView state...");
            gridView.onRestoreInstanceState(mGridState);
        }

    }


    // Performs click on first grid item
    private Handler handler = new Handler()  { // handler for commiting fragment after data is loaded
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                Log.d("TREVIE-MGF", "onload finished : handler called. setting the fragment.");
                gridView.performItemClick(
                        gridView.getAdapter().getView(0, null, null),
                        0,
                        gridView.getAdapter().getItemId(0));
            }
        }
    };

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        mMovieGridAdapter.clear();
    }
}
