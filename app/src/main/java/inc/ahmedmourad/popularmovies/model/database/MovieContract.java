package inc.ahmedmourad.popularmovies.model.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

public final class MovieContract {

    static final String CONTENT_AUTHORITY = "inc.ahmedmourad.popularmovies";

    static final String PATH_MOVIES = "movies";
    static final String PATH_POPULAR = "popular";
    static final String PATH_TOP_RATED = "top_rated";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Don't need count so not implementing BaseColumns
    public static final class MoviesEntry {

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_VOTES_AVERAGE = "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_IS_ADULT = "is_adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_GENRES = "genres";
        public static final String COLUMN_TAGLINE = "tagline";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        //content://inc.ahmedmourad.popularmovies/movies/#
        public static Uri buildMovieUriWithId(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        static long getMovieIdFromUri(final Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static final class PopularEntry {

        static final String TABLE_NAME = "popular_movies";

        static final String COLUMN_ID = "id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        static final String INDEX_MOVIE_ID = "popular_movie_id_index";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;

        //content://inc.ahmedmourad.popularmovies/popular/#
        static Uri buildPopularMovieUriWithId(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class TopRatedEntry {

        static final String TABLE_NAME = "top_rated_movies";

        static final String COLUMN_ID = "id";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        static final String INDEX_MOVIE_ID = "top_rated_movie_id_index";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_RATED).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_RATED;

        //content://inc.ahmedmourad.popularmovies/top_rated/#
        static Uri buildTopRatedMovieUriWithId(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}

