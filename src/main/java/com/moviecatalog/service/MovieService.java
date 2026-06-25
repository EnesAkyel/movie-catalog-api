package com.moviecatalog.service;

import com.moviecatalog.model.Movie;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        Specification<Movie> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (genre != null)    predicates.add(cb.equal(root.get("genre"), genre));
            if (rating != null)   predicates.add(cb.equal(root.get("rating"), rating));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        if (size <= 0) {
            return new PageResponse<>(List.of(), page, 0, movieRepository.count(spec));
        }

        Page<Movie> result = movieRepository.findAll(spec, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent(), page, size, result.getTotalElements());
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
        return movieRepository.findByStudioID(sid);
    }
}
