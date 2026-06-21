package com.moviecatalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.moviecatalog.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final List<Movie> movies = new ArrayList<>();

    public MovieService(StudioService studioService) {
        Random random = new Random();
        String[] genres = {"Action", "Romance", "Comedy", "Horror", "Drama",
                           "Thriller", "Sci-Fi", "Fantasy", "Mystery", "Adventure"};
        String[] ratings = {"G", "PG", "PG-13", "R", "NC-17"};
        String[] movieNames = {"Inception", "The Matrix", "Interstellar", "Pulp Fiction", "The Godfather",
                               "Forrest Gump", "Fight Club", "The Dark Knight", "Gladiator", "Shutter Island"};

        List<Studio> studios = studioService.getAll();
        for (int i = 0; i < 30; i++) {
            Movie movie = new Movie();
            movie.setMID(random.nextInt(1000, 9999));
            movie.setName(movieNames[random.nextInt(movieNames.length)]);
            movie.setGenre(genres[random.nextInt(genres.length)]);
            movie.setPrice(random.nextInt(0, 99) + 0.99);
            movie.setRating(ratings[random.nextInt(ratings.length)]);
            movie.setStudio(studios.get(random.nextInt(studios.size())).getSID());
            movies.add(movie);
        }
    }

    public PageResponse<Movie> getMovies(String genre, String rating, Double minPrice, Double maxPrice, int page, int size) {
        List<Movie> filtered = movies.stream()
                .filter(m -> genre == null || m.getGenre().equals(genre))
                .filter(m -> rating == null || m.getRating().equals(rating))
                .filter(m -> minPrice == null || m.getPrice() >= minPrice)
                .filter(m -> maxPrice == null || m.getPrice() <= maxPrice)
                .toList();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Movie> content = fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex);
        return new PageResponse<>(content, page, size, filtered.size());
    }

    public Optional<Movie> findById(int mid) {
        return movies.stream().filter(m -> m.getMID() == mid).findFirst();
    }

    public Optional<Movie> add(Movie movie) {
        if (movies.contains(movie)) {
            logger.warn("Movie with MID {} already exists", movie.getMID());
            return Optional.empty();
        }
        movies.add(movie);
        logger.info("Movie created: MID={}", movie.getMID());
        return Optional.of(movie);
    }

    public Optional<Movie> update(int mid, Movie movie) {
        movie.setMID(mid);
        if (!movies.contains(movie)) {
            logger.warn("Movie not found for update: MID={}", mid);
            return Optional.empty();
        }
        Movie existing = movies.get(movies.indexOf(movie));
        existing.setMID(mid);
        existing.setName(movie.getName());
        existing.setGenre(movie.getGenre());
        existing.setPrice(movie.getPrice());
        existing.setRating(movie.getRating());
        existing.setStudio(movie.getStudio());
        logger.info("Movie updated: MID={}", mid);
        return Optional.of(existing);
    }

    public Optional<Movie> delete(int mid) {
        Movie temp = new Movie();
        temp.setMID(mid);
        if (!movies.contains(temp)) {
            logger.warn("Movie not found for deletion: MID={}", mid);
            return Optional.empty();
        }
        Movie movie = movies.get(movies.indexOf(temp));
        movies.remove(movie);
        logger.info("Movie deleted: MID={}", mid);
        return Optional.of(movie);
    }

    public List<Movie> findByStudio(int sid) {
        return movies.stream().filter(m -> m.getStudio() == sid).toList();
    }
}
