package inc.ahmedmourad.popularmovies.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.popularmovies.R;
import inc.ahmedmourad.popularmovies.model.entities.SimpleMoviesEntity;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final List<SimpleMoviesEntity> moviesList;
    private final OnClickListener onClickListener;

    public RecyclerAdapter(final List<SimpleMoviesEntity> moviesList, final OnClickListener onClickListener) {
        this.moviesList = moviesList;
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup container, final int viewType) {

        return new ViewHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.item_movie, container, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.bind(moviesList.get(position), onClickListener);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    @FunctionalInterface
    public interface OnClickListener {
        void onClick(final SimpleMoviesEntity movie);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.poster)
        ImageView poster;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.rating)
        MaterialRatingBar rating;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void bind(final SimpleMoviesEntity movie, final OnClickListener onClickListener) {

            final String posterBaseUrl = "http://image.tmdb.org/t/p/";
            final String pathSize = "w342"; //"w92", "w154", "w185", "w342", "w500", "w780"

            final Uri uri = Uri.parse(posterBaseUrl).buildUpon()
                    .appendEncodedPath(pathSize)
                    .appendEncodedPath(movie.posterPath)
                    .build();

            Picasso.with(itemView.getContext())
                    .load(uri.toString())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(poster);

            itemView.setOnClickListener(v -> onClickListener.onClick(movie));

            title.setText(movie.originalTitle);
            rating.setRating((float) movie.votesAverage);
        }
    }
}
