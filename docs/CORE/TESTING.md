# Testing Strategy

**Version:** 1.0  
**Last Updated:** 2025-10-25  
**Status:** Active

---

## Overview

This document defines the testing strategy for NearYou ID, a Kotlin Multiplatform project targeting Android, iOS, and Server platforms.

---

## Testing Pyramid

```
        /\
       /E2E\         ← Few (Critical user flows)
      /------\
     /  API   \      ← Some (Integration tests)
    /----------\
   /   Unit     \    ← Many (Business logic)
  /--------------\
```

### Distribution Target
- **70% Unit Tests** → Fast, isolated, business logic
- **20% Integration Tests** → API endpoints, database operations
- **10% E2E Tests** → Critical user journeys

---

## Test Types

### 1. Unit Tests
**Purpose:** Test individual components in isolation

**Scope:**
- Domain models and validation logic
- Use cases and business rules
- Utility functions
- View models

**Tools:**
- Kotlin Test (`kotlin.test`)
- MockK (mocking framework)
- Turbine (Flow testing)

**Location:**
- `shared/src/commonTest/` → Platform-independent tests
- `shared/src/jvmTest/` → JVM-specific tests
- `composeApp/src/commonTest/` → UI logic tests

**Example:**
```kotlin
class UserValidationTest {
    @Test
    fun `valid email should pass validation`() {
        val email = "user@example.com"
        assertTrue(EmailValidator.isValid(email))
    }
}
```

---

### 2. Integration Tests
**Purpose:** Test component interactions and external dependencies

**Scope:**
- API endpoints (Ktor routes)
- Database operations (PostgreSQL + PostGIS)
- Repository implementations
- Network layer

**Tools:**
- Ktor Test (`io.ktor:ktor-server-test-host`)
- Testcontainers (PostgreSQL)
- Kotest (assertions)

**Location:**
- `server/src/test/` → Backend integration tests
- `shared/src/jvmTest/` → Repository tests

**Example:**
```kotlin
class AuthApiTest {
    @Test
    fun `POST auth register should create user`() = testApplication {
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@example.com","password":"Test123!"}""")
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
    }
}
```

---

### 3. End-to-End Tests
**Purpose:** Test complete user flows across the system

**Scope:**
- User registration and login
- Post creation and viewing
- Location-based features
- Chat functionality

**Tools:**
- Maestro (mobile UI testing)
- K6 (load testing)

**Location:**
- `maestro/` → Mobile UI test flows
- `k6/` → Load test scripts

**Example (Maestro):**
```yaml
appId: com.nearyou.id
---
- launchApp
- tapOn: "Sign Up"
- inputText: "test@example.com"
- tapOn: "Continue"
- assertVisible: "Welcome"
```

---

## Testing Best Practices

### General Principles
1. **Test Behavior, Not Implementation** → Focus on what, not how
2. **Arrange-Act-Assert (AAA)** → Clear test structure
3. **One Assertion Per Test** → Single responsibility
4. **Descriptive Test Names** → Use backticks for readability
5. **Fast and Isolated** → No external dependencies in unit tests

### Kotlin Multiplatform Specifics
1. **Prefer `commonTest`** → Write platform-independent tests when possible
2. **Use `expect/actual`** → For platform-specific test utilities
3. **Mock External Dependencies** → Use MockK for interfaces
4. **Test Shared Logic Thoroughly** → Shared code is used by all platforms

### Database Testing
1. **Use Testcontainers** → Real PostgreSQL instance for integration tests
2. **Clean State** → Reset database between tests
3. **Test Migrations** → Verify schema changes work correctly
4. **Test PostGIS Functions** → Verify geospatial queries

---

## Test Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| Domain Models | 90%+ | High |
| Use Cases | 85%+ | High |
| Repositories | 80%+ | Medium |
| API Endpoints | 75%+ | Medium |
| UI Components | 60%+ | Low |

**Measurement:**
```bash
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

---

## Running Tests

### All Tests
```bash
./gradlew test
```

### Specific Module
```bash
./gradlew :shared:test
./gradlew :server:test
./gradlew :composeApp:test
```

### Integration Tests Only
```bash
./gradlew integrationTest
```

### Android Instrumented Tests
```bash
./gradlew :composeApp:connectedDebugAndroidTest
```

### E2E Tests
```bash
# Maestro
maestro test maestro/

# K6 Load Tests
k6 run k6/load_test.js
```

---

## Continuous Integration

Tests run automatically on:
- Every push to feature branches
- Pull requests to `main` or `develop`
- Scheduled nightly builds

**CI Pipeline:**
1. Unit tests (all modules)
2. Integration tests (backend + shared)
3. Build verification (all platforms)
4. Code coverage report
5. E2E tests (on main branch only)

---

## Test Maintenance

1. **Monitor Coverage:** Track coverage trends over time
2. **Review Flaky Tests:** Identify and fix unstable tests
3. **Performance Benchmarks:** Track performance metrics
4. **Test Maintenance:** Regularly update and refactor tests
5. **Team Training:** Ensure team follows testing best practices

---

## Validation

For detailed validation procedures, see **[VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md)**.

**Quick reference:**
- **AI validation** → Automated tests, builds, file consistency
- **Human validation** → Manual testing, external services, UI/UX
- **Hybrid validation** → Combination of both

---

## Related Documents

- **[VALIDATION_GUIDE.md](./VALIDATION_GUIDE.md)** → Comprehensive validation procedures
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** → System architecture
- **[INFRA.md](./INFRA.md)** → Infrastructure and deployment
- **[BEST_PRACTICES_EVALUATION.md](./BEST_PRACTICES_EVALUATION.md)** → Best practices compliance
