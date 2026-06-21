package com.moviecatalog;

import com.moviecatalog.model.Movie;
import com.moviecatalog.model.Studio;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MovieIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int TEST_MID = 7001;
    private static final int TEST_SID = 55;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
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

    @Test
    @Order(1)
    void createStudio_returns201or409() throws Exception {
        given().contentType(ContentType.JSON).body(json(new Studio(TEST_SID, "Test Studio")))
                .when().post("/api/v1/studio")
                .then().statusCode(anyOf(is(201), is(409)));
    }

    @Test
    @Order(2)
    void getAllStudios_containsTestStudio() {
        when().get("/api/v1/studios")
                .then().statusCode(200)
                .body("content.sid", hasItem(TEST_SID))
                .body("totalElements", greaterThan(0));
    }

    @Test
    @Order(3)
    void createMovie_returns201() throws Exception {
        given().contentType(ContentType.JSON).body(json(buildMovie(TEST_MID, "Test Movie")))
                .when().post("/api/v1/movie")
                .then().statusCode(201)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Test Movie"))
                .body("genre", equalTo("Action"))
                .body("rating", equalTo("PG-13"));
    }

    @Test
    @Order(4)
    void getMovieByMid_returnsCreatedMovie() {
        when().get("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Test Movie"));
    }

    @Test
    @Order(5)
    void updateMovie_returns200WithNewName() throws Exception {
        given().contentType(ContentType.JSON).body(json(buildMovie(TEST_MID, "Updated Title")))
                .when().put("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID))
                .body("name", equalTo("Updated Title"));
    }

    @Test
    @Order(6)
    void getMoviesBySid_containsUpdatedMovie() {
        when().get("/api/v1/studios/{sid}/movies", TEST_SID)
                .then().statusCode(200)
                .body("mid", hasItem(TEST_MID))
                .body("name", hasItem("Updated Title"));
    }

    @Test
    @Order(7)
    void getAllMovies_containsUpdatedMovie() {
        when().get("/api/v1/movies?size=50")
                .then().statusCode(200)
                .body("content.mid", hasItem(TEST_MID))
                .body("totalElements", greaterThan(0));
    }

    @Test
    @Order(8)
    void deleteMovie_returns200WithDeletedMovie() {
        when().delete("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(200)
                .body("mid", equalTo(TEST_MID));
    }

    @Test
    @Order(9)
    void getDeletedMovie_returns404() {
        when().get("/api/v1/movie/{mid}", TEST_MID)
                .then().statusCode(404);
    }

    @Test
    @Order(10)
    void getMoviesByPriceRange_returns200WithResults() {
        when().get("/api/v1/movies?minPrice=0&maxPrice=100")
                .then().statusCode(200)
                .body("content.size()", greaterThan(0));
    }

    @Test
    @Order(11)
    void createMovieWithMidBelowRange_returns400WithFieldError() throws Exception {
        Movie bad = buildMovie(999, "Bad Movie");
        given().contentType(ContentType.JSON).body(json(bad))
                .when().post("/api/v1/movie")
                .then().statusCode(400)
                .body("message", equalTo("Spring Validation Error"))
                .body("errors.field", hasItem("mid"));
    }

    @Test
    @Order(12)
    void createMovieWithInvalidGenre_returns400() throws Exception {
        Movie bad = buildMovie(7002, "Bad Genre");
        bad.setGenre("Cartoon");
        given().contentType(ContentType.JSON).body(json(bad))
                .when().post("/api/v1/movie")
                .then().statusCode(400)
                .body("message", equalTo("Spring Validation Error"));
    }

    @Test
    @Order(13)
    void deleteNonExistentMovie_returns404() {
        when().delete("/api/v1/movie/1")
                .then().statusCode(404);
    }

    @Test
    @Order(14)
    void deleteStudio_returns200() throws Exception {
        given().contentType(ContentType.JSON).body(json(new Studio(56, "Temp Studio")))
                .when().post("/api/v1/studio");

        when().delete("/api/v1/studio/{sid}", 56)
                .then().statusCode(200)
                .body("sid", equalTo(56));
    }
}
