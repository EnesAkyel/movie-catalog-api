# Movie Catalog API

[![CI](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml)

A RESTful Spring Boot API for managing a catalog of movies and studios, built as an SDET portfolio project demonstrating layered architecture, validation, and a multi-layer test strategy.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.5 |
| Validation | Jakarta Bean Validation |
| Persistence | In-memory (List) — PostgreSQL migration planned |
| API Docs | springdoc-openapi 2.8 (Swagger UI) |
| Unit Tests | JUnit 5, Mockito (`@MockitoBean`) |
| Integration Tests | JUnit 5, RestAssured, `@SpringBootTest` |
| Coverage | JaCoCo |
| Static Analysis | SonarCloud |
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

## Test Strategy

The project uses three complementary test layers.

### Controller Tests — `MovieControllerTest`
`@WebMvcTest` with MockMvc and `@MockitoBean` services. Loads only the web layer for fast, isolated tests. Covers:
- Happy path for every endpoint
- Validation rejection (invalid field values, missing required fields)
- Boundary conditions via `@ParameterizedTest` (ID range edges, invalid enum values)
- Correct HTTP status codes per scenario (201, 200, 404, 409, 400)

### Service Tests — `MovieServiceTest`, `StudioServiceTest`
`@ExtendWith(MockitoExtension.class)` pure unit tests. Verify business logic in isolation:
- CRUD operations and duplicate detection
- Filtering and pagination math
- Not-found handling

### Integration Tests — `MovieIntegrationTest`
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with RestAssured against a live embedded server. Covers an ordered CRUD lifecycle:
1. Create studio → create movie → GET by ID → PUT update → GET by studio → GET all → DELETE → verify 404
2. Validation rejection (invalid MID, invalid genre)

---

## Running Locally

**Prerequisites:** Java 21, Maven (or use the included `./mvnw` wrapper)

```bash
# Start the application (uses in-memory data, no DB required)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run tests + generate JaCoCo coverage report
./mvnw verify
# Report: target/site/jacoco/index.html
```

---

## CI Pipeline

Every push to `main` and every pull request triggers the GitHub Actions workflow (`.github/workflows/ci.yml`):

1. Check out code
2. Set up Java 21 (Temurin)
3. Run `./mvnw test`
4. Upload JaCoCo report as a build artifact (retained 14 days)

---

## What's Next

The project is being extended in phases.

**Phase 1 — PostgreSQL**
Swap the in-memory `List<>` store for a real PostgreSQL database using Spring Data JPA. Tests will continue to run against H2 in-memory. No database required to run the test suite.

**Phase 2 — Docker**
Add a `Dockerfile` (multi-stage Maven → JRE 21 build) and a `docker-compose.yml` that runs the API and PostgreSQL together with a named volume for data persistence. One command to run the whole stack locally.

**Phase 3 — Test Quality**
Fix integration test isolation (currently tests are order-dependent), add missing edge-case coverage, add a JaCoCo minimum coverage gate, and switch CI from `test` to `verify`.

**Phase 4 — Load Testing (Gatling)**
Add a Gatling simulation with ramp-up load and response-time assertions wired into CI.

**Phase 5 — Contract Testing (Pact)**
Consumer-driven contract tests for the key API shapes, with provider verification in CI.