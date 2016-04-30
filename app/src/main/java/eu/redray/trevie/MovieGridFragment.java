/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import eu.redray.trevie.database.MoviesContract;
import eu.redray.trevie.utility.ConnectionDetector;
import eu.redray.trevie.utility.FavouritesHelper;

/**
 * A fragment that contains movie grid view.
 */
public class MovieGridFragment extends Fragment {
    // Available sort types
    private final String SORT_POPULARITY = "popularity.desc";
    private final String SORT_RATING = "vote_average.desc";
    private final String SORT_FAVOURITES = "favourites";

    // Member variables
    private MovieGridAdapter mMovieGridAdapter;
    private FavouritesGridAdapter mFavouritesGridAdapter;
    private SharedPreferences mSharedPreferences;
    private ClickHandler mClickHandler;

    // Loader IDs
    private static final int MOVIES_LOADER_ID = 0;
    private static final int FAVOURITES_LOADER_ID = 1;

    // Used to (re)store app state
    private static final String GRID_KEY = "grid_state";
    private static final String PAGE_KEY = "page_to_load";
    private static final String MOVIES_KEY = "movies_list";
    private Parcelable mGridState = null;

    // Used to load additional pages
    private static final int FIRST_PAGE = 1;
    private static final int LAST_PAGE = 1000;
    private int mPage = FIRST_PAGE;
    private boolean mNextPage = true;
    private boolean mRestored = false;

    // Binds views
    @BindView(R.id.movie_grid)
    GridView gridView;
    @BindView(R.id.movie_grid_empty)
    RelativeLayout emptyGridView;
    @BindView(R.id.movie_grid_blank)
    FrameLayout blankGridView;

    // Specifies columns we need to read from movies database
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_AVG_RATING,
            MoviesContract.MoviesEntry.COLUMN_SYNOPSIS,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_RUNTIME,
            MoviesContract.MoviesEntry.COLUMN_GENRES,
            MoviesContract.MoviesEntry.COLUMN_COUNTRIES
    };

    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change too
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_RELEASE_DATE = 2;
    static final int COL_MOVIE_AVG_RATING = 3;
    static final int COL_MOVIE_SYNOPSIS = 4;
    static final int COL_MOVIE_POSTER_PATH = 5;
    static final int COL_MOVIE_RUNTIME = 6;
    static final int COL_MOVIE_GENRES = 7;
    static final int COL_MOVIE_COUNTRIES = 8;

    /** TheMovieDB Loader */
    private LoaderManager.LoaderCallbacks<Movie[]> moviesResultLoaderListener
            = new LoaderManager.LoaderCallbacks<Movie[]>() {
        @Override
        public Loader<Movie[]> onCreateLoader(int id, Bundle args) {
            String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
            return new MoviesLoader(getActivity(), sortType, mPage);
        }

        @Override
        public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
            // The flag prevents loader from adding data when we restore state
            if (!mRestored) {
                // Add loaded data to the grid
                if (data != null) {
                    if (mPage == FIRST_PAGE) {
                        mMovieGridAdapter.clear();
                    }
                    for (Movie movie : data) {
                        mMovieGridAdapter.add(movie);
                    }
                }

                // Go to first item in the grid on new query
                if (gridView.getCount() > 0 && mGridState == null && mPage == FIRST_PAGE) {
                    if (getActivity().findViewById(R.id.movie_detail_container) != null) {
                        new ClickHandler(gridView).sendEmptyMessage(100);
                    } else {
                        gridView.smoothScrollToPosition(0);
                    }
                }

                // Otherwise restore previous state
                if (mGridState != null) {
                    gridView.onRestoreInstanceState(mGridState);
                }
            } else {
                mRestored = false;
            }
        }

        @Override
        public void onLoaderReset(Loader<Movie[]> loader) {
            mMovieGridAdapter.clear();
        }
    };

    /** Favourites database Loader */
    private LoaderManager.LoaderCallbacks<Cursor> favouritesResultLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Sort order:  Ascending, by title.
            String sortOrder = MoviesContract.MoviesEntry.COLUMN_TITLE + " ASC";
            Uri moviesUri = MoviesContract.MoviesEntry.buildMoviesUri();

            return new CursorLoader(getActivity(),
                    moviesUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mFavouritesGridAdapter.swapCursor(data);
            // Retrieve State
            if (mGridState != null) {
                gridView.onRestoreInstanceState(mGridState);
            }
            // Go to first item in the grid on new query
            if (gridView.getCount() > 0 && mGridState == null) {
                if (getActivity().findViewById(R.id.movie_detail_container) != null) {
                    new ClickHandler(gridView).sendEmptyMessage(100);
                } else {
                    gridView.smoothScrollToPosition(0);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mFavouritesGridAdapter.swapCursor(null);
        }
    };

    public MovieGridFragment() {
        setArguments(new Bundle());
    }

    /** Updates MovieGrid when new favourite was added. */
    public void onFavouritesUpdate() {
        if (!gridView.getAdapter().equals(mFavouritesGridAdapter)) {
            mGridState = gridView.onSaveInstanceState();
            updateGrid();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /** DetailFragmentCallback for when an item has been selected. */
        void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable fragment to handle menu events
        setHasOptionsMenu(true);
        mSharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        // Initialises MovieLoader that fetches data from TheMovieDB
        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, moviesResultLoaderListener);
        // Initialises FavouritesLoader that fetches data from local database
        getLoaderManager().initLoader(FAVOURITES_LOADER_ID, null, favouritesResultLoaderListener);
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

    /** Creates dialog that lets user choose sorting method for movie items in the grid. */
    private void createSortDialog() {
        // Retrieves sort preference
        String preferredSort = mSharedPreferences.getString(getString(R.string.pref_sort_key),
                SORT_POPULARITY);
        // Checks if internet connection is available
        final boolean isConnected = ConnectionDetector.isInternetConnectionAvailable(getActivity());

        int defaultChoice = -1;
        if (preferredSort.equals(SORT_POPULARITY)) defaultChoice = 0;
        if (preferredSort.equals(SORT_RATING)) defaultChoice = 1;
        if (preferredSort.equals(SORT_FAVOURITES)) defaultChoice = 2;

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.sort_dialog_title);
        dialogBuilder.setSingleChoiceItems(R.array.sort_type, defaultChoice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position of the selected item
                SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();

                if (which == 0) {
                    which = getWhich(which, sharedPreferencesEditor, SORT_POPULARITY);
                }
                if (which == 1) {
                    which = getWhich(which, sharedPreferencesEditor, SORT_RATING);
                }
                if (which == 2) {
                    // Sets empty grid view
                    gridView.setEmptyView(emptyGridView);
                    gridView.setAdapter(mFavouritesGridAdapter);
                    sharedPreferencesEditor.putString(getString(R.string.pref_sort_key),
                            SORT_FAVOURITES).apply();
                }
                dialog.dismiss();

                // Clear the grid
                mMovieGridAdapter.clear();
                // Set next page to load to first
                mPage = FIRST_PAGE;
                // Remove grid state as it is not related to new query
                mGridState = null;
                // Set restored flag to false
                mRestored = false;
                // Update GridView
                updateGrid();
            }

            /** Sets correct adapter for given sort type if there is internet connection.
             *  Otherwise, displays warning and selects favourites. */
            private int getWhich(int which, SharedPreferences.Editor sharedPreferencesEditor, String sortType) {
                if (isConnected) {
                    // Removes empty grid view
                    gridView.setEmptyView(blankGridView);
                    // Sets correct adapter
                    gridView.setAdapter(mMovieGridAdapter);
                    sharedPreferencesEditor.putString(getString(R.string.pref_sort_key),
                            sortType).apply();
                } else {
                    which = 2;
                    Toast.makeText(getActivity(), R.string.error_message_no_connection, Toast.LENGTH_LONG).show();
                    Toast.makeText(getActivity(), R.string.displaying_favourites, Toast.LENGTH_LONG).show();
                }
                return which;
            }
        });

        // Creates dialog
        AlertDialog sortDialog = dialogBuilder.create();
        // Shows dialog
        sortDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    /** Updates the movie grid according to chosen sorting method. */
    private void updateGrid() {
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);

        // Change subtitle in toolbar according to search type
        String subTitle = "";
        if (sortType.equals(SORT_POPULARITY)) {
            subTitle = getResources().getString(R.string.most_popular);
        }
        if (sortType.equals(SORT_RATING)) {
            subTitle = getResources().getString(R.string.highest_rated);
        }
        if (sortType.equals(SORT_FAVOURITES)) {
            subTitle = getResources().getString(R.string.favourites);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subTitle);

        // Restart appropriate loader
        if (sortType.equals(SORT_RATING) || sortType.equals(SORT_POPULARITY)) {
            getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, moviesResultLoaderListener).forceLoad();
        } else if (sortType.equals(SORT_FAVOURITES)) {
            getLoaderManager().restartLoader(FAVOURITES_LOADER_ID, null, favouritesResultLoaderListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        ButterKnife.bind(this, view);

        // Create adapter and add it to the grid
        mMovieGridAdapter = new MovieGridAdapter(getActivity(), R.layout.item_movie_grid, new ArrayList<Movie>());
        mFavouritesGridAdapter = new FavouritesGridAdapter(getActivity(), null, 0);

        // Checks if internet connection is available
        boolean isConnected = ConnectionDetector.isInternetConnectionAvailable(getActivity());

        // Sets correct adapter
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
        if ((sortType.equals(SORT_POPULARITY) || sortType.equals(SORT_RATING)) &&
                isConnected) {
            gridView.setEmptyView(blankGridView);
            gridView.setAdapter(mMovieGridAdapter);
        } else {
            if (!isConnected && mRestored) {
                Toast.makeText(getActivity(), R.string.error_message_no_connection, Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), R.string.displaying_favourites, Toast.LENGTH_LONG).show();
            }
            gridView.setEmptyView(emptyGridView);
            gridView.setAdapter(mFavouritesGridAdapter);
        }

        // Loads additional results when user scroll to the bottom of the list
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // The list is empty
                if (totalItemCount == 0) return;
                // Disable loading on scroll for favourites
                if (mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY)
                        .equals(SORT_FAVOURITES)) return;

                // Checks if user reached the bottom of the scroll view
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    // Check if we need to load next page
                    if (mNextPage && mPage <= LAST_PAGE) {
                        mPage++;
                        mNextPage = false;
                        updateGrid();
                    }
                } else if (!mNextPage) {
                    // Scrolling inside the list
                    mNextPage = true;
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve app state
        if (savedInstanceState != null && savedInstanceState.containsKey(GRID_KEY)) {

            if (gridView.getAdapter().equals(mMovieGridAdapter) && savedInstanceState.containsKey(MOVIES_KEY)
                    && savedInstanceState.containsKey(PAGE_KEY)) {
                // Retrieve all movies and add them to the grid
                ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
                mMovieGridAdapter.clear();
                mMovieGridAdapter.addAll(movies);
                // Restore number of page to load
                mPage = savedInstanceState.getInt(PAGE_KEY);
            }

            // Retrieve grid state and restore it
            mGridState = savedInstanceState.getParcelable(GRID_KEY);

            // Set flag so loader doesn't add data when grid is restored
            mRestored = true;
        }
    }

    /** Saves grid state. */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (gridView.getAdapter().equals(mMovieGridAdapter)) {
            // Add all grid items to a list
            ArrayList<Movie> movieList = new ArrayList<>();
            for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
                movieList.add((Movie) gridView.getAdapter().getItem(i));
            }

            // Retain the list
            outState.putParcelableArrayList(MOVIES_KEY, movieList);
            // Retain number of page to load
            outState.putInt(PAGE_KEY, mPage);
        }

        // Retain grid state
        mGridState = gridView.onSaveInstanceState();
        outState.putParcelable(GRID_KEY, mGridState);

        super.onSaveInstanceState(outState);
    }

    /**
     * Starts new activity that shows details of selected movie.
     *
     * @param position the location of the movie in the grid
     */
    @OnItemClick(R.id.movie_grid)
    void startMovieActivity(int position) {
        Movie movie;
        if (gridView.getAdapter().equals(mFavouritesGridAdapter)) {
            Cursor cursor = mFavouritesGridAdapter.getCursor();
            movie = createMovie(cursor);
        } else {
            movie = mMovieGridAdapter.getItem(position);
        }
        if (movie != null) {
            ((Callback) getActivity())
                    .onItemSelected(movie);
        }
    }

    // Creates new movie object based on cursor data
    private Movie createMovie(Cursor cursor) {
        // Get movie trailers
        ArrayList<Uri> trailers = FavouritesHelper.getTrailers(getActivity(),
                String.valueOf(cursor.getInt(COL_MOVIE_ID)));
        // Get movie reviews
        ArrayList<String> reviews = FavouritesHelper.getReviews(getActivity(),
                String.valueOf(cursor.getInt(COL_MOVIE_ID)));


        return new Movie(cursor.getInt(COL_MOVIE_ID),
                cursor.getString(COL_MOVIE_TITLE),
                cursor.getString(COL_MOVIE_RELEASE_DATE),
                cursor.getString(COL_MOVIE_AVG_RATING),
                cursor.getString(COL_MOVIE_SYNOPSIS),
                cursor.getString(COL_MOVIE_POSTER_PATH),
                cursor.getString(COL_MOVIE_RUNTIME),
                cursor.getString(COL_MOVIE_GENRES),
                cursor.getString(COL_MOVIE_COUNTRIES),
                trailers,
                reviews);
    }

    static class ClickHandler extends Handler {
        private final WeakReference<GridView> mGrid;

        ClickHandler(GridView gridView) {
            mGrid = new WeakReference<>(gridView);
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                GridView grid = mGrid.get();
                grid.performItemClick(
                        grid.getAdapter().getView(0, null, null),
                        0,
                        grid.getAdapter().getItemId(0));
                grid.smoothScrollToPosition(0);
            }
        }
    }
}
