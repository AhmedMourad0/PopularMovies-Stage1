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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.popularmovies.R;
import inc.ahmedmourad.popularmovies.adapters.RecyclerAdapter;
import inc.ahmedmourad.popularmovies.model.database.MovieContract;
import inc.ahmedmourad.popularmovies.model.entities.SimpleMoviesEntity;
import inc.ahmedmourad.popularmovies.utils.NetworkUtils;
import inc.ahmedmourad.popularmovies.utils.PreferencesUtils;

public class MoviesController extends Controller implements RecyclerAdapter.OnClickListener {

    public static final int COL_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_OVERVIEW = 3;
    public static final int COL_VOTES_AVERAGE = 4;
    public static final int COL_RELEASE_DATE = 5;
    public static final int COL_IS_ADULT = 6;
    public static final String KEY_MODE = "mode";
    public static final int MODE_POPULAR = 0;
    public static final int MODE_TOP_RATED = 1;
    private static final String[] COLUMNS = new String[]{
            MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_OVERVIEW,
            MovieContract.MoviesEntry.COLUMN_VOTES_AVERAGE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_IS_ADULT
    };
    private final List<SimpleMoviesEntity> moviesList = new ArrayList<>();
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private int mode = MODE_POPULAR;
    private ContentObserver moviesObserver;

    public MoviesController(@Nullable final Bundle args) {
        super(args);

        if (args != null)
            mode = args.getInt(KEY_MODE, MODE_POPULAR);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container) {

        final View view = inflater.inflate(R.layout.controller_movies, container, false);

        ButterKnife.bind(this, view);

        final Context context = view.getContext();

        final RecyclerAdapter recyclerAdapter = new RecyclerAdapter(moviesList, this);

        initializeRecyclerView(context, recyclerAdapter);

        final Uri uri;

        switch (mode) {

            case MODE_POPULAR:
                uri = MovieContract.PopularEntry.CONTENT_URI;
                break;

            case MODE_TOP_RATED:
                uri = MovieContract.TopRatedEntry.CONTENT_URI;
                break;

            default:
                uri = MovieContract.PopularEntry.CONTENT_URI;
        }

        // I miss Loaders;
        // But not fragments though
        moviesObserver = new ContentObserver(new Handler(Looper.myLooper())) {
            @Override
            public void onChange(final boolean selfChange) {

                final Cursor cursor = context.getContentResolver().query(uri, COLUMNS, null, null, null);

                if (cursor != null) {

                    if (cursor.moveToFirst()) {

                        moviesList.clear();

                        do {

                            moviesList.add(SimpleMoviesEntity.fromCursor(cursor));

                        } while (cursor.moveToNext());

                        cursor.close();

                        recyclerAdapter.notifyDataSetChanged();

                    } else {

                        cursor.close();
                    }
                }
            }
        };

        if (PreferencesUtils.defaultPrefs(context).getBoolean(PreferencesUtils.KEY_IS_DATA_INITIALIZED, false))
            moviesObserver.onChange(false);

        context.getContentResolver().registerContentObserver(MovieContract.PopularEntry.CONTENT_URI, false, moviesObserver);

        initializeRefreshLayout(context);

        return view;
    }

    /**
     * initialize our refreshLayout
     * @param context context
     */
    private void initializeRefreshLayout(final Context context) {

        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.refresh_progress_background);

        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        refreshLayout.setOnRefreshListener(() -> {

            switch (mode) {

                case MODE_POPULAR:
                    context.getContentResolver().delete(MovieContract.PopularEntry.CONTENT_URI, null, null);
                    NetworkUtils.fetchPopularMoviesData(context, null);
                    refreshLayout.setRefreshing(false);
                    break;

                case MODE_TOP_RATED:
                    context.getContentResolver().delete(MovieContract.TopRatedEntry.CONTENT_URI, null, null);
                    NetworkUtils.fetchTopRatedMoviesData(context, null);
                    refreshLayout.setRefreshing(false);
                    break;
            }
        });
    }

    /**
     * initialize our recyclerView
     * @param context context
     * @param recyclerAdapter recyclerAdapter
     */
    private void initializeRecyclerView(final Context context, final RecyclerAdapter recyclerAdapter) {

        recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    protected void onDetach(@NonNull final View view) {
        view.getContext().getContentResolver().unregisterContentObserver(moviesObserver);
    }

    @Override
    public void onClick(final SimpleMoviesEntity movie) {

        final Bundle bundle = new Bundle();
        bundle.putLong(DetailsController.KEY_ID, movie.id);

        if (getParentController() != null)
            getParentController().getRouter().pushController(RouterTransaction.with(new DetailsController(bundle)));
    }
}
