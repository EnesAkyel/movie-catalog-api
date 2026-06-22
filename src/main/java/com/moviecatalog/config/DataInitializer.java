package com.moviecatalog.config;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.repository.StudioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private static final String[] MOVIE_NAMES = {
            "Inception", "The Matrix", "Interstellar", "Pulp Fiction", "The Godfather",
            "Forrest Gump", "Fight Club", "The Dark Knight", "Gladiator", "Shutter Island"
    };

    private static final String[] GENRES = {
            "Action", "Romance", "Comedy", "Horror", "Drama",
            "Thriller", "Sci-Fi", "Fantasy", "Mystery", "Adventure"
    };

    private static final String[] RATINGS = {"G", "PG", "PG-13", "R", "NC-17"};
    private static final int[] STUDIO_IDS = {1, 2, 3, 4, 5};

    private final StudioRepository studioRepository;
    private final MovieRepository movieRepository;

    public DataInitializer(StudioRepository studioRepository, MovieRepository movieRepository) {
        this.studioRepository = studioRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public void run(String... args) {
        if (studioRepository.count() > 0) {
            logger.info("Seed data already present — skipping DataInitializer");
            return;
        }

        List<Studio> studios = List.of(
                new Studio(1, "Paramount"),
                new Studio(2, "Warner Bros"),
                new Studio(3, "Universal"),
                new Studio(4, "20th Century"),
                new Studio(5, "Miramax")
        );

        studioRepository.saveAll(studios);
        logger.info("Seeded {} studios", studios.size());

        List<Movie> movies = buildSeedMovies();
        movieRepository.saveAll(movies);
        logger.info("Seeded {} movies", movies.size());
    }

    private static List<Movie> buildSeedMovies() {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            movies.add(buildMovie(i));
        }
        return movies;
    }

    private static Movie buildMovie(int i) {
        Movie movie = new Movie();
        movie.setMID(1001 + i);
        movie.setName(MOVIE_NAMES[i % MOVIE_NAMES.length]);
        movie.setGenre(GENRES[i % GENRES.length]);
        movie.setPrice((i + 1) * 3.99);
        movie.setRating(RATINGS[i % RATINGS.length]);
        movie.setStudio(STUDIO_IDS[i % STUDIO_IDS.length]);
        return movie;
    }
}
