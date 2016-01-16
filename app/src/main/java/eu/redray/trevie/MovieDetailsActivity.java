/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @Bind(R.id.movie_details_reviews)
    TextView reviewsTextView;

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
        if (mMovie.getUserReviews() == null || mMovie.getUserReviews().size() < 1) {
            reviewsTextView.setText(R.string.error_message_noreviews);
        } else if (mMovie.getUserReviews().size() == 1){
            reviewsTextView.setText((String)mMovie.getUserReviews().get(0));
        }
    }

    /**
     * Creates dialog that lets user choose trailer to play.
     */
    @OnClick(R.id.trailer_button) void onClick() {
        final ArrayList<Uri> trailers = mMovie.getTrailerLinks();
        if (trailers.size() < 1) {
            // Show error message if there are no trailers to play
            Toast.makeText(this, getString(R.string.error_message_notrailers), Toast.LENGTH_LONG).show();
        }
        if (trailers.size() == 1) {
            // Launch trailer immediately if there is only one available
            launchTrailerIntent(trailers.get(0));
        } else {
            // Create dialog to help user choose which trailer to play if there is more than one
            final String[] trailersList = new String[trailers.size()];
            // Generate trailer names to display in the dialog
            for (int i = 0; i < trailersList.length; i++) {
                int index = i + 1;
                trailersList[i] = "Trailer #" + index;
            }

            // Create selection dialog
            final AlertDialog.Builder sortDialog = new AlertDialog.Builder(this);
            sortDialog.setTitle(R.string.choose_trailer)
                    .setItems(trailersList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            launchTrailerIntent(trailers.get(which));
                            dialog.dismiss();
                        }
                    });
            sortDialog.show();
        }
    }

    /**
     * Fires intent to play trailer via native app.
     */
    private void launchTrailerIntent(Uri uri) {
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
