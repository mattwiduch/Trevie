/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Displays details of movie selected on the grid.
 */
public class MovieDetailsActivity extends AppCompatActivity {
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
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovie = getIntent().getParcelableExtra(Movie.EXTRA_DETAILS);
        titleTextView.setText(mMovie.getTitle());
        releaseTextView.setText(mMovie.getReleaseDate());
        ratingTextView.setText(mMovie.getRating());
        synopsisTextView.setText(mMovie.getSynopsis());
        runtimeTextView.setText(mMovie.getRuntime());
        genresTextView.setText(mMovie.getGenres());
        genresTextView.setSelected(true);
        countriesTextView.setText(mMovie.getCountries());
        countriesTextView.setSelected(true);
        Picasso.with(this).load(mMovie.getPosterPath()).into(posterImageView);

    }

}
