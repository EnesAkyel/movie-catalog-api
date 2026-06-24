package com.moviecatalog.service;

import com.moviecatalog.model.Movie;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        String[] genres = {"Action", "Romance", "Comedy", "Horror", "Drama",
                           "Thriller", "Sci-Fi", "Fantasy", "Mystery", "Adventure"};
        String[] ratings = {"G", "PG", "PG-13", "R", "NC-17"};
        List<Movie> seeded = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Movie m = new Movie();
            m.setMID(1001 + i);
            m.setName("Movie " + i);
            m.setGenre(genres[i % genres.length]);
            m.setPrice((i + 1) * 3.99);
            m.setRating(ratings[i % ratings.length]);
            m.setStudio((i % 5) + 1);
            seeded.add(m);
        }
        lenient().when(movieRepository.findAll()).thenReturn(seeded);
        movieService = new MovieService(movieRepository);
    }

    private Movie movie(int mid) {
        Movie m = new Movie();
        m.setMID(mid);
        m.setName("Test Movie");
        m.setGenre("Action");
        m.setPrice(9.99);
        m.setRating("PG-13");
        m.setStudio(1);
        return m;
    }

    @Test
    void getMovies_noFilters_returns30Movies() {
        PageResponse<Movie> result = movieService.getMovies(null, null, null, null, 0, 100);
        assertEquals(30, result.getTotalElements());
    }

    @Test
    void getMovies_filterByGenre_returnsOnlyMatching() {
        PageResponse<Movie> all = movieService.getMovies(null, null, null, null, 0, 100);
        long expected = all.getContent().stream().filter(m -> "Action".equals(m.getGenre())).count();

        PageResponse<Movie> result = movieService.getMovies("Action", null, null, null, 0, 100);
        assertEquals(expected, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(m -> "Action".equals(m.getGenre())));
    }

    @Test
    void getMovies_filterByRating_returnsOnlyMatching() {
        PageResponse<Movie> all = movieService.getMovies(null, null, null, null, 0, 100);
        long expected = all.getContent().stream().filter(m -> "PG-13".equals(m.getRating())).count();

        PageResponse<Movie> result = movieService.getMovies(null, "PG-13", null, null, 0, 100);
        assertEquals(expected, result.getTotalElements());
    }

    @Test
    void getMovies_filterByMinPrice_excludesCheaper() {
        PageResponse<Movie> result = movieService.getMovies(null, null, 10000.0, null, 0, 100);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getMovies_filterByMaxPrice_excludesMoreExpensive() {
        PageResponse<Movie> result = movieService.getMovies(null, null, null, 0.0, 0, 100);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getMovies_pagination_returnsCorrectPage() {
        PageResponse<Movie> page0 = movieService.getMovies(null, null, null, null, 0, 10);
        PageResponse<Movie> page1 = movieService.getMovies(null, null, null, null, 1, 10);
        assertEquals(10, page0.getContent().size());
        assertEquals(10, page1.getContent().size());
        assertNotEquals(page0.getContent().getFirst().getMID(), page1.getContent().getFirst().getMID());
    }

    @Test
    void getMovies_pageOutOfRange_returnsEmptyContent() {
        PageResponse<Movie> result = movieService.getMovies(null, null, null, null, 999, 10);
        assertEquals(0, result.getContent().size());
        assertEquals(30, result.getTotalElements());
    }

    @Test
    void findById_existingMovie_returnsPresent() {
        Movie m = movie(5001);
        when(movieRepository.findById(5001)).thenReturn(Optional.of(m));

        Optional<Movie> result = movieService.findById(5001);
        assertTrue(result.isPresent());
        assertEquals(5001, result.get().getMID());
    }

    @Test
    void findById_unknownMid_returnsEmpty() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());
        Optional<Movie> result = movieService.findById(1);
        assertFalse(result.isPresent());
    }

    @Test
    void add_newMovie_returnsPresent() {
        Movie m = movie(5002);
        when(movieRepository.existsById(5002)).thenReturn(false);
        when(movieRepository.save(m)).thenReturn(m);

        Optional<Movie> result = movieService.add(m);
        assertTrue(result.isPresent());
        assertEquals(5002, result.get().getMID());
    }

    @Test
    void add_duplicate_returnsEmpty() {
        when(movieRepository.existsById(5003)).thenReturn(true);
        Optional<Movie> result = movieService.add(movie(5003));
        assertFalse(result.isPresent());
    }

    @Test
    void update_existingMovie_updatesFields() {
        Movie existing = movie(5004);
        Movie updated = movie(5004);
        updated.setName("New Name");
        when(movieRepository.findById(5004)).thenReturn(Optional.of(existing));
        when(movieRepository.save(existing)).thenReturn(existing);

        Optional<Movie> result = movieService.update(5004, updated);
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
    }

    @Test
    void update_unknownMid_returnsEmpty() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());
        Optional<Movie> result = movieService.update(1, movie(1));
        assertFalse(result.isPresent());
    }

    @Test
    void delete_existingMovie_returnsDeletedMovie() {
        Movie m = movie(5005);
        when(movieRepository.findById(5005)).thenReturn(Optional.of(m));

        Optional<Movie> result = movieService.delete(5005);
        assertTrue(result.isPresent());
        assertEquals(5005, result.get().getMID());
    }

    @Test
    void delete_existingMovie_deletesViaRepository() {
        Movie m = movie(5006);
        when(movieRepository.findById(5006)).thenReturn(Optional.of(m));

        movieService.delete(5006);
        verify(movieRepository).delete(m);
    }

    @Test
    void delete_unknownMid_returnsEmpty() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());
        Optional<Movie> result = movieService.delete(1);
        assertFalse(result.isPresent());
    }

    @Test
    void findByStudio_returnsMatchingMovies() {
        Movie m = movie(5007);
        m.setStudio(50);
        when(movieRepository.findAll()).thenReturn(List.of(m));

        List<Movie> result = movieService.findByStudio(50);
        assertTrue(result.stream().anyMatch(mv -> mv.getMID() == 5007));
    }

    @Test
    void findByStudio_noMatch_returnsEmptyList() {
        List<Movie> result = movieService.findByStudio(999);
        assertTrue(result.isEmpty());
    }
}
