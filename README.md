# Movie Catalog API

[![CI](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml)

A RESTful Spring Boot API for managing a catalog of movies and studios.

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
| CI | GitHub Actions |

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

Full interactive docs available via Swagger UI after starting the app:
`http://localhost:8080/swagger-ui/index.html`

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

---

## CI Pipeline

Every push to `main` and every pull request triggers the GitHub Actions workflow (`.github/workflows/ci.yml`):

1. Check out code
2. Set up Java 21 (Temurin)
3. Run `./mvnw verify sonar:sonar` (tests + JaCoCo + SonarCloud analysis)
4. Upload JaCoCo report as a build artifact (retained 14 days)
5. Validate `docker-compose.yml` syntax

---

## What's Next

- **Phase 3 — Test Quality:** integration test isolation, missing edge cases, JaCoCo coverage gate
- **Phase 4 — Load Testing:** Gatling simulations for baseline performance
- **Phase 5 — Contract Testing:** Pact consumer/provider tests