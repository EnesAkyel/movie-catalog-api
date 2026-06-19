# Movie Catalog API

[![CI](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EnesAkyel/movie-catalog-api/actions/workflows/ci.yml)

A RESTful Spring Boot API for managing a catalog of movies and studios.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.5 |
| Validation | Jakarta Bean Validation |
| API Docs | springdoc-openapi 2.8 (Swagger UI) |
| Unit/Integration Tests | JUnit 5, MockMvc, RestAssured |
| Coverage | JaCoCo |
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

## Test Strategy

The project uses two complementary test layers.

### Controller Tests — `MovieControllerTest` (35 tests)
`@WebMvcTest` with MockMvc. Spins up only the web layer (no full application context) for fast, focused tests. Covers:
- Happy path for every endpoint
- Validation rejection (invalid field values, missing required fields)
- Boundary conditions via `@ParameterizedTest` (ID range edges, invalid enum values)
- Correct HTTP status codes per scenario (201, 200, 302, 404, 409, 422)

### Integration Tests — `MovieIntegrationTest` (15 tests)
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with RestAssured against a live embedded server. Covers an ordered CRUD lifecycle:
1. Create studio → create movie → GET by ID → PUT update → GET by studio → GET all → DELETE → verify 404

This separation keeps the fast feedback loop (MockMvc) for edge cases while RestAssured validates the full request/response contract end-to-end.

---

## Running Locally

**Prerequisites:** Java 21, Maven (or use the included `./mvnw` wrapper)

```bash
# Start the application
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