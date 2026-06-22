package com.moviecatalog.service;

import com.moviecatalog.model.Movie;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public PageResponse<Movie> getMovies(String genre, String rating, Double minPrice, Double maxPrice, int page, int size) {
        List<Movie> filtered = movieRepository.findAll().stream()
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
        return movieRepository.findById(mid);
    }

    public Optional<Movie> add(Movie movie) {
        if (movieRepository.existsById(movie.getMID())) {
            logger.warn("Movie with MID {} already exists", movie.getMID());
            return Optional.empty();
        }
        logger.info("Movie created: MID={}", movie.getMID());
        return Optional.of(movieRepository.save(movie));
    }

    public Optional<Movie> update(int mid, Movie movie) {
        movie.setMID(mid);
        Optional<Movie> found = movieRepository.findById(mid);
        if (found.isEmpty()) {
            logger.warn("Movie not found for update: MID={}", mid);
            return Optional.empty();
        }
        Movie existing = found.get();
        existing.setName(movie.getName());
        existing.setGenre(movie.getGenre());
        existing.setPrice(movie.getPrice());
        existing.setRating(movie.getRating());
        existing.setStudio(movie.getStudio());
        logger.info("Movie updated: MID={}", mid);
        return Optional.of(movieRepository.save(existing));
    }

    public Optional<Movie> delete(int mid) {
        Optional<Movie> found = movieRepository.findById(mid);
        if (found.isEmpty()) {
            logger.warn("Movie not found for deletion: MID={}", mid);
            return Optional.empty();
        }
        Movie movie = found.get();
        movieRepository.delete(movie);
        logger.info("Movie deleted: MID={}", mid);
        return Optional.of(movie);
    }

    public List<Movie> findByStudio(int sid) {
        return movieRepository.findAll().stream()
                .filter(m -> m.getStudio() == sid)
                .toList();
    }
}
