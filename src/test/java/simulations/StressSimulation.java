package simulations;

import scenarios.PostScenarios;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;

public class StressSimulation extends Simulation {
    {
        setUp(
                PostScenarios.browseMoviesFlow
                        .injectOpen(
                                rampUsers(5).during(Duration.ofSeconds(10)),
                                stressPeakUsers(10).during(Duration.ofSeconds(20)),
                                stressPeakUsers(20).during(Duration.ofSeconds(20)),
                                stressPeakUsers(30).during(Duration.ofSeconds(20)),
                                stressPeakUsers(40).during(Duration.ofSeconds(20)),
                                stressPeakUsers(50).during(Duration.ofSeconds(20)),
                                constantUsersPerSec(50).during(Duration.ofSeconds(30)),
                                rampUsersPerSec(50).to(0).during(Duration.ofSeconds(10))
                        )
        )
                .protocols(PostScenarios.HTTP_PROTOCOL)
                .assertions(
                        global().responseTime().max().lt(100000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
