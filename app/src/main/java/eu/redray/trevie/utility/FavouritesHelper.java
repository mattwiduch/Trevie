/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

import eu.redray.trevie.Movie;
import eu.redray.trevie.database.MoviesContract;

/**
 * Provides methods for retrieving and deleting movie data from favourites database
 */
public class FavouritesHelper {
    // Specifies columns we need to read from trailers database
    private static final String[] TRAILERS_COLUMNS = {
            MoviesContract.TrailersEntry.TABLE_NAME + "." + MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY,
            MoviesContract.TrailersEntry.COLUMN_URL
    };
    // These indices are tied to TRAILER_COLUMNS
    static final int COL_TRAILER_URL = 1;

    // Specifies columns we need to read from reviews database
    private static final String[] REVIEWS_COLUMNS = {
            MoviesContract.ReviewsEntry.TABLE_NAME + "." + MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY,
            MoviesContract.ReviewsEntry.COLUMN_REVIEW
    };
    // These indices are tied to TRAILER_COLUMNS
    static final int COL_REVIEW_CONTENT = 1;

    /**
     * Removes provided movie from favourites database
     *
     * @param context the context of the app giving access to content provider
     * @param movieId the id of a movie to be removed from the database
     */
    public static void removeMovie(Context context, String movieId) {
        // Remove movie from DB
        context.getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                MoviesContract.MoviesEntry._ID + " = ?",
                new String[]{movieId});

        // Remove all related trailers
        context.getContentResolver().delete(MoviesContract.TrailersEntry.CONTENT_URI,
                MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{movieId});

        // Remove all related reviews
        context.getContentResolver().delete(MoviesContract.ReviewsEntry.CONTENT_URI,
                MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{movieId});

        // Remove poster image from storage
        PosterHelper.deletePoster(context, movieId);
    }

    /**
     * Adds provided movie to favourites database
     *
     * @param context the context of the app giving access to content provider
     * @param movie the movie object to be added to the database
     * @param poster the poster of a movie provided as bitmap
     */
    public static void addMovie(Context context, Movie movie, Bitmap poster) {
        ContentValues movieValues = new ContentValues();

        // Save poster image
        String posterPath = PosterHelper.savePoster(context,
                poster,
                movie.getTitle());

        movieValues.put(MoviesContract.MoviesEntry._ID, movie.getId());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, movie.getTitle());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_AVG_RATING, movie.getRating());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_COUNTRIES, movie.getCountries());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_GENRES, movie.getGenres());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,movie.getReleaseDate());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RUNTIME, movie.getRuntime());
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        // Add movie to DB
        context.getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValues);

        // Populate trailers db with links for this particular movie
        if (movie.getTrailerLinks() != null) {
            for (Object trailer : movie.getTrailerLinks()) {
                ContentValues trailersValues = new ContentValues();
                trailersValues.put(MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY, movie.getId());
                trailersValues.put(MoviesContract.TrailersEntry.COLUMN_URL, trailer.toString());
                context.getContentResolver().insert(MoviesContract.TrailersEntry.CONTENT_URI, trailersValues);
            }
        }

        // Populate reviews db with reviews for this particular movie
        if (movie.getUserReviews() != null) {
            for (Object review : movie.getUserReviews()) {
                ContentValues reviewsValues = new ContentValues();
                reviewsValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY, movie.getId());
                reviewsValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW, review.toString());
                context.getContentResolver().insert(MoviesContract.ReviewsEntry.CONTENT_URI, reviewsValues);
            }
        }

    }

    /**
     * Retrieves trailers associated with given movie
     *
     * @param context the context of the app giving access to content provider
     * @param movieId the id of a movie
     * @return        an array list of trailer URIs associated with given movie id
     */
    public static ArrayList<Uri> getTrailers(Context context, String movieId) {
        Cursor trailersCursor = context.getContentResolver().query(
                MoviesContract.TrailersEntry.buildTrailerId(movieId),
                TRAILERS_COLUMNS,
                null,
                null,
                null
        );
        ArrayList<Uri> trailers = new ArrayList<>();

        if (trailersCursor != null && trailersCursor.moveToFirst()) {
            do {
                Uri link = Uri.parse(trailersCursor.getString(COL_TRAILER_URL));
                trailers.add(link);
            } while (trailersCursor.moveToNext());
            trailersCursor.close();
        }
        return trailers;
    }

    /**
     * Retrieve user reviews for given movie
     *
     * @param context the context of the app giving access to content provider
     * @param movieId the id of a movie
     * @return        an array list of reviews associated with given movie id
     */
    public static ArrayList<String> getReviews(Context context, String movieId) {
        Cursor reviewsCursor = context.getContentResolver().query(
                MoviesContract.ReviewsEntry.buildReviewId(movieId),
                REVIEWS_COLUMNS,
                null,
                null,
                null
        );
        ArrayList<String> reviews = new ArrayList<>();

        if (reviewsCursor != null && reviewsCursor.moveToFirst()) {
            do {
                String review = reviewsCursor.getString(COL_REVIEW_CONTENT);
                reviews.add(review);
            } while (reviewsCursor.moveToNext());
            reviewsCursor.close();
        }
        return reviews;
    }
}
