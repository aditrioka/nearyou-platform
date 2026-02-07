# Backend Architecture Assessment

**Date:** 2026-02-07
**Scope:** Ktor server, database, API design, security, production readiness
**Status:** MVP / Early Development

---

## Executive Summary

The backend is a Ktor-based REST API with PostgreSQL/PostGIS, Redis, and Koin DI. The architecture follows reasonable patterns for an MVP but has **critical security vulnerabilities** (SQL injection), missing production infrastructure (CORS, request size limits, migration tooling), and several code quality issues that should be addressed before any public deployment.

**Overall Grade: C+** -- Functional MVP with solid foundations, but critical gaps in security and production readiness.

---

## 1. Routing Structure and API Design

**Grade: B**

### Strengths
- Clean route organization by domain: `auth/`, `users/`, `posts/`, `upload/`
- Routes are extension functions on `Route`, idiomatic Ktor pattern
- Consistent use of `authenticate("auth-jwt")` blocks for protected endpoints
- RESTful conventions followed (GET, POST, PUT, DELETE with appropriate status codes)
- Health check endpoint at `/health` with structured response

### Issues
- **No API versioning** (e.g., `/api/v1/`). Breaking changes will affect all clients simultaneously.
- **No CORS configuration**. Mobile clients may work without it, but web clients will be blocked. This should be configured explicitly regardless.
- **No request body size limits** configured via Ktor plugins. The upload route manually checks 5MB, but POST bodies to other routes are unbounded.
- **Inconsistent service injection**: `authRoutes` and `userRoutes` use `application.get<>()`, while `postRoutes` uses `by inject<>()`. Should pick one pattern.
- **No pagination** on the nearby posts endpoint beyond a simple `limit` parameter (no cursor/offset).

---

## 2. Dependency Injection

**Grade: B+**

### Strengths
- Koin is well-suited for Ktor; `serverModule` is clean and centralized
- Services are properly scoped as singletons
- Redis client lifecycle is managed through Koin + shutdown hook
- `StorageService` uses interface-based DI, enabling easy swap to S3/GCS

### Issues
- `UserService()` and `PostService()` are instantiated with no constructor dependencies, yet they call static `UserRepository` and `PostRepository` objects directly. This defeats testability -- repositories should be injected.
- `UserRepository` and `PostRepository` are `object` singletons with static methods, making them impossible to mock without frameworks like MockK's `mockkObject`.
- The `TestDatabaseConnection.kt` and `ShowDatabaseData.kt` files hardcode database credentials and bypass the DI/config system entirely. These should be dev-only utilities or removed from `main` sources.

---

## 3. Error Handling

**Grade: A-**

### Strengths
- Well-designed exception hierarchy: `ApiException` sealed class with specific subtypes (`ValidationException`, `AuthenticationException`, `NotFoundException`, etc.)
- Centralized `StatusPages` plugin handles all exception types consistently
- Structured `ErrorResponse` with error code, message, and timestamp
- Covers edge cases: serialization errors, NPEs, 404/405 status codes
- Proper logging levels (warn for client errors, error for server errors)

### Issues
- `ValidationException` handler is redundant since `ValidationException` extends `ApiException` and the generic `ApiException` handler would catch it. The more specific handler runs first in Ktor, so it works, but the response logic is duplicated.
- `AuthenticationException` handler is similarly redundant.
- `PostService` throws `RuntimeException("Failed to create post")` instead of a proper `InternalServerException`. Same for update/delete failures.

---

## 4. Security

**Grade: D** (Critical Issues)

### Critical: SQL Injection in PostRepository

`PostRepository.findNearbyPosts()` and `PostRepository.createPost()` build SQL queries via **string interpolation** with user-controlled values:

```kotlin
// findNearbyPosts - latitude/longitude/radiusMeters come from query params
ST_MakePoint(${userLocation.longitude}, ${userLocation.latitude})::geography,
$radiusMeters

// createPost - content is user input, only single-quote escaped
'${content.replace("'", "''")}'

// mediaUrls are interpolated directly
"ARRAY[${mediaUrls.joinToString(",") { "'$it'" }}]"
```

The `latitude`/`longitude` values are parsed as `Double` (somewhat safe), but `mediaUrls` strings are directly interpolated with no sanitization. The content escaping (`replace("'", "''")`) is insufficient for all edge cases. **These must use parameterized queries.**

### Other Security Issues

- **JWT secret has a hardcoded default**: `"your-secret-key-change-in-production"`. The `validate()` function only enforces changing it when `otpProvider != "mock"`, which means a misconfigured production deployment could run with the default secret.
- **Access token expiry is 7 days** -- excessively long. Industry standard is 15-60 minutes. The refresh token (30 days) is reasonable.
- **No CORS configuration** at all.
- **No rate limiting on authentication endpoints** beyond OTP-specific rate limiting. Login/register endpoints are vulnerable to brute force.
- **OTP is 6 digits generated with `kotlin.random.Random`** (not `SecureRandom`). This is cryptographically weak. The OTP space is only 900,000 values.
- **OTP codes are logged to stdout** in mock mode: `println("Code: $code")`. Ensure this is never active in production.
- **Static file serving** at `/uploads` exposes the upload directory directly. No access control on uploaded files.
- **No input validation on registration fields** (email format, phone format, password strength) in the `AuthService`. The database has a username regex check, but email/phone are unchecked.
- **Pending registration data stored in Redis** uses pipe-delimited format that could be exploited if a username contains `|`.
- **File upload validates content-type header** but does not validate actual file content (magic bytes). A user could upload a non-image file with an `image/*` content-type.
- **No `HttpsRedirect` or `HSTS` configuration**.

---

## 5. Database Access Patterns

**Grade: B-**

### Strengths
- PostGIS usage is correct: `GEOGRAPHY(Point, 4326)` type, `ST_DWithin` for radius queries, `ST_Distance` for sorting
- Partial GiST index on `posts.location WHERE is_deleted = FALSE` is well-designed
- HikariCP connection pooling with reasonable defaults
- Schema has good constraints (CHECK, UNIQUE, NOT NULL) and partial indexes
- Soft delete pattern for posts
- Database triggers for `updated_at` columns

### Issues
- **Raw SQL with string interpolation** (security issue above) instead of parameterized queries
- **Mixed ORM patterns**: `UserRepository` uses Exposed DSL properly, but `PostRepository` uses raw SQL `exec()` for most operations. This inconsistency makes the codebase harder to maintain.
- **Table definitions are scattered**: `OtpCodes` and `RefreshTokens` are defined inside `AuthService.Companion`, `Users` inside `UserRepository`, and `Posts` as a top-level object. Should be centralized.
- **No database migration tool** (Flyway/Liquibase). Migrations are plain SQL files loaded by Docker init scripts, which means re-running migrations or applying them to an existing database is manual.
- **`exposed-kotlin-datetime` timestamps vs SQL `TIMESTAMP`**: The schema uses `TIMESTAMP` (without timezone) but the code uses `kotlinx.datetime.Instant`. This can cause timezone issues. Should use `TIMESTAMPTZ`.
- **No transaction isolation levels specified** for operations that need consistency (e.g., checking if email exists then creating user).
- **Cleanup of expired OTPs/tokens**: A SQL function `cleanup_expired_auth_data()` exists but no mechanism to invoke it (no cron, no scheduled task).

---

## 6. Serialization

**Grade: B+**

### Strengths
- `kotlinx.serialization` with Ktor content negotiation is a good choice
- `ignoreUnknownKeys = true` makes the API forward-compatible
- Shared models in the `shared` module enable code sharing with the mobile client

### Issues
- `isLenient = true` accepts malformed JSON. This should be `false` in production for stricter parsing.
- `prettyPrint = true` adds unnecessary payload size. Should be `false` in production (or configurable per environment).
- Some route handlers return `mapOf(...)` instead of typed response classes (e.g., health check, nearby posts, logout, delete post). These ad-hoc maps bypass serialization type safety.

---

## 7. Production Readiness

**Grade: D+**

### What Exists
- Dockerfile with multi-stage build, non-root user, health check, JVM container flags
- docker-compose.yml with PostgreSQL (PostGIS) and Redis, health checks, persistent volumes
- HikariCP connection pool with leak detection
- Graceful shutdown hook for Redis and database connections
- Environment-based configuration via `EnvironmentConfig`

### What Is Missing
- **No structured logging**. Uses `println()` throughout and SLF4J/Logback only in error handling. No request/response logging, no correlation IDs, no JSON log format.
- **No metrics/monitoring**. Prometheus/Grafana are commented out in docker-compose. No Micrometer integration in Ktor.
- **No CORS plugin installed**.
- **No request logging** (Ktor `CallLogging` plugin not installed).
- **No compression** (Ktor `Compression` plugin not installed).
- **No request timeout** configuration.
- **No graceful shutdown** for the Ktor server itself (only the shutdown hook cleans up resources, but there is no `ShutdownUrl` or signal handling).
- **Server is not in docker-compose**. Only postgres and redis are defined; the application itself must be run separately.
- **No CI/CD pipeline** visible.
- **`TestDatabaseConnection.kt` and `ShowDatabaseData.kt`** are in `main` source set with hardcoded credentials. Should be in `test` or a separate tooling module.
- **Config validation is weak**: only checks JWT secret and DB credentials. Does not validate Redis connectivity, port ranges, or token expiry values.

---

## 8. Code Organization and Modularity

**Grade: B**

### Strengths
- Feature-based package structure: `auth/`, `user/`, `post/`, `upload/`, `config/`, `plugins/`, `exceptions/`, `di/`, `storage/`, `repository/`
- Shared module (`projects.shared`) for cross-platform models
- Clean separation: Routes -> Service -> Repository
- `StorageService` interface allows swapping implementations

### Issues
- **Repository layer inconsistency**: `UserRepository` is in `repository/` package, but `PostRepository` is in `post/` package
- **`UserRepository` is an `object`**, `PostRepository` is an `object` -- fine for singletons, but prevents injection and testing
- **Table definitions scattered** across multiple files and even nested in companion objects
- **No shared base for extracting JWT principal**. The pattern `call.principal<JWTPrincipal>() ?: throw AuthenticationException(...)` followed by `principal.payload.subject ?: throw ...` is repeated verbatim in every authenticated route handler (8+ times). Should be extracted to a utility function.
- **`TestDatabaseConnection.kt` and `ShowDatabaseData.kt`** should not be in main source set

---

## Priority Recommendations

### P0 -- Critical (Fix Before Any Public Deployment)
1. **Fix SQL injection in `PostRepository`** -- Use parameterized queries for all raw SQL
2. **Use `SecureRandom` for OTP generation** instead of `kotlin.random.Random`
3. **Reduce access token expiry** to 15-60 minutes
4. **Remove/restrict default JWT secret** -- fail startup if not explicitly configured in non-dev environments

### P1 -- High (Fix Before Beta)
5. Add CORS configuration
6. Add request body size limits
7. Add proper request logging (`CallLogging` plugin)
8. Use parameterized queries consistently or migrate PostRepository to Exposed DSL
9. Add input validation for email, phone, and password on registration
10. Validate file content (magic bytes) on upload, not just content-type header
11. Extract JWT principal extraction to a shared utility

### P2 -- Medium (Improve for Production)
12. Add API versioning (`/api/v1/`)
13. Install a migration tool (Flyway)
14. Add structured JSON logging
15. Add metrics (Micrometer + Prometheus)
16. Make repositories injectable (interface + implementation)
17. Centralize table definitions
18. Use `TIMESTAMPTZ` instead of `TIMESTAMP` in schema
19. Add pagination (cursor-based) to list endpoints
20. Move test utilities out of main source set

### P3 -- Low (Nice to Have)
21. Add response compression
22. Set `prettyPrint = false` and `isLenient = false` for production
23. Add rate limiting to all endpoints (not just OTP)
24. Add server to docker-compose.yml
25. Schedule expired OTP/token cleanup

---

## Architecture Diagram (Current State)

```
Client (Android/iOS)
    |
    v
[Ktor Server (Netty)]
    |-- Koin DI
    |-- JWT Authentication
    |-- StatusPages Error Handling
    |-- Content Negotiation (kotlinx.serialization)
    |
    +-- /auth/*     -> AuthService  -> UserRepository (Exposed) + OTP/RefreshToken tables
    +-- /users/*    -> UserService  -> UserRepository (Exposed)
    +-- /posts/*    -> PostService  -> PostRepository (Raw SQL + Exposed)
    +-- /upload/*   -> StorageService (LocalStorageService)
    +-- /uploads/*  -> Static Files
    |
    v                    v
[PostgreSQL + PostGIS]  [Redis]
  (HikariCP pool)       (Lettuce)
```
