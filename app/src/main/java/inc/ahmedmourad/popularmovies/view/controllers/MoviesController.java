package inc.ahmedmourad.popularmovies.view.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class MoviesController extends Controller implements RecyclerAdapter.OnClickListener, AppBarLayout.OnOffsetChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int COL_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_OVERVIEW = 3;
    public static final int COL_VOTES_AVERAGE = 4;
    public static final int COL_RELEASE_DATE = 5;
    public static final int COL_IS_ADULT = 6;

    private static final String[] COLUMNS = new String[]{
            MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_OVERVIEW,
            MovieContract.MoviesEntry.COLUMN_VOTES_AVERAGE,
            MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MovieContract.MoviesEntry.COLUMN_IS_ADULT
    };

    public static final String KEY_MODE = "mode";

    public static final int MODE_POPULAR = 0;
    public static final int MODE_TOP_RATED = 1;

    private int mode = MODE_POPULAR;

    private ContentObserver moviesObserver;

    private final List<SimpleMoviesEntity> moviesList = new ArrayList<>();

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private RecyclerAdapter recyclerAdapter;

    private SharedPreferences prefs;

    private Context context;

    private MainController parentController;

    private int item;

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

        context = view.getContext();

        prefs = PreferencesUtils.defaultPrefs(context);

        parentController = (MainController) getParentController();

        item = prefs.getInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID);

        setHasOptionsMenu(true);

        final boolean shouldUsePoster = item == PreferencesUtils.ITEM_GRID;

        if (shouldUsePoster)
            recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_poster);
        else
            recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_detailed);

        initializeRecyclerView(recyclerAdapter, shouldUsePoster);

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

        initializeRefreshLayout();

        return view;
    }

    /**
     * initialize our refreshLayout
     */
    private void initializeRefreshLayout() {

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
     *
     * @param recyclerAdapter recyclerAdapter
     * @param shouldUsePoster if the user prefers the poster layout
     */
    private void initializeRecyclerView(final RecyclerAdapter recyclerAdapter, final boolean shouldUsePoster) {

        if (shouldUsePoster)
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
        else
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {

        if (mode == MODE_POPULAR) {

            super.onCreateOptionsMenu(menu, inflater);

            if (parentController != null)
                inflater.inflate(R.menu.menu_movies, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {

        if (mode == MODE_POPULAR) {

            super.onPrepareOptionsMenu(menu);

            if (parentController != null)
                if (prefs.getInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID) == PreferencesUtils.ITEM_GRID)
                    menu.getItem(0).setIcon(R.drawable.list_linear);
                else
                    menu.getItem(0).setIcon(R.drawable.list_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {

        if (mode == prefs.getInt(PreferencesUtils.KEY_SELECTED_TAB, PreferencesUtils.TAB_POPULAR)) {

            if (parentController != null)
                if (item.getItemId() == R.id.item_layout)
                    swapLayout(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void swapLayout(@NonNull final MenuItem item) {

        final Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

        if (prefs.getInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID) == PreferencesUtils.ITEM_GRID) {

            recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_detailed);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            item.setIcon(R.drawable.list_grid);

            PreferencesUtils.edit(context, e -> e.putInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_LINEAR));

        } else {

            recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_poster);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));

            item.setIcon(R.drawable.list_linear);

            PreferencesUtils.edit(context, e -> e.putInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID));
        }

        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    @Override
    protected void onAttach(@NonNull final View view) {
        super.onAttach(view);

        context.getContentResolver().registerContentObserver(MovieContract.PopularEntry.CONTENT_URI, false, moviesObserver);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (parentController != null)
            parentController.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onDetach(@NonNull final View view) {

        context.getContentResolver().unregisterContentObserver(moviesObserver);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        if (parentController != null)
            parentController.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onClick(final SimpleMoviesEntity movie) {

        final Bundle bundle = new Bundle();
        bundle.putLong(DetailsController.KEY_ID, movie.id);

        if (parentController != null)
            parentController.getRouter().pushController(RouterTransaction.with(new DetailsController(bundle)));
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        refreshLayout.setEnabled(verticalOffset == 0);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

        if (key.equals(PreferencesUtils.KEY_ITEM)) {

            if (item != prefs.getInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID)) {

                item = prefs.getInt(PreferencesUtils.KEY_ITEM, PreferencesUtils.ITEM_GRID);

                final Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

                if (item == PreferencesUtils.ITEM_GRID) {

                    recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_poster);
                    recyclerView.setAdapter(recyclerAdapter);
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));

                } else {

                    recyclerAdapter = new RecyclerAdapter(moviesList, this, R.layout.item_movie_detailed);
                    recyclerView.setAdapter(recyclerAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                }

                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }

        }
    }
}
