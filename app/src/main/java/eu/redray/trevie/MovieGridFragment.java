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
import android.widget.AbsListView;
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
    private static final String SELECTED_KEY = "selected_position";
    private static final String PAGE_KEY = "page_to_load";
    private static final String GRID_KEY = "grid_state";
    private Parcelable mGridState = null;

    // Used to load additional pages
    private static final int FIRST_PAGE = 1;
    private static final int LAST_PAGE = 1000;
    private int mPage = FIRST_PAGE;
    private boolean mNextPage = true;
    private boolean mRestored = false;

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

                        mMovieGridAdapter.clear();
                        // reset to first position on sort
                        //mPosition = 0;
                        mPage = FIRST_PAGE;
                        //mNextPage = false;
                        mGridState = null;
                        mRestored = false;
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
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // the list is empty, return
                if (totalItemCount == 0) return;

                // if "the first item visible on the screen" +
                // "number of item visible" == "total items actually in the list"
                // then I'm at the end, get next page
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    //since the method is called several times, check if I already get the new page
                    if (mNextPage && mPage <= LAST_PAGE) {
                        mPage++;
                        mNextPage = false;
                        //new GetUrlsAsyncTask().execute(context, mPage);
                        updateGrid();
                        Log.v("TREVIE-MGF", "Scrolled! " + (mPage - 1));
                        //getLoaderManager().getLoader(MOVIES_LOADER_ID).onContentChanged();
                    }
                } else if (!mNextPage){
                    //scrolling inside the list
                    Log.v("TREVIE-MGF", "Scrolled without load! " + (mPage));
                    mNextPage = true;
                }
            }
        });


        //Bundle mySavedInstanceState = getArguments();
        //int persistentVariable = mySavedInstanceState.getInt(SELECTED_KEY);
        //mPosition = persistentVariable;
        //Log.v("TREVIE-MGF", "onCreateView. Loading persistent..." + persistentVariable);

        return view;
    }

    /**
     * Restores grid state
     *
     * @param view View that was created
     * @param savedInstanceState Bundle with saved state
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve app state
        if (savedInstanceState != null
                && savedInstanceState.containsKey(SELECTED_KEY)
                && savedInstanceState.containsKey(GRID_KEY)
                && savedInstanceState.containsKey(PAGE_KEY)) {

            // Retrieve all movies and add them to the grid
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(GRID_KEY);
            mMovieGridAdapter.clear();
            mMovieGridAdapter.addAll(movies);

            // Retrieve grid state and restore it
            mGridState = savedInstanceState.getParcelable(SELECTED_KEY);
            if (mGridState != null) {
                gridView.onRestoreInstanceState(mGridState);
            }

            // Restore number of page to load
            mPage = savedInstanceState.getInt(PAGE_KEY);

            // Set flag so loader doesn't add data when grid is restored
            mRestored = true;
        }
    }

    /**
     * Saves grid state
     *
     * @param outState Bundle to contain saved state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Add all grid items to a list
        ArrayList<Movie> movieList = new ArrayList<>();
        for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
            movieList.add((Movie)gridView.getAdapter().getItem(i));
        }
        // Retain the list
        outState.putParcelableArrayList(GRID_KEY, movieList);

        // Retain grid state
        mGridState = gridView.onSaveInstanceState();
        outState.putParcelable(SELECTED_KEY, mGridState);

        // Retain number of page to load
        outState.putInt(PAGE_KEY, mPage);

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
        //mPosition = position;
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
        return new MoviesLoader(getActivity(), sortType, mPage);
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
    if (!mRestored) {
        if (data != null) {
            if (mPage == FIRST_PAGE) {
                mMovieGridAdapter.clear();
                Log.v("TREVIE-MGF", "Clearing GridView....");
            }
            for (Movie movie : data) {
                mMovieGridAdapter.add(movie);
            }
        }

        if (mGridState == null && mPage == FIRST_PAGE) {
            if (getActivity().findViewById(R.id.movie_detail_container) != null) {
                handler.sendEmptyMessage(0);
            } else {
                gridView.smoothScrollToPosition(0);
            }
        }
    } else {
        mRestored = false;
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
                gridView.smoothScrollToPosition(0);
            }
        }
    };

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        mMovieGridAdapter.clear();
    }

}
