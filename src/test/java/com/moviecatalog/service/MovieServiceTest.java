package com.moviecatalog.service;

import com.moviecatalog.model.Movie;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
        movieService = new MovieService(movieRepository);
        lenient().when(movieRepository.findAll(ArgumentMatchers.<Specification<Movie>>any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
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
    void getMovies_returnsPagedResults() {
        List<Movie> content = List.of(movie(1001), movie(1002));
        when(movieRepository.findAll(ArgumentMatchers.<Specification<Movie>>any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 2));

        PageResponse<Movie> result = movieService.getMovies(null, null, null, null, 0, 10);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
    }

    @Test
    void getMovies_pageOutOfRange_returnsEmptyContent() {
        when(movieRepository.findAll(ArgumentMatchers.<Specification<Movie>>any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(999, 10), 30));

        PageResponse<Movie> result = movieService.getMovies(null, null, null, null, 999, 10);
        assertEquals(0, result.getContent().size());
        assertEquals(30, result.getTotalElements());
    }

    @Test
    void getMovies_sizeZero_returnsEmptyContentWithTotal() {
        when(movieRepository.count(ArgumentMatchers.<Specification<Movie>>any())).thenReturn(30L);

        PageResponse<Movie> result = movieService.getMovies(null, null, null, null, 0, 0);
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
        assertFalse(movieService.findById(1).isPresent());
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
        assertFalse(movieService.add(movie(5003)).isPresent());
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
        assertFalse(movieService.update(1, movie(1)).isPresent());
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
        assertFalse(movieService.delete(1).isPresent());
    }

    @Test
    void findByStudio_returnsMatchingMovies() {
        Movie m = movie(5007);
        m.setStudio(50);
        when(movieRepository.findByStudioID(50)).thenReturn(List.of(m));

        List<Movie> result = movieService.findByStudio(50);
        assertTrue(result.stream().anyMatch(mv -> mv.getMID() == 5007));
    }

    @Test
    void findByStudio_noMatch_returnsEmptyList() {
        when(movieRepository.findByStudioID(anyInt())).thenReturn(List.of());
        assertTrue(movieService.findByStudio(999).isEmpty());
    }
}
