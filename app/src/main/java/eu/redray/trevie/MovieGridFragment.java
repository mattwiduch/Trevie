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
    //private int mPosition = GridView.INVALID_POSITION;
    //private int mOffset = 0;
    private static final String SELECTED_KEY = "selected_position";
    //private static final String OFFSET_KEY = "selected_offset";
    private Parcelable mGridState = null;

    //used to load additional pages
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve app state
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            //mPosition = savedInstanceState.getInt(SELECTED_KEY);
            //mOffset = savedInstanceState.getInt(OFFSET_KEY);
            //getLoaderManager().getLoader(MOVIES_LOADER_ID).reset();
            mGridState = savedInstanceState.getParcelable(SELECTED_KEY);
            //gridView.smoothScrollToPosition(savedInstanceState.getInt("LAST_VISIBLE"));
            //Log.v("TREVIE-MGF", "State retrieved. Position: " + mPosition);// + " / Offset: " + mOffset);
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList("key");
            mRestored = true;
            mPage = savedInstanceState.getInt("PAGE2LOAD");
            mMovieGridAdapter.clear();
            mMovieGridAdapter.addAll(movies);
            movies.clear();
            if (mGridState != null) {
                gridView.onRestoreInstanceState(mGridState);
            }
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
        /*
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
        */
        mGridState = gridView.onSaveInstanceState();
        Log.v("TREVIE-MGF", "Grid State Saved!! State: " + mGridState);
        outState.putParcelable(SELECTED_KEY, mGridState);
        //outState.putInt("LAST_VISIBLE", gridView.getLastVisiblePosition());
        int count = gridView.getAdapter().getCount();
        ArrayList<Movie> movieList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            movieList.add((Movie)gridView.getAdapter().getItem(i));
        }

        outState.putParcelableArrayList("key", movieList);
        outState.putInt("PAGE2LOAD", mPage);
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
