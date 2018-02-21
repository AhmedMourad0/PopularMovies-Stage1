package inc.ahmedmourad.popularmovies.view.controllers;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.conductor.Controller;
import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.popularmovies.R;
import inc.ahmedmourad.popularmovies.model.database.MovieContract;
import inc.ahmedmourad.popularmovies.model.database.MovieDatabase;
import inc.ahmedmourad.popularmovies.model.entities.MoviesEntity;
import inc.ahmedmourad.popularmovies.model.entities.MoviesGenre;
import inc.ahmedmourad.popularmovies.utils.NetworkUtils;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * This's where we display the details of our movie
 */
public class DetailsController extends Controller {

    public static final int COL_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_OVERVIEW = 3;
    public static final int COL_VOTES_AVERAGE = 4;
    public static final int COL_RELEASE_DATE = 5;
    public static final int COL_IS_ADULT = 6;
    public static final int COL_RUNTIME = 7;
    public static final int COL_BACKDROP_PATH = 8;
    public static final int COL_GENRES = 9;
    public static final int COL_TAGLINE = 10;
    static final String KEY_ID = "id";
    private static final String[] COLUMNS = new String[]{
            MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_OVERVIEW,
            MovieContract.MoviesEntry.COLUMN_VOTES_AVERAGE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_IS_ADULT,
            MovieContract.MoviesEntry.COLUMN_RUNTIME,
            MovieContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MoviesEntry.COLUMN_GENRES,
            MovieContract.MoviesEntry.COLUMN_TAGLINE
    };
    @BindView(R.id.details_overview)
    TextView overviewTextView;
    @BindView(R.id.details_runtime)
    TextView runtimeTextView;
    @BindView(R.id.details_rating)
    TextView ratingTextView;
    @BindView(R.id.details_rating_bar)
    MaterialRatingBar ratingBar;
    @BindView(R.id.details_poster)
    ImageView posterImageView;
    @BindView(R.id.details_backdrop)
    ImageView backdropImageView;
    @BindView(R.id.details_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.details_toolbar)
    Toolbar toolbar;
    @BindView(R.id.details_collapsing_toolbar)
    SubtitleCollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.details_date)
    TextView dateTextView;
    @BindView(R.id.details_genres)
    FlexboxLayout flexboxLayout;
    @BindView(R.id.details_adult)
    FrameLayout adultFrameLayout;
    private long id;
    private ContentObserver contentObserver;

    @SuppressWarnings("WeakerAccess")
    public DetailsController(@Nullable final Bundle args) {
        super(args);

        if (args != null) {

            id = args.getLong(KEY_ID);
        }
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container) {

        final View view = inflater.inflate(R.layout.controller_details, container, false);

        ButterKnife.bind(this, view);

        final Context context = view.getContext();

        initializeData(context);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        toolbar.setNavigationOnClickListener(v -> getRouter().popController(this));

        contentObserver = new ContentObserver(new Handler(Looper.myLooper())) {
            @Override
            public void onChange(final boolean selfChange) {

                initializeData(context);
            }
        };

        context.getContentResolver().registerContentObserver(MovieContract.MoviesEntry.CONTENT_URI, true, contentObserver);

        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.refresh_progress_background);

        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        refreshLayout.setOnRefreshListener(() -> {
            NetworkUtils.fetchSingleMovieData(context, id);
            refreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    protected void onDetach(@NonNull final View view) {
        view.getContext().getContentResolver().unregisterContentObserver(contentObserver);
    }

    /**
     * if complete movie is in the database display it, otherwise fetch it from the Api
     * @param context c for short
     */
    private void initializeData(final Context context) {

        if (MovieDatabase.isMovieAvailable(context, id)) {

            displayMovieData(context);

        } else {

            NetworkUtils.fetchSingleMovieData(context, id);
        }
    }

    /**
     * populate our UI with data from the database
     * @param context stub
     */
    private void displayMovieData(final Context context) {

        final Cursor cursor = context.getContentResolver()
                .query(MovieContract.MoviesEntry.buildMovieUriWithId(id),
                        COLUMNS,
                        null,
                        null,
                        null);

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                final MoviesEntity movie = MoviesEntity.fromCursor(cursor);

                cursor.close();

                toolbar.setTitle(movie.originalTitle);
                collapsingToolbar.setSubtitle(movie.tagline);

                final String posterBaseUrl = "http://image.tmdb.org/t/p/";

                final Uri posterUri = Uri.parse(posterBaseUrl)
                        .buildUpon()
                        .appendEncodedPath("w342") //"w92", "w154", "w185", "w342", "w500", "w780"
                        .appendEncodedPath(movie.posterPath)
                        .build();

                final Uri backdropUri = Uri.parse(posterBaseUrl)
                        .buildUpon()
                        .appendEncodedPath("w780") //"w92", "w154", "w185", "w342", "w500", "w780"
                        .appendEncodedPath(movie.backdropPath)
                        .build();

                Picasso.with(context)
                        .load(posterUri)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .into(posterImageView);

                Picasso.with(context)
                        .load(backdropUri)
                        .placeholder(R.drawable.placeholder_wide)
                        .error(R.drawable.error_wide)
                        .into(backdropImageView);

                String date = "";

                try {

                    date = DateFormat.format("MMM yyyy",
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .parse(movie.releaseDate)
                    ).toString();

                } catch (final ParseException e) {

                    date = date.substring(0, 4);

                    e.printStackTrace();
                }

                dateTextView.setText(date);

                final String runtime;

                final int hours = movie.runtime / 60, minutes = movie.runtime % 60;

                if (movie.runtime % 60 == 0)
                    runtime = context.getResources().getQuantityString(R.plurals.runtime_hours, hours, hours);
                else if (movie.runtime < 60)
                    runtime = context.getResources().getQuantityString(R.plurals.runtime_minutes, minutes, minutes);
                else
                    runtime = context.getResources().getQuantityString(R.plurals.runtime_hours, hours, hours) + ", " +
                            context.getResources().getQuantityString(R.plurals.runtime_minutes, minutes, minutes);

                runtimeTextView.setText(runtime);

                overviewTextView.setText(movie.overview);
                ratingTextView.setText(context.getString(R.string.rating, Double.toString(movie.votesAverage).substring(0, 3)));

                ratingBar.setRating((float) movie.votesAverage);

                if (movie.isAdult)
                    adultFrameLayout.setVisibility(View.VISIBLE);
                else
                    adultFrameLayout.setVisibility(View.GONE);

                displayGenres(context, movie.genres);

            } else {

                cursor.close();
            }

        } else {

            closeOnError(context);
        }
    }

    private void displayGenres(final Context context, final List<MoviesGenre> genres) {

        View view;

        TextView genreTextView;

        for (final MoviesGenre genre : genres) {

            view = LayoutInflater.from(context).inflate(R.layout.item_genre, flexboxLayout, false);

            genreTextView = view.findViewById(R.id.genre_name);

            genreTextView.setText(genre.name);

            flexboxLayout.addView(view);
        }
    }

    /**
     * Not exiting the app, just going back to our lists
     * @param context bla
     */
    private void closeOnError(final Context context) {

        getRouter().popController(this);

        Toast.makeText(context, R.string.error_message, Toast.LENGTH_LONG).show();
    }
}
