package inc.ahmedmourad.popularmovies.utils;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import inc.ahmedmourad.popularmovies.R;
import inc.ahmedmourad.popularmovies.model.api.ApiClient;
import inc.ahmedmourad.popularmovies.model.api.ApiInterface;
import inc.ahmedmourad.popularmovies.model.database.MovieContract;
import inc.ahmedmourad.popularmovies.model.entities.PopularMoviesEntity;
import inc.ahmedmourad.popularmovies.model.entities.SimpleMoviesEntity;
import inc.ahmedmourad.popularmovies.model.entities.TopRatedMoviesEntity;
import io.reactivex.schedulers.Schedulers;

import static inc.ahmedmourad.popularmovies.utils.ConcurrencyUtils.runOnUiThread;

public final class NetworkUtils {

    /**
     * Fetch complete data for a single movie
     *
     * @param context The secret key
     * @param id      id
     */
    public static void fetchSingleMovieData(final Context context, final long id) {

        ApiClient.getInstance()
                .create(ApiInterface.class)
                .getMovie(id)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response != null)
                        context.getContentResolver().insert(MovieContract.MoviesEntry.CONTENT_URI, response.toContentValues());

                }, throwable -> {

                    // static import, cause it's prettier
                    runOnUiThread(context, () -> {

                        if (throwable instanceof ConnectException || throwable instanceof UnknownHostException)
                            Toast.makeText(
                                    context,
                                    R.string.network_error,
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.network_error_cause, throwable.getLocalizedMessage(), throwable.getCause().getLocalizedMessage()),
                                    Toast.LENGTH_LONG).show();
                    });

                    throwable.printStackTrace();
                });
    }

    /**
     * Fetch the 20 popular movies of the first page
     *
     * @param context Batman's Belt
     * @param client  client
     */
    public static void fetchPopularMoviesData(final Context context, ApiInterface client) {

        if (client == null)
            client = ApiClient.getInstance().create(ApiInterface.class);

        client.getPopularMovies()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response != null) {

                        final List<SimpleMoviesEntity> moviesList = response.movies;
                        final List<PopularMoviesEntity> popularList = response.getPopularMovies();

                        final ContentValues[] movieValues = new ContentValues[moviesList.size()];
                        final ContentValues[] popularValues = new ContentValues[popularList.size()];

                        for (int i = 0; i < moviesList.size(); ++i)
                            movieValues[i] = moviesList.get(i).toContentValues();

                        for (int i = 0; i < popularList.size(); ++i)
                            popularValues[i] = popularList.get(i).toContentValues();

                        context.getContentResolver().bulkInsert(MovieContract.MoviesEntry.CONTENT_URI, movieValues);
                        context.getContentResolver().bulkInsert(MovieContract.PopularEntry.CONTENT_URI, popularValues);
                    }


                }, throwable -> {

                    // static import
                    runOnUiThread(context, () -> {

                        if (throwable instanceof ConnectException || throwable instanceof UnknownHostException)
                            Toast.makeText(
                                    context,
                                    R.string.network_error,
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.network_error_cause, throwable.getLocalizedMessage(), throwable.getCause().getLocalizedMessage()),
                                    Toast.LENGTH_LONG).show();
                    });

                    throwable.printStackTrace();
                });
    }

    /**
     * Fetch the 20 top rated movies of the first page
     *
     * @param context Your secret identity
     * @param client  client
     */
    public static void fetchTopRatedMoviesData(final Context context, ApiInterface client) {

        if (client == null)
            client = ApiClient.getInstance().create(ApiInterface.class);

        client.getTopRatedMovies()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response != null) {

                        final List<SimpleMoviesEntity> moviesList = response.movies;
                        final List<TopRatedMoviesEntity> topRatedList = response.getTopRatedMovies();

                        final ContentValues[] movieValues = new ContentValues[moviesList.size()];
                        final ContentValues[] topRatedValues = new ContentValues[topRatedList.size()];

                        for (int i = 0; i < moviesList.size(); ++i)
                            movieValues[i] = moviesList.get(i).toContentValues();

                        for (int i = 0; i < topRatedList.size(); ++i)
                            topRatedValues[i] = topRatedList.get(i).toContentValues();

                        context.getContentResolver().bulkInsert(MovieContract.MoviesEntry.CONTENT_URI, movieValues);
                        context.getContentResolver().bulkInsert(MovieContract.TopRatedEntry.CONTENT_URI, topRatedValues);
                    }

                }, throwable -> {

                    // static import
                    runOnUiThread(context, () -> {

                        if (throwable instanceof ConnectException || throwable instanceof UnknownHostException)
                            Toast.makeText(
                                    context,
                                    R.string.network_error,
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.network_error_cause, throwable.getLocalizedMessage(), throwable.getCause().getLocalizedMessage()),
                                    Toast.LENGTH_LONG).show();
                    });

                    throwable.printStackTrace();
                });
    }
}
