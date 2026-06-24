package scenarios;

import config.Config;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class PostScenarios {

    public static final HttpProtocolBuilder HTTP_PROTOCOL = http
            .baseUrl(Config.BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling Performance Tests");

    public static final ScenarioBuilder getAllMovies = scenario("Get All Movies")
            .exec(
                    http("GET /api/v1/movies")
                            .get("/api/v1/movies")
                            .check(status().is(200))
                            .check(jsonPath("$.content[0].mid").exists())
                            .check(jsonPath("$.totalElements").ofInt().gt(0))
            );

    public static final ScenarioBuilder getMovieById = scenario("Get Movie By ID")
            .exec(
                    http("GET /api/v1/movie/1001")
                            .get("/api/v1/movie/1001")
                            .check(status().is(200))
                            .check(jsonPath("$.mid").is("1001"))
                            .check(jsonPath("$.name").exists())
                            .check(jsonPath("$.genre").exists())
            );

    public static final ScenarioBuilder createMovie = scenario("Create Movie")
            .exec(
                    http("POST /api/v1/movie")
                            .post("/api/v1/movie")
                            .body(StringBody(
                                    """
                                    {
                                        "mid": 8001,
                                        "name": "Gatling Test Movie",
                                        "genre": "Action",
                                        "price": 9.99,
                                        "rating": "PG-13",
                                        "studio": 1
                                    }
                                    """
                            ))
                            .check(status().in(201, 409))
                            .check(jsonPath("$.mid").exists())
            );

    public static final ScenarioBuilder updateMovie = scenario("Update Movie")
            .exec(
                    http("PUT /api/v1/movie/1001")
                            .put("/api/v1/movie/1001")
                            .body(StringBody(
                                    """
                                    {
                                        "mid": 1001,
                                        "name": "Updated Gatling Movie",
                                        "genre": "Action",
                                        "price": 12.99,
                                        "rating": "PG-13",
                                        "studio": 1
                                    }
                                    """
                            ))
                            .check(status().is(200))
                            .check(jsonPath("$.mid").is("1001"))
                            .check(jsonPath("$.name").is("Updated Gatling Movie"))
            );

    public static final ScenarioBuilder browseMoviesFlow = scenario("Browse Movies Flow")
            .exec(
                    http("GET /api/v1/movies")
                            .get("/api/v1/movies")
                            .check(status().is(200))
            )
            .pause(1)
            .exec(
                    http("GET /api/v1/movie/1001")
                            .get("/api/v1/movie/1001")
                            .check(status().is(200))
                            .check(jsonPath("$.mid").is("1001"))
            )
            .pause(1)
            .exec(
                    http("GET /api/v1/studios/1/movies")
                            .get("/api/v1/studios/1/movies")
                            .check(status().is(200))
                            .check(jsonPath("$[0].studio").is("1"))
            );
}
