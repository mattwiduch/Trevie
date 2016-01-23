package eu.redray.trevie.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIES_WITH_ID = 101;
    static final int TRAILERS = 200;
    static final int TRAILERS_WITH_ID = 201;
    static final int REVIEWS = 300;
    static final int REVIEWS_WITH_ID = 301;

    @Override
    public boolean onCreate() {
        // CreatesMoviesDbHelper for later use
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    private static final SQLiteQueryBuilder sMovieByIdQueryBuilder;
    static{
        sMovieByIdQueryBuilder = new SQLiteQueryBuilder();
        sMovieByIdQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME);
    }

    //movies.movie_id = ?
    private static final String sMovieIdSelection =
            MoviesContract.MoviesEntry.TABLE_NAME +
                    "." + MoviesContract.MoviesEntry._ID + " = ? ";

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

            selection = sMovieIdSelection;
            selectionArgs = new String[]{movieId};

        return sMovieByIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private static final SQLiteQueryBuilder sTrailersByMovieIdQueryBuilder;
    static{
        sTrailersByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is left outer join which looks like
        //movies LEFT OUTER JOIN trailers ON movies._ID = trailers.movie_id ORDER BY movies._ID
        sTrailersByMovieIdQueryBuilder.setTables(
        MoviesContract.MoviesEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                MoviesContract.TrailersEntry.TABLE_NAME + " ON " +
                MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID +
                " = " + MoviesContract.TrailersEntry.TABLE_NAME + "." +
                MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY
        );
    }

    //trailers.movie_id = ?
    private static final String sTrailerByMovieIdSelection =
            MoviesContract.TrailersEntry.TABLE_NAME +
                    "." + MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + " = ? ";

    private Cursor getTrailersByMovieId(Uri uri, String[] projection, String sortOrder) {
            String movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

            String[] selectionArgs;
            String selection;

                selection = sTrailerByMovieIdSelection;
                selectionArgs = new String[]{movieId};

            return sTrailersByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
    }

    private static final SQLiteQueryBuilder sReviewsByMovieIdQueryBuilder;
    static{
        sReviewsByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        //This is left outer join which looks like
        //movies LEFT OUTER JOIN trailers ON movies._ID = trailers.movie_id ORDER BY movies._ID
        sReviewsByMovieIdQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        MoviesContract.ReviewsEntry.TABLE_NAME + " ON " +
                        MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID +
                        " = " + MoviesContract.ReviewsEntry.TABLE_NAME + "." +
                        MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY
        );
    }

    //reviews.movie_id = ?
    private static final String sReviewsByMovieIdSelection =
            MoviesContract.ReviewsEntry.TABLE_NAME +
                    "." + MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = ? ";

    private Cursor getReviewsByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sReviewsByMovieIdSelection;
        selectionArgs = new String[]{movieId};

        return sReviewsByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Matches each URI to MOVIE, MOVIE_WITH_ID, TRAILERS_WITH_ID and REVIEWS integer constants
     * @return UriMatcher
     */
    static UriMatcher buildUriMatcher() {
        // Build new UriMatcher. Uses NO_MATCH code so it doesn't return root URI
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // Adds URIs to the matcher to match each of the defined types
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS, TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS + "/#", TRAILERS_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS + "/#", REVIEWS_WITH_ID);

        // Returns the matcher
        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES_WITH_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return MoviesContract.TrailersEntry.CONTENT_TYPE;
            case TRAILERS_WITH_ID:
                return MoviesContract.TrailersEntry.CONTENT_ITEM_TYPE;
            case REVIEWS:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            case REVIEWS_WITH_ID:
                return MoviesContract.ReviewsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies/*"
            case MOVIES_WITH_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TRAILERS_WITH_ID: {
                retCursor = getTrailersByMovieId(uri, projection, sortOrder);
                break;
            }
            // "reviews"
            case REVIEWS_WITH_ID: {
                retCursor = getReviewsByMovieId(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        // Inserts data to a database determined by UriMatcher
        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {
                long _id = db.insert(MoviesContract.TrailersEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.TrailersEntry.buildTrailersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.ReviewsEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // If selection is null, delete all rows
        if ( null == selection ) selection = "1";

        // Choose database to delete from based on UriMatcher
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                rowsDeleted = db.delete(
                        MoviesContract.TrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                }

        // Notify the uri listener only if rows were deleted
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        // Choose database to update
        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRAILERS:
                rowsUpdated = db.update(MoviesContract.TrailersEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(MoviesContract.ReviewsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify listener only if database was updated
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
