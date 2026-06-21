package com.moviecatalog.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public class Movie implements Comparable<Movie> {
    @NotNull(message = "Movie ID must be present")
    @Digits(integer=4,fraction=0, message = "Movie ID must be an integer")
    @Range(min=1000,max=9999, message = "Movie ID must be a 4 digit number")
    private int mid;

    @NotEmpty(message = "Name must have length greater than 0")
    @NotNull(message = "Name must be present")
    private String name;

    @NotEmpty(message = "Genre must have length greater than 0")
    @NotNull(message = "Genre must be present")
    @Pattern(regexp = "^(Action|Romance|Comedy|Horror|Drama|Thriller|Sci-Fi|Fantasy|Mystery|Adventure)$",
            message = "Genre must be one of Action, Romance, Comedy, Horror, Drama, Thriller, Sci-Fi, Fantasy, Mystery, Adventure")
    private String genre;

    @Positive(message = "Price must be bigger than $0.00")
    private double price;

    @NotEmpty(message = "Rating must have length greater than 0")
    @NotNull(message = "Rating must be present")
    @Pattern(regexp = "^(G|PG|PG-13|R|NC-17)$", message = "Rating must be one of G, PG, PG-13, R, NC-17")
    private String rating;

    @NotNull(message = "Studio ID must be present")
    @Digits(integer=3,fraction=0, message = "Studio ID must be an integer with up to 3 digits")
    @Range(min=1,max=100, message = "Studio ID must be between 1 and 100")
    @Positive(message = "Studio ID must be bigger than 0")
    private int studioID;

    @Override
    public int hashCode() {
        return Integer.hashCode(mid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Movie other = (Movie) obj;
        return this.mid == other.mid;
    }

    @Override
    public String toString() {
        return "Movie{" + "MID=" + mid + ", name=" + name + ", genre=" + genre + ", price=" + price + ", rating=" + rating + '}';
    }

    public int getMID() {
        return mid;
    }

    public void setMID(int mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getStudio() {
        return studioID;
    }

    public void setStudio(int studioID) {
        this.studioID = studioID;
    }

    @Override
    public int compareTo(Movie other) {
        return Integer.compare(this.mid, other.mid);
    }
}
