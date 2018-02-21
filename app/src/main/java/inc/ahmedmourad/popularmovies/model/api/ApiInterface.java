package inc.ahmedmourad.popularmovies.model.api;

import inc.ahmedmourad.popularmovies.BuildConfig;
import inc.ahmedmourad.popularmovies.model.api.responses.PopularMoviesResponse;
import inc.ahmedmourad.popularmovies.model.api.responses.TopRatedMoviesResponse;
import inc.ahmedmourad.popularmovies.model.entities.MoviesEntity;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {

    // http://api.themoviedb.org/3/movie/283995?api_key=XXXXXXXXXXXXXXXX //&append_to_response=trailers,reviews
    @GET("movie/{id}?api_key=" + BuildConfig.API_KEY) //+ "&append_to_response=trailers,reviews"
    Observable<MoviesEntity> getMovie(@Path("id") long id);

    // http://api.themoviedb.org/3/movie/popular?api_key=XXXXXXXXXXXXXXXX
    @GET("movie/popular?api_key=" + BuildConfig.API_KEY)
    Observable<PopularMoviesResponse> getPopularMovies();

    // http://api.themoviedb.org/3/movie/top_rated?api_key=XXXXXXXXXXXXXXXX
    @GET("movie/top_rated?api_key=" + BuildConfig.API_KEY)
    Observable<TopRatedMoviesResponse> getTopRatedMovies();
}
