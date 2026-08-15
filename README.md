# Movie Catalog API

[![CI](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml)

A RESTful Spring Boot API for managing a catalog of movies and studios, built as an SDET portfolio project demonstrating layered architecture, validation, and a multi-layer test strategy.

---

## Tech Stack

| Layer                  | Technology                                                                                                                                                                    |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Runtime                | Java 25, Spring Boot 4.1                                                                                                                                                      |
| Auth                   | Spring Security 7.1, JWT (jjwt) - all endpoints except `/api/v1/auth/login` require a Bearer token                                                                            |
| Persistence            | PostgreSQL 16, Spring Data JPA, Hibernate 7                                                                                                                                   |
| Test Database          | PostgreSQL 16 via Testcontainers (same engine as production)                                                                                                                  |
| Schema Migrations      | Flyway (V1 schema, V2 seed data)                                                                                                                                              |
| Validation             | Jakarta Bean Validation                                                                                                                                                       |
| API Docs               | springdoc-openapi 3.0.3 (Swagger UI)                                                                                                                                          |
| Unit/Integration Tests | JUnit 5, Mockito, MockMvc, RestAssured                                                                                                                                        |
| Coverage               | JaCoCo                                                                                                                                                                        |
| Containerisation       | Docker, Docker Compose                                                                                                                                                        |
| API Test Suite         | TypeScript, Jest, Axios, AJV ([api-testing-ts](https://github.com/EnesAkyel/api-testing-ts))                                                                                  |
| Contract Testing       | Pact ([pact-contract-tests](https://github.com/EnesAkyel/pact-contract-tests))                                                                                                |
| Load Testing           | k6 ([k6-performance-tests](https://github.com/EnesAkyel/k6-performance-tests)), Gatling ([gatling-performance-tests](https://github.com/EnesAkyel/gatling-performance-tests)) |
| CI                     | GitHub Actions                                                                                                                                                                |

---

## Project Structure

```
src/main/java/com/moviecatalog/
├── auth/            # AuthController, JwtUtil, JwtFilter - login + token issuing/validation
├── config/          # CORS, OpenAPI, Spring Security, and startup configuration
├── controller/      # MovieController - HTTP mapping only, delegates to services
├── dto/             # MovieRequest, StudioRequest - request-body models
├── exception/       # GlobalExceptionHandler, validation error response models
├── model/           # Movie, Studio - validated domain models
├── repository/      # MovieRepository, StudioRepository - Spring Data JPA
├── service/         # MovieService, StudioService - business logic
└── util/            # PageResponse - generic paginated response wrapper

src/test/java/com/moviecatalog/   # JUnit tests, mirroring the main package structure
```

Load testing (Gatling, k6) and API/contract testing (api-testing-ts, pact-contract-tests) live in their own repos and are pulled into this repo's CI as separate jobs - see [CI Pipeline](#ci-pipeline).

---

## API Overview

Base path: `/api/v1`

All endpoints below require `Authorization: Bearer <token>` except `/auth/login`. Get a token by posting credentials to `/auth/login`; tokens are issued by `JwtUtil` and validated on every request by `JwtFilter`.

| Method   | Path                    | Description                                                                  |
|----------|-------------------------|------------------------------------------------------------------------------|
| `POST`   | `/auth/login`           | Exchange `AUTH_USERNAME`/`AUTH_PASSWORD` credentials for a JWT               |
| `GET`    | `/movies`               | List movies - filter by `genre`, `rating`, `minPrice`, `maxPrice`; paginated |
| `GET`    | `/movie/{mid}`          | Get movie by ID                                                              |
| `POST`   | `/movie`                | Create a movie                                                               |
| `PUT`    | `/movie/{mid}`          | Update a movie                                                               |
| `DELETE` | `/movie/{mid}`          | Delete a movie                                                               |
| `GET`    | `/studios`              | List studios (paginated)                                                     |
| `GET`    | `/studios/{sid}/movies` | Get all movies for a studio                                                  |
| `POST`   | `/studio`               | Create a studio                                                              |
| `PUT`    | `/studio/{sid}`         | Update a studio                                                              |
| `DELETE` | `/studio/{sid}`         | Delete a studio                                                              |

Interactive docs available via Swagger UI after starting the app:
`http://localhost:8080/swagger-ui/index.html`

### Validation rules

| Field    | Rule                                                                                          |
|----------|-----------------------------------------------------------------------------------------------|
| `mid`    | 4-digit integer (1000–9999)                                                                   |
| `genre`  | One of: Action, Romance, Comedy, Horror, Drama, Thriller, Sci-Fi, Fantasy, Mystery, Adventure |
| `rating` | One of: G, PG, PG-13, R, NC-17                                                                |
| `price`  | Positive (> 0.00)                                                                             |
| `sid`    | Integer 1–100                                                                                 |

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

Set `AUTH_USERNAME`, `AUTH_PASSWORD`, and `JWT_SECRET` in a `.env` file (or your shell) before starting - `docker-compose.yml` passes them through to the container, and the app won't issue tokens without them.

```bash
# Build images and start API + PostgreSQL
docker compose up --build

# Stop containers (data persists in the pgdata volume)
docker compose down

# Stop and remove all data
docker compose down -v
```

The API is available at `http://localhost:8080`. On first startup, Flyway runs `V1__init_schema.sql` (creates tables) and `V2__seed_data.sql` (seeds 5 studios and 30 movies). Flyway tracks applied migrations - restarting the containers will not re-run them.

Log in to get a token, then call any other endpoint with it:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$AUTH_USERNAME\",\"password\":\"$AUTH_PASSWORD\"}" | jq -r .token)

curl http://localhost:8080/api/v1/movies -H "Authorization: Bearer $TOKEN"
```

### Without Docker

**Prerequisites:** Java 25, a running PostgreSQL instance, Docker (required for Testcontainers). Maven 3.9.16 is required - the included `./mvnw` wrapper downloads it automatically if not present.

Set the following environment variables (DB defaults shown; `AUTH_USERNAME`/`AUTH_PASSWORD`/`JWT_SECRET` have no defaults and must be set):

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=moviecatalog
DB_USER=postgres
DB_PASSWORD=postgres
AUTH_USERNAME=
AUTH_PASSWORD=
JWT_SECRET=
```

```bash
# Start the application
./mvnw spring-boot:run

# Run all tests (integration tests use Testcontainers - Docker must be running)
./mvnw test

# Run tests + generate JaCoCo coverage report
./mvnw verify
# Report: target/site/jacoco/index.html
```

---

## Test Strategy

The project uses three in-repo JUnit layers, plus separate repos for HTTP-level, contract, and load testing.

### Controller Tests - `MovieControllerTest` (34 tests)
`@ExtendWith(MockitoExtension.class)` with standalone `MockMvcBuilders` (no Spring context). Spring Boot 4.x removed `@WebMvcTest`, so the controller and its dependencies are wired manually. Covers:
- Happy path for every endpoint
- Validation rejection (invalid field values, missing required fields)
- Boundary conditions via `@ParameterizedTest` (ID range edges, invalid enum values)
- Correct HTTP status codes per scenario (201, 200, 404, 409, 400)

### Service Tests - `MovieServiceTest` / `StudioServiceTest` (26 tests)
`@ExtendWith(MockitoExtension.class)` with mocked repositories. Pure unit tests - no Spring context, no database. Covers all CRUD operations and filtering logic.

### Integration Tests - `MovieIntegrationTest` (22 tests)
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with RestAssured 6.x against a live embedded server backed by a real PostgreSQL 16 container (Testcontainers). Each test is isolated via `@BeforeEach`/`@AfterEach` - no shared ordering. Covers full CRUD, edge cases (price=0, size=0, mismatched MID, orphan studioID), and validation errors.

### Auth Tests - `AuthControllerTest`, `JwtUtilTest`, `JwtFilterTest`, `SecurityConfigTest`
Cover login happy/sad paths, token generation/parsing/expiry, filter chain behavior for missing/invalid/expired tokens, and that `/api/v1/auth/login` is the only `permitAll()` route.

### API Tests - [api-testing-ts](https://github.com/EnesAkyel/api-testing-ts)
A separate TypeScript framework that targets the running API over HTTP, authenticating via `/auth/login` before each suite. Requires the API to be started via Docker Compose first.

| Suite       | What it covers                                                                     |
|-------------|------------------------------------------------------------------------------------|
| Smoke       | Endpoints return 200, seeded data is accessible                                    |
| Contract    | AJV schema validation - response shapes match declared types                       |
| Integration | Full CRUD lifecycle, error cases, response time assertions                         |
| Regression  | Collection integrity and individual retrieval against seeded data (MIDs 1001–1030) |

```bash
# From the api-testing-ts repo (after docker compose up in this repo)
npm run test:smoke
npm run test:contract
npm run test:integration
npm run test:regression:local
```

### Contract Tests - [pact-contract-tests](https://github.com/EnesAkyel/pact-contract-tests)
Consumer-driven contract tests (Pact). Consumer specs generate a pact file against a mock server; provider verification replays it against the real, running API using a JWT obtained from `/auth/login`.

### Load Tests - [k6-performance-tests](https://github.com/EnesAkyel/k6-performance-tests) and [gatling-performance-tests](https://github.com/EnesAkyel/gatling-performance-tests)
Both target the live API (started via Docker Compose) and authenticate first. k6 covers a smoke profile in CI; Gatling covers `BasicSimulation` in CI, with additional Load/Stress/Soak/Spike simulations available for local runs.

---

## CI Pipeline

Every push to `main` and every pull request triggers the GitHub Actions workflow (`.github/workflows/ci.yml`):

```
build ─┬─ test ─┬─ sonar (needs test; runs on main push and on PRs)
       │        │
       ├─ api-tests
       ├─ pact
       ├─ k6       (main only)
       └─ gatling  (main only)
```

**`build`**
1. Check out code
2. Set up Java 25 (Temurin)
3. `./mvnw package -DskipTests` - compiles and packages the JAR
4. Upload the JAR as a build artifact (retained 1 day) for downstream jobs
5. Validate `docker-compose.yml` syntax

**`test`** (needs `build`)
1. Run `./mvnw verify` - unit, controller, and integration tests + JaCoCo
2. Upload JaCoCo report and Surefire reports as build artifacts
3. Publish JUnit results to the PR checks panel via `dorny/test-reporter`

**`sonar`** (needs `test`)
1. Download the JaCoCo/Surefire reports from the `test` job
2. Run `./mvnw package -DskipTests sonar:sonar` - SonarCloud analysis

**`api-tests`**, **`pact`**, **`k6`** (main only), **`gatling`** (main only) - each needs `build` and runs independently (not chained off `test`):
1. Download the pre-built JAR and start the API via the shared `./.github/actions/start-api` composite action (builds the CI Docker image, starts it with `AUTH_USERNAME`/`AUTH_PASSWORD`/`JWT_SECRET` from GitHub Secrets, waits for `GET /v3/api-docs` to respond)
2. Check out the corresponding sibling repo (`api-testing-ts`, `pact-contract-tests`, `k6-performance-tests`, or `gatling-performance-tests`)
3. Run that repo's tests against the running API, authenticating with the same secrets
4. Upload reports as artifacts (`api-tests`/`k6`/`gatling` also publish or retain HTML/JUnit output; `pact` currently does not upload an artifact)

---

## Checking for Dependency Upgrades

```bash
# List dependencies with newer versions available
./mvnw versions:display-dependency-updates

# List available plugin updates
./mvnw versions:display-plugin-updates

# List available property-based version updates (e.g. <spring-security.version>)
./mvnw versions:display-property-updates
```

`spring-boot-starter-parent` pins most Spring dependency versions via its BOM, so bumping Spring itself usually means bumping the parent version in `pom.xml` rather than individual `spring-boot-starter-*` artifacts.

