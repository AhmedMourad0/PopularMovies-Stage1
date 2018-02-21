package inc.ahmedmourad.popularmovies.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import inc.ahmedmourad.popularmovies.model.database.MovieContract.MoviesEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.PopularEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.TopRatedEntry;

class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "movies.db";

    MovieDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE IF NOT EXISTS " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry.COLUMN_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                MoviesEntry.COLUMN_IS_ADULT + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_VOTES_AVERAGE + " REAL NOT NULL," +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT," +
                MoviesEntry.COLUMN_BACKDROP_PATH + " TEXT," +
                MoviesEntry.COLUMN_RUNTIME + " INTEGER," +
                MoviesEntry.COLUMN_GENRES + " TEXT," +
                MoviesEntry.COLUMN_TAGLINE + " TEXT" +
                ");";

        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE IF NOT EXISTS " + PopularEntry.TABLE_NAME + " (" +
                PopularEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + PopularEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_ID + ") ON DELETE CASCADE);";

        final String SQL_CREATE_POPULAR_INDEX =  "CREATE UNIQUE INDEX IF NOT EXISTS " + PopularEntry.INDEX_MOVIE_ID +
                " ON " + PopularEntry.TABLE_NAME + "(" + PopularEntry.COLUMN_MOVIE_ID + ");";

        final String SQL_CREATE_TOP_RATED_TABLE = "CREATE TABLE IF NOT EXISTS " + TopRatedEntry.TABLE_NAME + " (" +
                TopRatedEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TopRatedEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE," +
                " FOREIGN KEY (" + TopRatedEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_ID + ") ON DELETE CASCADE);";

        final String SQL_CREATE_TOP_RATED_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS " + TopRatedEntry.INDEX_MOVIE_ID +
                " ON " + TopRatedEntry.TABLE_NAME + "(" + TopRatedEntry.COLUMN_MOVIE_ID + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_INDEX);
        sqLiteDatabase.execSQL(SQL_CREATE_TOP_RATED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TOP_RATED_INDEX);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldVersion, final int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if we change the version number for our database.
        // It does NOT depend on the version number for our application.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP INDEX IF EXISTS " + PopularEntry.INDEX_MOVIE_ID);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TopRatedEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP INDEX IF EXISTS " + TopRatedEntry.INDEX_MOVIE_ID);

        onCreate(sqLiteDatabase);
    }
}

