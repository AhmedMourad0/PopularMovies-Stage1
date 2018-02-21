package inc.ahmedmourad.popularmovies.model.entities;

import com.google.gson.annotations.SerializedName;

public class MoviesGenre {

    @SerializedName(value = "name")
    public String name = "";

    // For Gson
    public MoviesGenre() {

    }

    MoviesGenre(final String name) {
        this.name = name;
    }
}
