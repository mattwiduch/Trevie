package com.redraysoftware.trevie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailsActivity extends AppCompatActivity {
    @Bind(R.id.movie_details_title) TextView titleTextView;
    @Bind(R.id.movie_details_release_date) TextView releaseTextView;
    @Bind(R.id.movie_details_rating) TextView ratingTextView;
    @Bind(R.id.movie_details_synopsis) TextView synopsisTextView;
    @Bind(R.id.movie_details_poster) ImageView posterImageView;
    private MovieDetails mMovieDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
