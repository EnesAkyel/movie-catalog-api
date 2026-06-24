package simulations;

import scenarios.PostScenarios;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;

public class SpikeSimulation extends Simulation {
    {
        setUp(
                PostScenarios.browseMoviesFlow
                        .injectOpen(
                                constantUsersPerSec(5).during(Duration.ofSeconds(20)),
                                atOnceUsers(50),
                                constantUsersPerSec(5).during(Duration.ofSeconds(20)),
                                atOnceUsers(100),
                                constantUsersPerSec(5).during(Duration.ofSeconds(20))
                        )
        )
                .protocols(PostScenarios.HTTP_PROTOCOL)
                .assertions(
                        global().responseTime().max().lt(20000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
