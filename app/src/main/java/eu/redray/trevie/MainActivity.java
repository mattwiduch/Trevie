/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Starts the application.
 */
public class MainActivity extends AppCompatActivity implements MovieGridFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.v("TREVIE", "onCreate");

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                //getSupportFragmentManager().beginTransaction()
                  //      .replace(R.id.movie_detail_container, new MovieDetailsFragment(), DETAILFRAGMENT_TAG)
                    //    .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("TREVIE", "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("TREVIE", "onPause");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.v("TREVIE", "onResume");
        if (mTwoPane) {
            MovieGridFragment fragment = (MovieGridFragment) getSupportFragmentManager().findFragmentById(R.id.movie_grid_fragment);
            //fragment.startMovieActivity(0);
            fragment.gridView.requestFocusFromTouch();
            fragment.gridView.setSelection(0);
            //fragment.gridView.performItemClick(
            //        fragment.gridView.getAdapter().getView(0, null, null), 0, 0);
            //fragment.gridView.getAdapter().getView(0, null, null).performClick();
            //fragment.gridView.performItemClick(null, 0, fragment.gridView.getItemIdAtPosition(0));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("TREVIE", "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("TREVIE", "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Movie movie) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(Movie.EXTRA_DETAILS, movie);

            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(Movie.EXTRA_DETAILS, movie);
            startActivity(intent);
        }
    }
}
