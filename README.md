# Movie Catalog API

[![CI](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml)

A RESTful Spring Boot API for managing a catalog of movies and studios, built as an SDET portfolio project demonstrating layered architecture, validation, and a multi-layer test strategy.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.5 |
| Persistence | PostgreSQL 16, Spring Data JPA, Hibernate 6 |
| Test Database | H2 (in-memory, auto-configured for all test contexts) |
| Validation | Jakarta Bean Validation |
| API Docs | springdoc-openapi 2.8 (Swagger UI) |
| Unit/Integration Tests | JUnit 5, Mockito, MockMvc, RestAssured |
| Coverage | JaCoCo |
| Containerisation | Docker, Docker Compose |
| API Test Suite | TypeScript, Jest, Axios, AJV ([api-testing-ts](https://github.com/EnesAkyel/api-testing-ts)) |
| Load Testing | Gatling (Java API), Maven Gatling Plugin |
| CI | GitHub Actions |

---

## Project Structure

```
src/main/java/com/moviecatalog/
├── config/          # CORS, OpenAPI, and startup configuration
├── controller/      # MovieController — HTTP mapping only, delegates to services
├── exception/       # GlobalExceptionHandler, validation error response models
├── model/           # Movie, Studio — validated domain models
├── service/         # MovieService, StudioService — business logic
└── util/            # PageResponse — generic paginated response wrapper

src/test/java/com/moviecatalog/   # JUnit tests (unit, controller, integration)

src/gatling/java/
├── config/                   # Config.java — base URL, user counts, thresholds
├── scenarios/                # PostScenarios.java — reusable Gatling scenarios
└── simulations/              # BasicSimulation, LoadSimulation, StressSimulation,
                              # SoakSimulation, SpikeSimulation
```

---

## API Overview

Base path: `/api/v1`

| Method | Path | Description |
|---|---|---|
| `GET` | `/movies` | List movies — filter by `genre`, `rating`, `minPrice`, `maxPrice`; paginated |
| `GET` | `/movie/{mid}` | Get movie by ID |
| `POST` | `/movie` | Create a movie |
| `PUT` | `/movie/{mid}` | Update a movie |
| `DELETE` | `/movie/{mid}` | Delete a movie |
| `GET` | `/studios` | List studios (paginated) |
| `GET` | `/studios/{sid}/movies` | Get all movies for a studio |
| `POST` | `/studio` | Create a studio |
| `PUT` | `/studio/{sid}` | Update a studio |
| `DELETE` | `/studio/{sid}` | Delete a studio |

Interactive docs available via Swagger UI after starting the app:
`http://localhost:8080/swagger-ui/index.html`

### Validation rules

| Field | Rule |
|---|---|
| `mid` | 4-digit integer (1000–9999) |
| `genre` | One of: Action, Romance, Comedy, Horror, Drama, Thriller, Sci-Fi, Fantasy, Mystery, Adventure |
| `rating` | One of: G, PG, PG-13, R, NC-17 |
| `price` | Positive (> 0.00) |
| `sid` | Integer 1–100 |

### Validation error response (HTTP 400)

```json
{
  "message": "Spring Validation Error",
  "errors": [
    { "field": "mid", "message": "Movie ID must be a 4 digit number" }
  ]
}
```

---

## Running Locally

### With Docker (recommended)

**Prerequisites:** Docker Desktop

```bash
# Build images and start API + PostgreSQL
docker compose up --build

# Stop containers (data persists in the pgdata volume)
docker compose down

# Stop and remove all data
docker compose down -v
```

The API is available at `http://localhost:8080`. On first startup, `DataInitializer` seeds 5 studios and 30 movies automatically. The seed is idempotent. Restarting the containers will not duplicate data.

### Without Docker

**Prerequisites:** Java 21, Maven (or use the included `./mvnw` wrapper), a running PostgreSQL instance

Set the following environment variables (defaults shown):

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=moviecatalog
DB_USER=postgres
DB_PASSWORD=postgres
```

```bash
# Start the application
./mvnw spring-boot:run

# Run all tests (uses H2 — no PostgreSQL needed)
./mvnw test

# Run tests + generate JaCoCo coverage report
./mvnw verify
# Report: target/site/jacoco/index.html
```

---

## Test Strategy

The project uses two complementary test layers.

### Controller Tests — `MovieControllerTest` (35 tests)
`@WebMvcTest` with MockMvc. Spins up only the web layer (no full application context) for fast, focused tests. Covers:
- Happy path for every endpoint
- Validation rejection (invalid field values, missing required fields)
- Boundary conditions via `@ParameterizedTest` (ID range edges, invalid enum values)
- Correct HTTP status codes per scenario (201, 200, 302, 404, 409, 422)

### Service Tests — `MovieServiceTest` / `StudioServiceTest` (29 tests)
`@ExtendWith(MockitoExtension.class)` with mocked repositories. Pure unit tests — no Spring context, no database. Covers all CRUD operations and filtering logic.

### Integration Tests — `MovieIntegrationTest` (15 tests)
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with RestAssured against a live embedded server backed by H2. Covers an ordered CRUD lifecycle:
1. Create studio → create movie → GET by ID → PUT update → GET by studio → GET all → DELETE → verify 404

All test contexts use H2 via `src/test/resources/application.properties` — no PostgreSQL or Docker required to run the test suite.

### Load Tests — Gatling
Five simulations covering different load profiles, all built on shared scenarios in `PostScenarios.java`. Requires the API to be running via Docker Compose.

| Simulation | Profile |
|------------|---------|
| `BasicSimulation` | 1 user per scenario — sanity check for all endpoints |
| `LoadSimulation` | Ramp 5 users → sustain 10/s for 60s → ramp down |
| `StressSimulation` | Progressive spikes: 10 → 20 → 30 → 40 → 50 users |
| `SoakSimulation` | 10 users sustained for 5 minutes — detects memory leaks |
| `SpikeSimulation` | Baseline 5/s → sudden burst of 50 → baseline → burst of 100 |

```bash
# Run BasicSimulation (after docker compose up)
./mvnw gatling:test -Dgatling.simulationClass=simulations.BasicSimulation
# Reports: target/gatling/
```

### API Tests — [api-testing-ts](https://github.com/EnesAkyel/api-testing-ts)
A separate TypeScript framework that targets the running API over HTTP. Requires the API to be started via Docker Compose first.

| Suite | What it covers |
|-------|----------------|
| Smoke | Endpoints return 200, seeded data is accessible |
| Contract | AJV schema validation — response shapes match declared types |
| Integration | Full CRUD lifecycle, error cases, response time assertions |
| Regression | Collection integrity and individual retrieval against seeded data (MIDs 1001–1030) |

```bash
# From the api-testing-ts repo (after docker compose up in this repo)
npm run test:smoke
npm run test:contract
npm run test:integration
npm run test:regression:local
```

---

## CI Pipeline

Every push to `main` and every pull request triggers the GitHub Actions workflow (`.github/workflows/ci.yml`):

```
build-and-test ┐
api-tests       ├─ (parallel)
gatling ────────┘  (main only)
```

**`build-and-test`**
1. Check out code
2. Set up Java 21 (Temurin)
3. Run `./mvnw verify` — unit, controller, and integration tests + JaCoCo
4. Run `./mvnw sonar:sonar` — SonarCloud analysis (main only)
5. Upload JaCoCo report as a build artifact (retained 14 days)
6. Validate `docker-compose.yml` syntax

**`api-tests`** (runs after `build-and-test` passes)
1. Start the API via `docker compose up --build`
2. Wait for `GET /api/v1/movies` to return 200
3. Check out [api-testing-ts](https://github.com/EnesAkyel/api-testing-ts)
4. Run smoke → contract → integration test suites
5. Upload HTML + JUnit reports as artifacts (retained 14 days)
6. Publish JUnit results to the PR checks panel via `dorny/test-reporter`

**`gatling`** (runs after `api-tests` passes, main only)
1. Start the API via `docker compose up --build`
2. Wait for API to be ready
3. Run `BasicSimulation` via `./mvnw gatling:test`
4. Upload Gatling HTML report as artifact (retained 30 days)

---

## What's Next

- **Java test quality:** integration test isolation, missing edge cases, JaCoCo coverage gate
- **TypeScript test gaps:** validation error body assertions, minPrice filter, studio-not-found edge case
