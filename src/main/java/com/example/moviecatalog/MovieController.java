package com.example.moviecatalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Movie Catalog", description = "Endpoints for managing movies and studios")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final ArrayList<Movie> movies = new ArrayList<>();
    private final ArrayList<Studio> studios = new ArrayList<>();

    public MovieController() {
        Random random = new Random();

        String[] genres = {"Action", "Romance", "Comedy", "Horror", "Drama",
                            "Thriller", "Sci-Fi", "Fantasy", "Mystery", "Adventure"};
        String[] rating = {"G", "PG", "PG-13", "R", "NC-17"};
        String[] movieNames = {"Inception", "The Matrix","Interstellar", "Pulp Fiction", "The Godfather",
                                "Forrest Gump", "Fight Club", "The Dark Knight", "Gladiator", "Shutter Island"};
        String[] studioNames = {"Paramount", "Warner Bros", "Universal", "20th Century", "Miramax"};

        for (int i = 0; i < 5; i++) {
            Studio studio = new Studio(random.nextInt(1, 100), studioNames[i]);
            studios.add(studio);
        }

        for (int i = 0; i < 30; i++) {
            Movie movie = new Movie();
            movie.setMID(random.nextInt(1000, 9999));
            movie.setName(movieNames[random.nextInt(movieNames.length)]);
            movie.setGenre(genres[random.nextInt(genres.length)]);
            movie.setPrice(random.nextInt(0, 99) + 0.99);
            movie.setRating(rating[random.nextInt(rating.length)]);
            movie.setStudio(studios.get(random.nextInt(studios.size())).getSID());
            movies.add(movie);
        }
    }

    @Operation(summary = "List movies", description = "Returns a paginated list of movies. Filter by genre, rating, and/or price range.")
    @GetMapping("/movies")
    public ResponseEntity<PageResponse<Movie>> getAllMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Movie> filtered = movies.stream()
                .filter(m -> genre == null || m.getGenre().equals(genre))
                .filter(m -> rating == null || m.getRating().equals(rating))
                .filter(m -> minPrice == null || m.getPrice() >= minPrice)
                .filter(m -> maxPrice == null || m.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Movie> content = fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex);

        return new ResponseEntity<>(new PageResponse<>(content, page, size, filtered.size()), HttpStatus.OK);
    }

    @Operation(summary = "Get movie by ID")
    @GetMapping("/movie/{mid}")
    public ResponseEntity<Movie> getMovieByMid(@PathVariable int mid) {
        for (Movie movie : movies) {
            if (movie.getMID() == mid) {
                return new ResponseEntity<>(movie, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create a movie")
    @PostMapping(value = "/movie", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> addMovie(@RequestBody @Valid Movie newMovie) {
        if (movies.contains(newMovie)) {
            logger.warn("Movie with MID {} already exists", newMovie.getMID());
            return new ResponseEntity<>(newMovie, HttpStatus.CONFLICT);
        }
        movies.add(newMovie);
        logger.info("Movie created: MID={}", newMovie.getMID());
        return new ResponseEntity<>(newMovie, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a movie")
    @PutMapping(value = "/movie/{mid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> editMovie(@PathVariable int mid, @RequestBody @Valid Movie newMovie) {
        newMovie.setMID(mid);
        if (movies.contains(newMovie)) {
            Movie existing = movies.get(movies.indexOf(newMovie));
            existing.setMID(mid);
            existing.setName(newMovie.getName());
            existing.setGenre(newMovie.getGenre());
            existing.setPrice(newMovie.getPrice());
            existing.setRating(newMovie.getRating());
            existing.setStudio(newMovie.getStudio());
            logger.info("Movie updated: MID={}", mid);
            return new ResponseEntity<>(existing, HttpStatus.OK);
        }
        logger.warn("Movie not found for update: MID={}", mid);
        return new ResponseEntity<>(newMovie, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Delete a movie")
    @DeleteMapping("/movie/{mid}")
    public ResponseEntity<Movie> deleteMovie(@PathVariable int mid) {
        Movie tempMovie = new Movie();
        tempMovie.setMID(mid);
        if (movies.contains(tempMovie)) {
            Movie movie = movies.get(movies.indexOf(tempMovie));
            movies.remove(movie);
            logger.info("Movie deleted: MID={}", mid);
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
        logger.warn("Movie not found for deletion: MID={}", mid);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "List studios", description = "Returns a paginated list of studios.")
    @GetMapping("/studios")
    public ResponseEntity<PageResponse<Studio>> getAllStudios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, studios.size());
        List<Studio> content = fromIndex >= studios.size() ? List.of() : studios.subList(fromIndex, toIndex);

        return new ResponseEntity<>(new PageResponse<>(content, page, size, studios.size()), HttpStatus.OK);
    }

    @Operation(summary = "Get movies by studio")
    @GetMapping("/studios/{sid}/movies")
    public ResponseEntity<List<Movie>> getMoviesBySid(@PathVariable int sid) {
        List<Movie> matching = new ArrayList<>();
        for (Movie movie : movies) {
            if (movie.getStudio() == sid) {
                matching.add(movie);
            }
        }
        if (!matching.isEmpty()) {
            return new ResponseEntity<>(matching, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create a studio")
    @PostMapping(value = "/studio", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Studio> addStudio(@RequestBody @Valid Studio newStudio) {
        if (studios.contains(newStudio)) {
            logger.warn("Studio with SID {} already exists", newStudio.getSID());
            return new ResponseEntity<>(newStudio, HttpStatus.CONFLICT);
        }
        studios.add(newStudio);
        logger.info("Studio created: SID={}", newStudio.getSID());
        return new ResponseEntity<>(newStudio, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a studio")
    @PutMapping(value = "/studio/{sid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Studio> editStudio(@PathVariable int sid, @RequestBody @Valid Studio newStudio) {
        newStudio.setSID(sid);
        if (studios.contains(newStudio)) {
            Studio existing = studios.get(studios.indexOf(newStudio));
            existing.setSID(sid);
            existing.setName(newStudio.getName());
            logger.info("Studio updated: SID={}", sid);
            return new ResponseEntity<>(existing, HttpStatus.OK);
        }
        logger.warn("Studio not found for update: SID={}", sid);
        return new ResponseEntity<>(newStudio, HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Delete a studio")
    @DeleteMapping("/studio/{sid}")
    public ResponseEntity<Studio> deleteStudio(@PathVariable int sid) {
        Studio tempStudio = new Studio();
        tempStudio.setSID(sid);
        if (studios.contains(tempStudio)) {
            Studio studio = studios.get(studios.indexOf(tempStudio));
            studios.remove(studio);
            logger.info("Studio deleted: SID={}", sid);
            return new ResponseEntity<>(studio, HttpStatus.OK);
        }
        logger.warn("Studio not found for deletion: SID={}", sid);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}
