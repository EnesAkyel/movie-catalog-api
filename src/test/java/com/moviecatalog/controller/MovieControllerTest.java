package com.moviecatalog.controller;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.moviecatalog.service.MovieService;
import com.moviecatalog.service.StudioService;
import com.moviecatalog.util.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
@DisplayName("MovieController Tests")
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private StudioService studioService;

    private Movie validMovie(int mid) {
        Movie m = new Movie();
        m.setMID(mid);
        m.setName("Test Movie");
        m.setGenre("Action");
        m.setPrice(9.99);
        m.setRating("PG-13");
        m.setStudio(1);
        return m;
    }

    private Studio validStudio(int sid) {
        return new Studio(sid, "Test Studio");
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private PageResponse<Movie> moviePage(Movie... movies) {
        return new PageResponse<>(List.of(movies), 0, 10, movies.length);
    }

    private PageResponse<Studio> studioPage(Studio... studios) {
        return new PageResponse<>(List.of(studios), 0, 10, studios.length);
    }

    @Test
    void getAllMovies_returns200WithPaginatedList() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(moviePage(validMovie(1001), validMovie(1002)));

        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void getAllMovies_pageParam_returnsCorrectPage() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), isNull(), isNull(), eq(1), eq(5)))
                .thenReturn(new PageResponse<>(List.of(), 1, 5, 0));

        mockMvc.perform(get("/api/v1/movies").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void getAllMovies_filterByGenre_returnsMatchingMovies() throws Exception {
        when(movieService.getMovies(eq("Action"), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(moviePage(validMovie(1003)));

        mockMvc.perform(get("/api/v1/movies").param("genre", "Action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getAllMovies_filterByRating_returnsMatchingMovies() throws Exception {
        when(movieService.getMovies(isNull(), eq("R"), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(moviePage(validMovie(1004)));

        mockMvc.perform(get("/api/v1/movies").param("rating", "R"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getAllMovies_filterByPriceRange_returnsResults() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), eq(0.0), eq(100.0), anyInt(), anyInt()))
                .thenReturn(moviePage(validMovie(1005)));

        mockMvc.perform(get("/api/v1/movies").param("minPrice", "0").param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getAllMovies_filterByPriceAboveAllMovies_returnsEmptyContent() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), eq(10000.0), isNull(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/v1/movies").param("minPrice", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getAllMovies_filterByPriceBelowAllMovies_returnsEmptyContent() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), isNull(), eq(0.0), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/v1/movies").param("maxPrice", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getAllMovies_pageOutOfRange_returnsEmptyContent() throws Exception {
        when(movieService.getMovies(isNull(), isNull(), isNull(), isNull(), eq(999), eq(10)))
                .thenReturn(new PageResponse<>(List.of(), 999, 10, 0));

        mockMvc.perform(get("/api/v1/movies").param("page", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getAllMovies_filterByGenreAndRating_returnsMatchingMovies() throws Exception {
        when(movieService.getMovies(eq("Action"), eq("PG-13"), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(moviePage(validMovie(1006)));

        mockMvc.perform(get("/api/v1/movies").param("genre", "Action").param("rating", "PG-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getMovieByMid_found_returns200() throws Exception {
        when(movieService.findById(1001)).thenReturn(Optional.of(validMovie(1001)));

        mockMvc.perform(get("/api/v1/movie/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mid", is(1001)));
    }

    @Test
    void getMovieByMid_notFound_returns404() throws Exception {
        when(movieService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/movie/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMovie_validInput_returns201() throws Exception {
        Movie movie = validMovie(9992);
        when(movieService.add(any())).thenReturn(Optional.of(movie));

        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mid", is(9992)));
    }

    @Test
    void addMovie_duplicate_returns409() throws Exception {
        when(movieService.add(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9993))))
                .andExpect(status().isConflict());
    }

    @Test
    void addMovie_midAtLowerBoundary1000_isAccepted() throws Exception {
        Movie movie = validMovie(1000);
        when(movieService.add(any())).thenReturn(Optional.of(movie));

        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isCreated());
    }

    @Test
    void addMovie_midAtUpperBoundary9999_returns201() throws Exception {
        Movie movie = validMovie(9999);
        when(movieService.add(any())).thenReturn(Optional.of(movie));

        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "MID={0} is out of range and should return 400")
    @ValueSource(ints = {999, 10000})
    void addMovie_midOutOfRange_returns400(int mid) throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(mid))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("mid")));
    }

    @ParameterizedTest(name = "genre=\"{0}\" is invalid and should return 400")
    @ValueSource(strings = {"Cartoon", "Sports", "action", ""})
    void addMovie_invalidGenre_returns400(String genre) throws Exception {
        Movie movie = validMovie(9994);
        movie.setGenre(genre);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "rating=\"{0}\" is invalid and should return 400")
    @ValueSource(strings = {"X", "NR", "PG13", ""})
    void addMovie_invalidRating_returns400(String rating) throws Exception {
        Movie movie = validMovie(9995);
        movie.setRating(rating);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMovie_missingName_returns400() throws Exception {
        Movie movie = validMovie(9996);
        movie.setName(null);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("name")));
    }

    @Test
    void addMovie_negativePrice_returns400() throws Exception {
        Movie movie = validMovie(9997);
        movie.setPrice(-1.0);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMovie_priceZero_returns400() throws Exception {
        Movie movie = validMovie(9998);
        movie.setPrice(0.0);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("price")));
    }

    @Test
    void editMovie_existing_returns200WithUpdatedName() throws Exception {
        Movie updated = validMovie(9985);
        updated.setName("Updated Title");
        when(movieService.update(eq(9985), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/v1/movie/9985")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Title")));
    }

    @Test
    void editMovie_notFound_returns404() throws Exception {
        when(movieService.update(eq(9984), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/movie/9984")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9984))))
                .andExpect(status().isNotFound());
    }

    @Test
    void editMovie_mismatchedMidInBody_usesPathMid() throws Exception {
        Movie returned = validMovie(9985);
        when(movieService.update(eq(9985), any())).thenReturn(Optional.of(returned));

        mockMvc.perform(put("/api/v1/movie/9985")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(1111))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mid", is(9985)));
    }

    @Test
    void deleteMovie_existing_returns200() throws Exception {
        when(movieService.delete(9986)).thenReturn(Optional.of(validMovie(9986)));

        mockMvc.perform(delete("/api/v1/movie/9986"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mid", is(9986)));
    }

    @Test
    void deleteMovie_notFound_returns404() throws Exception {
        when(movieService.delete(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/movie/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudios_returns200WithPaginatedList() throws Exception {
        when(studioService.getStudios(0, 10))
                .thenReturn(studioPage(validStudio(1), validStudio(2)));

        mockMvc.perform(get("/api/v1/studios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void getAllStudios_pageOutOfRange_returnsEmptyContent() throws Exception {
        when(studioService.getStudios(999, 10))
                .thenReturn(new PageResponse<>(List.of(), 999, 10, 0));

        mockMvc.perform(get("/api/v1/studios").param("page", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getMoviesBySid_found_returns200() throws Exception {
        Movie movie = validMovie(1007);
        movie.setStudio(95);
        when(movieService.findByStudio(95)).thenReturn(List.of(movie));

        mockMvc.perform(get("/api/v1/studios/95/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getMoviesBySid_notFound_returns404() throws Exception {
        when(movieService.findByStudio(200)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/studios/200/movies"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addStudio_validInput_returns201() throws Exception {
        Studio studio = validStudio(90);
        when(studioService.add(any())).thenReturn(Optional.of(studio));

        mockMvc.perform(post("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(studio)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sid", is(90)));
    }

    @Test
    void addStudio_sidAboveMax_returns400() throws Exception {
        Studio studio = new Studio(101, "Bad Studio");
        mockMvc.perform(post("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(studio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("sid")));
    }

    @Test
    void addStudio_duplicate_returns409() throws Exception {
        when(studioService.add(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validStudio(94))))
                .andExpect(status().isConflict());
    }

    @Test
    void editStudio_existing_returns200WithUpdatedName() throws Exception {
        Studio updated = new Studio(92, "Updated Name");
        when(studioService.update(eq(92), any())).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/v1/studio/92")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void editStudio_notFound_returns404() throws Exception {
        when(studioService.update(eq(100), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/studio/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validStudio(100))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudio_existing_returns200() throws Exception {
        when(studioService.delete(91)).thenReturn(Optional.of(validStudio(91)));

        mockMvc.perform(delete("/api/v1/studio/91"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sid", is(91)));
    }

    @Test
    void deleteStudio_notFound_returns404() throws Exception {
        when(studioService.delete(200)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/studio/200"))
                .andExpect(status().isNotFound());
    }
}
