package simulations;

import config.Config;
import scenarios.PostScenarios;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

public class SoakSimulation extends Simulation {
    {
        setUp(
                PostScenarios.browseMoviesFlow
                        .injectOpen(
                                rampUsers(10).during(Duration.ofSeconds(30)),
                                constantUsersPerSec(10).during(Duration.ofMinutes(5)),
                                rampUsersPerSec(10).to(0).during(Duration.ofSeconds(30))
                        )
        )
                .protocols(PostScenarios.HTTP_PROTOCOL)
                .assertions(
                        global().responseTime().max().lt((int) Config.MAX_RESPONSE_TIME_MS),
                        global().responseTime().percentile(95).lt((int) Config.PERCENTILE_95_MS),
                        global().successfulRequests().percent().gt(100 - Config.MAX_ERROR_RATE_PERCENT)
                );
    }
}
