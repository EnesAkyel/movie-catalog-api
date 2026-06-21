package com.moviecatalog.controller;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.moviecatalog.service.MovieService;
import com.moviecatalog.service.StudioService;
import com.moviecatalog.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
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

    private final MovieService movieService;
    private final StudioService studioService;

    public MovieController(MovieService movieService, StudioService studioService) {
        this.movieService = movieService;
        this.studioService = studioService;
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
        return ResponseEntity.ok(movieService.getMovies(genre, rating, minPrice, maxPrice, page, size));
    }

    @Operation(summary = "Get movie by ID")
    @GetMapping("/movie/{mid}")
    public ResponseEntity<Movie> getMovieByMid(@PathVariable int mid) {
        return movieService.findById(mid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a movie")
    @PostMapping(value = "/movie", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> addMovie(@RequestBody @Valid Movie movie) {
        Optional<Movie> result = movieService.add(movie);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(movie, HttpStatus.CONFLICT));
    }

    @Operation(summary = "Update a movie")
    @PutMapping(value = "/movie/{mid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> editMovie(@PathVariable int mid, @RequestBody @Valid Movie movie) {
        return movieService.update(mid, movie)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(movie, HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Delete a movie")
    @DeleteMapping("/movie/{mid}")
    public ResponseEntity<Movie> deleteMovie(@PathVariable int mid) {
        return movieService.delete(mid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List studios", description = "Returns a paginated list of studios.")
    @GetMapping("/studios")
    public ResponseEntity<PageResponse<Studio>> getAllStudios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(studioService.getStudios(page, size));
    }

    @Operation(summary = "Get movies by studio")
    @GetMapping("/studios/{sid}/movies")
    public ResponseEntity<List<Movie>> getMoviesBySid(@PathVariable int sid) {
        List<Movie> matching = movieService.findByStudio(sid);
        return matching.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(matching);
    }

    @Operation(summary = "Create a studio")
    @PostMapping(value = "/studio", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Studio> addStudio(@RequestBody @Valid Studio studio) {
        Optional<Studio> result = studioService.add(studio);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(studio, HttpStatus.CONFLICT));
    }

    @Operation(summary = "Update a studio")
    @PutMapping(value = "/studio/{sid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Studio> editStudio(@PathVariable int sid, @RequestBody @Valid Studio studio) {
        return studioService.update(sid, studio)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(studio, HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Delete a studio")
    @DeleteMapping("/studio/{sid}")
    public ResponseEntity<Studio> deleteStudio(@PathVariable int sid) {
        return studioService.delete(sid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
