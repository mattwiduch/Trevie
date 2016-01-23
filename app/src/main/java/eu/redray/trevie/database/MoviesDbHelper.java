package eu.redray.trevie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.redray.trevie.database.MoviesContract.MoviesEntry;
import eu.redray.trevie.database.MoviesContract.TrailersEntry;
import eu.redray.trevie.database.MoviesContract.ReviewsEntry;

/**
 * Manages a local database for movies data.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create Movies table
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                // Movie ID fetched from TheMovieDB
                MoviesEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_AVG_RATING + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_GENRES + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_COUNTRIES + " TEXT NOT NULL)";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);

        // Create trailers table
        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME + " (" +
                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // The id of a movie associated with this trailer
                TrailersEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                TrailersEntry.COLUMN_URL + " TEXT NOT NULL, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + TrailersEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID +
                "));";

        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);

        // Create reviews table
        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +
                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // The id of a movie associated with this trailer
                ReviewsEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                ReviewsEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + ReviewsEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry._ID +
                "));";

        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);
    }
}

