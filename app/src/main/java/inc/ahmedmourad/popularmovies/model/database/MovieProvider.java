package inc.ahmedmourad.popularmovies.model.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import inc.ahmedmourad.popularmovies.model.database.MovieContract.MoviesEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.PopularEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.TopRatedEntry;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static MovieDbHelper movieDbHelper;
    private SQLiteDatabase writableDatabase;

    private static final int MOVIE = 100;

    private static final int POPULAR = 101;
    private static final int TOP_RATED = 102;

    private static final int SINGLE_MOVIE_BY_ID = 200;

    private static final String singleMovieSelection =
            MoviesEntry.TABLE_NAME +
                    "." + MoviesEntry.COLUMN_ID + " = ?";

    private static final SQLiteQueryBuilder singleMovieQueryBuilder;
    private static final SQLiteQueryBuilder moviePopularQueryBuilder;
    private static final SQLiteQueryBuilder movieTopRatedQueryBuilder;

    static {

        singleMovieQueryBuilder = new SQLiteQueryBuilder();
        moviePopularQueryBuilder = new SQLiteQueryBuilder();
        movieTopRatedQueryBuilder = new SQLiteQueryBuilder();

        singleMovieQueryBuilder.setTables(MoviesEntry.TABLE_NAME);

        moviePopularQueryBuilder.setTables(
                PopularEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesEntry.TABLE_NAME +
                        " ON " + PopularEntry.TABLE_NAME +
                        "." + PopularEntry.COLUMN_MOVIE_ID + " = " +
                        MoviesEntry.TABLE_NAME +
                        "." + MoviesEntry.COLUMN_ID);

        movieTopRatedQueryBuilder.setTables(
                TopRatedEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesEntry.TABLE_NAME +
                        " ON " + TopRatedEntry.TABLE_NAME +
                        "." + TopRatedEntry.COLUMN_MOVIE_ID + " = " +
                        MoviesEntry.TABLE_NAME +
                        "." + MoviesEntry.COLUMN_ID);
    }

    private Cursor getSingleMovieById(final Uri uri, final String[] projection, final String sortOrder) {

        final long id = MoviesEntry.getMovieIdFromUri(uri);

        return singleMovieQueryBuilder.query(movieDbHelper.getReadableDatabase(),
                projection,
                singleMovieSelection,
                new String[]{Long.toString(id)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getPopularMovies(final String[] projection) {

        return moviePopularQueryBuilder.query(movieDbHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                PopularEntry.TABLE_NAME + "." + PopularEntry.COLUMN_ID + " ASC"
        );
    }

    private Cursor getTopRatedMovies(final String[] projection) {

        return movieTopRatedQueryBuilder.query(movieDbHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                TopRatedEntry.TABLE_NAME + "." + TopRatedEntry.COLUMN_ID + " ASC"
        );
    }

    /**
     * I know what you're thinking.  Why create a UriMatcher when you can use regular
     * expressions instead? Because you're not crazy, that's why.
     *
     * @return the uri matcher
     */
    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        //Updating
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_POPULAR, POPULAR);
        matcher.addURI(authority, MovieContract.PATH_TOP_RATED, TOP_RATED);

        //Querying
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", SINGLE_MOVIE_BY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        writableDatabase = movieDbHelper.getWritableDatabase();
        return true;
    }

    @Override
    public String getType(@NonNull final Uri uri) {

        final int match = uriMatcher.match(uri);

        switch (match) {

            case SINGLE_MOVIE_BY_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;

            case MOVIE:
                return MoviesEntry.CONTENT_TYPE;

            case POPULAR:
                return PopularEntry.CONTENT_TYPE;

            case TOP_RATED:
                return TopRatedEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {

        final Cursor retCursor;

        switch (uriMatcher.match(uri)) {

            case MOVIE: {
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case POPULAR: {
                retCursor = getPopularMovies(projection);
                break;
            }

            case TOP_RATED: {
                retCursor = getTopRatedMovies(projection);
                break;
            }

            case SINGLE_MOVIE_BY_ID: {
                retCursor = getSingleMovieById(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null)
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {

        final Uri returnUri;

        switch (uriMatcher.match(uri)) {

            case MOVIE: {

                final long id = writableDatabase.insert(MoviesEntry.TABLE_NAME, null, values);

                if (id > 0)
                    returnUri = MoviesEntry.buildMovieUriWithId(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }

            case POPULAR: {

                final long id = writableDatabase.insert(PopularEntry.TABLE_NAME, null, values);

                if (id > 0)
                    returnUri = PopularEntry.buildPopularMovieUriWithId(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }

            case TOP_RATED: {

                final long id = writableDatabase.insert(TopRatedEntry.TABLE_NAME, null, values);

                if (id > 0)
                    returnUri = TopRatedEntry.buildTopRatedMovieUriWithId(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull final Uri uri, String selection, final String[] selectionArgs) {

        final int rowsDeleted;

        if (selection == null)
            selection = "1";

        switch (uriMatcher.match(uri)) {

            case MOVIE: {
                rowsDeleted = writableDatabase.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case POPULAR: {
                rowsDeleted = writableDatabase.delete(PopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case TOP_RATED: {
                rowsDeleted = writableDatabase.delete(TopRatedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {

        final int rowsUpdated;

        switch (uriMatcher.match(uri)) {

            case MOVIE: {
                rowsUpdated = writableDatabase.update(MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case POPULAR: {
                rowsUpdated = writableDatabase.update(PopularEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case TOP_RATED: {
                rowsUpdated = writableDatabase.update(TopRatedEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values) {

        int returnCount = 0;

        switch (uriMatcher.match(uri)) {

            case MOVIE: {

                writableDatabase.beginTransaction();

                for (final ContentValues value : values) {

                    final long id = writableDatabase.insert(MoviesEntry.TABLE_NAME, null, value);

                    if (id != -1)
                        returnCount++;
                }

                break;
            }

            case POPULAR: {

                writableDatabase.beginTransaction();

                for (final ContentValues value : values) {

                    final long id = writableDatabase.insert(PopularEntry.TABLE_NAME, null, value);

                    if (id != -1)
                        returnCount++;
                }

                break;
            }

            case TOP_RATED: {

                writableDatabase.beginTransaction();

                for (final ContentValues value : values) {

                    final long id = writableDatabase.insert(TopRatedEntry.TABLE_NAME, null, value);

                    if (id != -1)
                        returnCount++;
                }

                break;
            }

            default:
                return super.bulkInsert(uri, values);
        }

        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();

        if (getContext() != null && returnCount != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return returnCount;
    }
}
