package com.example.moviecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void getAllMovies_returns200WithPaginatedList() throws Exception {
        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void getAllMovies_pageParam_returnsCorrectPage() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void getAllMovies_filterByGenre_returnsMatchingMovies() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("genre", "Action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    @Test
    void getAllMovies_filterByRating_returnsMatchingMovies() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("rating", "R"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    @Test
    void getAllMovies_filterByPriceRange_returnsResults() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("minPrice", "0").param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void getAllMovies_filterByGenreAndRating_returnsMatchingMovies() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("genre", "Action").param("rating", "PG-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(List.class)));
    }

    @Test
    void getMovieByMid_found_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9991))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/movie/9991"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mid", is(9991)));
    }

    @Test
    void getMovieByMid_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/movie/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudios_returns200WithPaginatedList() throws Exception {
        mockMvc.perform(get("/api/v1/studios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));
    }

    @Test
    void getMoviesBySid_found_returns200() throws Exception {
        Studio studio = new Studio(95, "Test Studio");
        mockMvc.perform(post("/api/v1/studio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(studio)));

        Movie movie = validMovie(9990);
        movie.setStudio(95);
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(movie)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/studios/95/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void getMoviesBySid_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/studios/200/movies"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMovie_validInput_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9992))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mid", is(9992)));
    }

    @Test
    void addMovie_duplicate_returns302() throws Exception {
        String body = json(validMovie(9993));
        mockMvc.perform(post("/api/v1/movie").contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(post("/api/v1/movie").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isFound());
    }

    @Test
    void addMovie_midAtLowerBoundary1000_isAccepted() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(1000))))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void addMovie_midAtUpperBoundary9999_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9999))))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "MID={0} is out of range and should return 400")
    @ValueSource(ints = {999, 10000})
    void addMovie_midOutOfRange_returns400(int mid) throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(mid))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("MID")));
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
    void editMovie_existing_returns200WithUpdatedName() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9985))))
                .andExpect(status().isCreated());

        Movie updated = validMovie(9985);
        updated.setName("Updated Title");
        mockMvc.perform(put("/api/v1/movie/9985")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Title")));
    }

    @Test
    void editMovie_notFound_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validMovie(9984))));
        mockMvc.perform(delete("/api/v1/movie/9984"));

        mockMvc.perform(put("/api/v1/movie/9984")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9984))))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteMovie_existing_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMovie(9986))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/movie/9986"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mid", is(9986)));
    }

    @Test
    void deleteMovie_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/movie/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addStudio_validInput_returns201() throws Exception {
        Studio studio = new Studio(90, "New Studio");
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
                .andExpect(jsonPath("$.errors[0].field", is("SID")));
    }

    @Test
    void deleteStudio_existing_returns200() throws Exception {
        Studio studio = new Studio(91, "Studio To Delete");
        mockMvc.perform(post("/api/v1/studio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(studio)));

        mockMvc.perform(delete("/api/v1/studio/91"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sid", is(91)));
    }

    @Test
    void deleteStudio_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/studio/200"))
                .andExpect(status().isNotFound());
    }
}
