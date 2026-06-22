package com.moviecatalog.dto;

import com.moviecatalog.model.Movie;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public record MovieRequest(
        @NotNull(message = "Movie ID must be present")
        @Digits(integer = 4, fraction = 0, message = "Movie ID must be an integer")
        @Range(min = 1000, max = 9999, message = "Movie ID must be a 4 digit number")
        int mid,

        @NotEmpty(message = "Name must have length greater than 0")
        @NotNull(message = "Name must be present")
        String name,

        @NotEmpty(message = "Genre must have length greater than 0")
        @NotNull(message = "Genre must be present")
        @Pattern(regexp = "^(Action|Romance|Comedy|Horror|Drama|Thriller|Sci-Fi|Fantasy|Mystery|Adventure)$",
                message = "Genre must be one of Action, Romance, Comedy, Horror, Drama, Thriller, Sci-Fi, Fantasy, Mystery, Adventure")
        String genre,

        @Positive(message = "Price must be bigger than $0.00")
        double price,

        @NotEmpty(message = "Rating must have length greater than 0")
        @NotNull(message = "Rating must be present")
        @Pattern(regexp = "^(G|PG|PG-13|R|NC-17)$", message = "Rating must be one of G, PG, PG-13, R, NC-17")
        String rating,

        @NotNull(message = "Studio ID must be present")
        @Digits(integer = 3, fraction = 0, message = "Studio ID must be an integer with up to 3 digits")
        @Range(min = 1, max = 100, message = "Studio ID must be between 1 and 100")
        @Positive(message = "Studio ID must be bigger than 0")
        int studio
) {
    public Movie toMovie() {
        Movie m = new Movie();
        m.setMID(this.mid);
        m.setName(this.name);
        m.setGenre(this.genre);
        m.setPrice(this.price);
        m.setRating(this.rating);
        m.setStudio(this.studio);
        return m;
    }
}
