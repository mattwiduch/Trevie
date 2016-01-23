package eu.redray.trevie.utility;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import eu.redray.trevie.Movie;
import eu.redray.trevie.database.MoviesContract;

/**
 * Created by frano on 23/01/2016.
 */
public class FavouritesHelper {
    public static void removeMovie(Context context, Movie movie) {
        // Remove movie from DB
        context.getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                MoviesContract.MoviesEntry._ID + " = ?",
                new String[]{String.valueOf(movie.getId())});

        // Remove all related trailers
        context.getContentResolver().delete(MoviesContract.TrailersEntry.CONTENT_URI,
                MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{String.valueOf(movie.getId())});

        // Remove all related reviews
        context.getContentResolver().delete(MoviesContract.ReviewsEntry.CONTENT_URI,
                MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{String.valueOf(movie.getId())});

        // Remove poster image from storage
        PosterHeper.deletePoster(context, movie.getTitle());
    }

    public static void addMovie(Context context, Movie movie, Bitmap poster) {
        ContentValues movieValues = new ContentValues();

        // Save poster image
        String posterPath = PosterHeper.savePoster(context,
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
}
