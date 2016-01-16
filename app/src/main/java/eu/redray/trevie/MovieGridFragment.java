/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
public class MovieGridFragment extends Fragment {
    public final String SORT_POPULARITY = "popularity.desc";
    public final String SORT_RATING = "vote_average.desc";

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


    /**
     * Updates the movie grid according to chosen sorting method.
     */
    private void updateGrid() {
        String sortType = mSharedPreferences.getString(getString(R.string.pref_sort_key), SORT_POPULARITY);
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(mMovieDetailsAdapter);
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


    /**
     * Starts new activity that shows details of selected movie.
     *
     * @param position the location of the movie in the grid
     */
    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra(Movie.EXTRA_DETAILS, mMovieDetailsAdapter.getItem(position));
        startActivity(intent);
    }
}
