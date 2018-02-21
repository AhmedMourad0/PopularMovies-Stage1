package inc.ahmedmourad.popularmovies.model.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import inc.ahmedmourad.popularmovies.model.database.MovieContract.MoviesEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.PopularEntry;
import inc.ahmedmourad.popularmovies.model.database.MovieContract.TopRatedEntry;
import inc.ahmedmourad.popularmovies.utils.PreferencesUtils;

public final class MovieDatabase {

    /**
     * checks whether our data are not complete and needs to sync
     * @param context context
     * @return whether we need to sync our data or not
     */
    public static boolean needsSync(final Context context) {

        final Cursor cursor = context.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                new String[]{MoviesEntry.COLUMN_ID},
                null,
                null,
                null);

        boolean result = true;

        if (cursor != null) {

            // movies tables must have at least 20 records
            result = cursor.getCount() < 20;

            cursor.close();
        }

        return result;
    }

    /**
     * clears everything in our database for a fresh start
     * @param context context
     */
    public static void reset(final Context context) {

        PreferencesUtils.edit(context, e -> e.putBoolean(PreferencesUtils.KEY_IS_DATA_INITIALIZED, false));

        final ContentResolver contentResolver = context.getContentResolver();

        contentResolver.delete(MoviesEntry.CONTENT_URI, null, null);
        contentResolver.delete(PopularEntry.CONTENT_URI, null, null);
        contentResolver.delete(TopRatedEntry.CONTENT_URI, null, null);
    }

    /**
     * checks if the complete version of this movie is available
     * @param context context
     * @param id the movie id
     * @return 'true' if the complete version of the movie exists, 'false' otherwise
     */
    public static boolean isMovieAvailable(final Context context, final long id) {

        final Cursor cursor = context.getContentResolver()
                .query(MoviesEntry.buildMovieUriWithId(id),
                        new String[]{MoviesEntry.COLUMN_GENRES},
                        null,
                        null,
                        null);

        final boolean cursorIsNull = cursor == null;

        final boolean isAvailable =
                !cursorIsNull && cursor.moveToFirst() && cursor.getString(0) != null;

        if (!cursorIsNull)
            cursor.close();

        return isAvailable;
    }
}
