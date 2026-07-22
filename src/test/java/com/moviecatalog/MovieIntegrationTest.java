package com.moviecatalog;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
@DisplayName("Movie Integration Tests")
class MovieIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int TEST_MID = 7001;
    private static final int TEST_SID = 55;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        given().contentType(ContentType.JSON)
                .body(json(new Studio(TEST_SID, "Test Studio")))
                .when().post("/api/v1/studio")
                .then().statusCode(anyOf(is(201), is(409)));
        when().delete("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(anyOf(is(200), is(404)));
    }

    @AfterEach
    void tearDown() {
        when().delete("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(anyOf(is(200), is(404)));
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Movie buildMovie(int mid, String name) {
        Movie m = new Movie();
        m.setMID(mid);
        m.setName(name);
        m.setGenre("Action");
        m.setPrice(14.99);
        m.setRating("PG-13");
        m.setStudio(TEST_SID);
        return m;
    }

    private void createTestMovie(String name) throws Exception {
        given().contentType(ContentType.JSON)
                .body(json(buildMovie(TEST_MID, name)))
                .when().post("/api/v1/movie")
                .then().statusCode(201);
    }

    @Test
    @DisplayName("POST /studio creates studio or returns 409 if already exists")
    void createStudio_returns201or409() throws Exception {
        given().contentType(ContentType.JSON)
                .body(json(new Studio(TEST_SID, "Test Studio")))
                .when().post("/api/v1/studio")
                .then().statusCode(anyOf(is(201), is(409)));
    }

    @Test
    @DisplayName("GET /studios lists all studios and includes seeded data")
    void getAllStudios_containsTestStudio() {
        when().get("/api/v1/studios")
                .then().statusCode(200)
                .body("content.sid", hasItem(TEST_SID))
                .body("totalElements", greaterThan(0));
    }

    @Test
    @DisplayName("POST /movie with valid body returns 201 with the created movie")
    void createMovie_returns201() throws Exception {
        given().contentType(ContentType.JSON)
                .body(json(buildMovie(TEST_MID, "Test Movie")))
                .when().post("/api/v1/movie")
                .then().statusCode(201)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Test Movie"))
                .body("genre", equalTo("Action"))
                .body("rating", equalTo("PG-13"));
    }

    @Test
    @DisplayName("POST /movie with duplicate MID returns 409")
    void createMovie_duplicate_returns409() throws Exception {
        createTestMovie("Test Movie");
        given().contentType(ContentType.JSON)
                .body(json(buildMovie(TEST_MID, "Duplicate")))
                .when().post("/api/v1/movie")
                .then().statusCode(409);
    }

    @Test
    @DisplayName("GET /movie/{mid} returns the movie after it is created")
    void getMovieByMid_returnsCreatedMovie() throws Exception {
        createTestMovie("Test Movie");
        when().get("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Test Movie"));
    }

    @Test
    @DisplayName("GET /movie/{mid} for a non-existent MID returns 404")
    void getMovieByMid_notFound_returns404() {
        when().get("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("PUT /movie/{mid} updates movie name and returns 200")
    void updateMovie_returns200WithNewName() throws Exception {
        createTestMovie("Test Movie");
        given().contentType(ContentType.JSON)
                .body(json(buildMovie(TEST_MID, "Updated Title")))
                .when().put("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Updated Title"));
    }

    @Test
    @DisplayName("PUT /movie with mismatched MID in body uses path MID")
    void updateMovie_mismatchedBodyMid_usesPathMid() throws Exception {
        createTestMovie("Original");
        Movie withDifferentMid = buildMovie(9999, "Patched");
        given().contentType(ContentType.JSON)
                .body(json(withDifferentMid))
                .when().put("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID));
    }

    @Test
    @DisplayName("GET /studios/{sid}/movies returns movies belonging to the studio")
    void getMoviesBySid_containsMovie() throws Exception {
        createTestMovie("Studio Movie");
        when().get("/api/v1/studios/{sid}/movies", TEST_SID)
                .then().statusCode(200)
                .body("mid", hasItem(TEST_MID));
    }

    @Test
    @DisplayName("GET /movies returns paginated results from seeded data")
    void getAllMovies_returnsResults() {
        when().get("/api/v1/movies?size=50")
                .then().statusCode(200)
                .body("totalElements", greaterThan(0));
    }

    @Test
    @DisplayName("DELETE /movie/{mid} returns the deleted movie with 200")
    void deleteMovie_returns200WithDeletedMovie() throws Exception {
        createTestMovie("Movie To Delete");
        when().delete("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID));
    }

    @Test
    @DisplayName("DELETE /movie/{mid} for non-existent MID returns 404")
    void deleteNonExistentMovie_returns404() {
        when().delete("/api/v1/movie/1")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("DELETE /studio/{sid} deletes the studio and returns 200")
    void deleteStudio_returns200() throws Exception {
        given().contentType(ContentType.JSON)
                .body(json(new Studio(56, "Temp Studio")))
                .when().post("/api/v1/studio");
        when().delete("/api/v1/studio/{sid}", 56)
                .then().statusCode(200)
                .body("sid", equalTo(56));
    }

    @Test
    @DisplayName("POST /movie with MID below 1000 returns 400 with field error on mid")
    void createMovieWithMidBelowRange_returns400WithFieldError() throws Exception {
        given().contentType(ContentType.JSON)
                .body(json(buildMovie(999, "Bad Movie")))
                .when().post("/api/v1/movie")
                .then().statusCode(400)
                .body("message", equalTo("Spring Validation Error"))
                .body("errors.field", hasItem("mid"));
    }

    @Test
    @DisplayName("POST /movie with invalid genre returns 400 with validation error")
    void createMovieWithInvalidGenre_returns400() throws Exception {
        Movie bad = buildMovie(7002, "Bad Genre");
        bad.setGenre("Cartoon");
        given().contentType(ContentType.JSON)
                .body(json(bad))
                .when().post("/api/v1/movie")
                .then().statusCode(400)
                .body("message", equalTo("Spring Validation Error"));
    }

    @Test
    @DisplayName("POST /movie with price 0 returns 400 — @Positive requires strictly > 0")
    void createMovieWithZeroPrice_returns400() throws Exception {
        Movie bad = buildMovie(7003, "Free Movie");
        bad.setPrice(0.0);
        given().contentType(ContentType.JSON)
                .body(json(bad))
                .when().post("/api/v1/movie")
                .then().statusCode(400)
                .body("message", equalTo("Spring Validation Error"))
                .body("errors.field", hasItem("price"));
    }

    @Test
    @DisplayName("GET /movies?size=0 returns 200 with empty content and full totalElements")
    void getAllMovies_sizeZero_returnsEmptyContentWithTotalElements() {
        when().get("/api/v1/movies?page=0&size=0")
                .then().statusCode(200)
                .body("content.size()", is(0))
                .body("totalElements", greaterThan(0));
    }

    @Test
    @DisplayName("GET /movies?genre=Action returns only Action genre movies")
    void getMovies_filterByGenre_returnsFilteredResults() {
        when().get("/api/v1/movies?genre=Action&size=50")
                .then().statusCode(200)
                .body("content.genre", everyItem(equalTo("Action")));
    }

    @Test
    @DisplayName("GET /movies?rating=G returns only G-rated movies")
    void getMovies_filterByRating_returnsFilteredResults() {
        when().get("/api/v1/movies?rating=G&size=50")
                .then().statusCode(200)
                .body("content.rating", everyItem(equalTo("G")));
    }

    @Test
    @DisplayName("GET /movies?minPrice=100 returns only movies priced >= 100")
    void getMovies_filterByMinPrice_returnsFilteredResults() {
        when().get("/api/v1/movies?minPrice=100&size=50")
                .then().statusCode(200)
                .body("content.size()", is(5));
    }

    @Test
    @DisplayName("GET /movies?maxPrice=10 returns only movies priced <= 10")
    void getMovies_filterByMaxPrice_returnsFilteredResults() {
        when().get("/api/v1/movies?maxPrice=10&size=50")
                .then().statusCode(200)
                .body("content.size()", is(2));
    }

    @Test
    @DisplayName("POST /movie with valid-range studioID not in DB succeeds — no FK check")
    void createMovie_withStudioNotInDb_returns201() throws Exception {
        Movie m = buildMovie(7004, "Orphan Movie");
        m.setStudio(77);
        given().contentType(ContentType.JSON)
                .body(json(m))
                .when().post("/api/v1/movie")
                .then().statusCode(201)
                .body("mid", equalTo(7004));
        when().delete("/api/v1/movie/7004").then().statusCode(anyOf(is(200), is(404)));
    }
}
