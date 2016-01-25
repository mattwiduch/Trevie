/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract {
    // Symbolic name of the entire provider
    public static final String CONTENT_AUTHORITY = "eu.redray.trevie";

    // Base URI to contact Content Provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    /* Inner class that defines the table contents of the movies table */
    public static final class MoviesEntry implements BaseColumns {
        // Name of a database table
        public static final String TABLE_NAME = "movies";

        // Movie title, stored as string
        public static final String COLUMN_TITLE = "title";
        // Release date, stored as string
        public static final String COLUMN_RELEASE_DATE = "release_date";
        // Average rating, stored as string
        public static final String COLUMN_AVG_RATING = "avg_rating";
        // Movie overview, stored as string
        public static final String COLUMN_SYNOPSIS = "synopsis";
        // Path to poster, stored as string
        public static final String COLUMN_POSTER_PATH = "poster_path";
        // Runtime in minutes, stored as string
        public static final String COLUMN_RUNTIME = "runtime";
        // Movie genres, stored as string
        public static final String COLUMN_GENRES = "genres";
        // Production countries, stored as string
        public static final String COLUMN_COUNTRIES = "countries";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Methods used to build movies content URIs
        public static Uri buildMoviesUri() {
            return CONTENT_URI.buildUpon().build();
        }

        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Inner class that defines the table contents of the trailers table
     */
    public static final class TrailersEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailers";

        // Column with the foreign key into the movies table
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        // Column with trailer's URL
        public static final String COLUMN_URL = "url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        // Builds trailers content URI
        public static Uri buildTrailersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }

    /**
     * Inner class that defines the table contents of the trailers table
     */
    public static final class ReviewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        // Column with the foreign key into the movies table
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        // Column with review text
        public static final String COLUMN_REVIEW = "review";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Builds reviews content URI
        public static Uri buildReviewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}
