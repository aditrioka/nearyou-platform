# NearYou ID - Testing Strategy

**Version:** 1.0  
**Last Updated:** 2025-10-16  
**Status:** Active

---

## Testing Overview

This document outlines the testing strategy for NearYou ID, including unit tests, integration tests, end-to-end tests, and performance tests. The goal is to achieve >80% code coverage and ensure high-quality, reliable software.

---

## Testing Pyramid

```
                    ┌─────────────┐
                    │   E2E Tests │  (10%)
                    └─────────────┘
                  ┌───────────────────┐
                  │ Integration Tests │  (30%)
                  └───────────────────┘
              ┌─────────────────────────────┐
              │       Unit Tests            │  (60%)
              └─────────────────────────────┘
```

### Test Distribution
- **Unit Tests:** 60% - Fast, isolated, test individual components
- **Integration Tests:** 30% - Test component interactions
- **E2E Tests:** 10% - Test complete user flows

---

## Unit Testing

### Scope
Test individual functions, classes, and components in isolation.

### Tools
- **Framework:** Kotlin Test (kotlin.test)
- **Mocking:** MockK
- **Assertions:** kotlin.test assertions
- **Coverage:** Kover (Kotlin Code Coverage)

### Shared Module Tests

**Location:** `shared/src/commonTest/kotlin/`

**Test Categories:**

#### 1. Domain Model Tests
```kotlin
class UserTest {
    @Test
    fun `username validation rejects invalid characters`() {
        val result = User.validateUsername("user@123")
        assertFalse(result.isValid)
        assertEquals("Username can only contain alphanumeric and underscore", result.error)
    }

    @Test
    fun `username validation accepts valid username`() {
        val result = User.validateUsername("user_123")
        assertTrue(result.isValid)
    }
}
```

#### 2. Validation Logic Tests
```kotlin
class PostValidationTest {
    @Test
    fun `post content exceeds max length`() {
        val content = "a".repeat(501)
        val result = PostValidation.validateContent(content)
        assertFalse(result.isValid)
    }

    @Test
    fun `post content is empty`() {
        val result = PostValidation.validateContent("")
        assertFalse(result.isValid)
    }

    @Test
    fun `post content is valid`() {
        val result = PostValidation.validateContent("Hello, world!")
        assertTrue(result.isValid)
    }
}
```

#### 3. Repository Tests (with Mocks)
```kotlin
class PostRepositoryTest {
    private val mockApi = mockk<PostApi>()
    private val mockDb = mockk<PostDao>()
    private val repository = PostRepository(mockApi, mockDb)

    @Test
    fun `getNearbyPosts returns cached data when offline`() = runTest {
        coEvery { mockDb.getNearbyPosts(any(), any()) } returns listOf(mockPost)
        coEvery { mockApi.getNearbyPosts(any(), any()) } throws IOException()

        val result = repository.getNearbyPosts(0.0, 0.0, 1000)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }
}
```

### Backend Tests

**Location:** `server/src/test/kotlin/`

**Test Categories:**

#### 1. Service Layer Tests
```kotlin
class AuthServiceTest {
    private val mockUserRepo = mockk<UserRepository>()
    private val mockOtpService = mockk<OtpService>()
    private val authService = AuthService(mockUserRepo, mockOtpService)

    @Test
    fun `register user with valid email`() = runTest {
        coEvery { mockUserRepo.findByEmail(any()) } returns null
        coEvery { mockUserRepo.create(any()) } returns mockUser
        coEvery { mockOtpService.sendOtp(any()) } returns true

        val result = authService.register("test@example.com", null)

        assertTrue(result.isSuccess)
        coVerify { mockOtpService.sendOtp("test@example.com") }
    }

    @Test
    fun `register fails when email already exists`() = runTest {
        coEvery { mockUserRepo.findByEmail(any()) } returns mockUser

        val result = authService.register("test@example.com", null)

        assertTrue(result.isFailure)
    }
}
```

#### 2. Repository Tests
```kotlin
class PostRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: PostRepository

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        repository = PostRepository(database)
    }

    @Test
    fun `findNearbyPosts returns posts within radius`() = runTest {
        // Insert test posts
        repository.create(createPost(lat = 0.0, lon = 0.0))
        repository.create(createPost(lat = 0.01, lon = 0.01)) // ~1.5 km away
        repository.create(createPost(lat = 1.0, lon = 1.0)) // ~150 km away

        val result = repository.findNearbyPosts(0.0, 0.0, 5000) // 5 km radius

        assertEquals(2, result.size)
    }
}
```

### Frontend Tests

**Location:** `composeApp/src/commonTest/kotlin/`

**Test Categories:**

#### 1. ViewModel Tests
```kotlin
class TimelineViewModelTest {
    private val mockPostRepo = mockk<PostRepository>()
    private val viewModel = TimelineViewModel(mockPostRepo)

    @Test
    fun `loadNearbyPosts updates state with posts`() = runTest {
        val posts = listOf(mockPost1, mockPost2)
        coEvery { mockPostRepo.getNearbyPosts(any(), any(), any()) } returns Result.success(posts)

        viewModel.loadNearbyPosts(0.0, 0.0, 1000)

        assertEquals(posts, viewModel.state.value.posts)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadNearbyPosts handles error`() = runTest {
        coEvery { mockPostRepo.getNearbyPosts(any(), any(), any()) } returns Result.failure(Exception())

        viewModel.loadNearbyPosts(0.0, 0.0, 1000)

        assertTrue(viewModel.state.value.error != null)
        assertFalse(viewModel.state.value.isLoading)
    }
}
```

#### 2. UI Component Tests
```kotlin
class PostCardTest {
    @Test
    fun `PostCard displays post content`() = runComposeUiTest {
        setContent {
            PostCard(post = mockPost, onLike = {}, onReply = {}, onMessage = {})
        }

        onNodeWithText(mockPost.content).assertIsDisplayed()
        onNodeWithText(mockPost.user.displayName).assertIsDisplayed()
    }

    @Test
    fun `PostCard like button triggers callback`() = runComposeUiTest {
        var liked = false
        setContent {
            PostCard(post = mockPost, onLike = { liked = true }, onReply = {}, onMessage = {})
        }

        onNodeWithContentDescription("Like").performClick()

        assertTrue(liked)
    }
}
```

### Coverage Target
- **Minimum:** 80% line coverage
- **Goal:** 90% line coverage for critical paths (auth, payments, geo queries)

---

## Integration Testing

### Scope
Test interactions between components, including API endpoints, database operations, and external services.

### Tools
- **Framework:** Kotlin Test
- **HTTP Testing:** Ktor Test (testApplication)
- **Database:** Testcontainers (PostgreSQL + PostGIS)
- **Mocking:** MockK for external services

### Backend Integration Tests

**Location:** `server/src/test/kotlin/integration/`

#### 1. API Endpoint Tests
```kotlin
class PostRoutesTest {
    @Test
    fun `POST posts creates new post`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.post("/posts") {
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest(content = "Hello", lat = 0.0, lon = 0.0))
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val post = response.body<Post>()
        assertEquals("Hello", post.content)
    }

    @Test
    fun `GET posts nearby returns posts within radius`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.get("/posts/nearby?lat=0.0&lon=0.0&radius=1000") {
            bearerAuth(validToken)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val posts = response.body<List<Post>>()
        assertTrue(posts.all { it.distance <= 1000 })
    }
}
```

#### 2. Database Integration Tests
```kotlin
class PostRepositoryIntegrationTest {
    private lateinit var container: PostgreSQLContainer<*>
    private lateinit var database: Database

    @BeforeTest
    fun setup() {
        container = PostgreSQLContainer("postgis/postgis:15-3.3")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
        container.start()

        database = connectToDatabase(container.jdbcUrl)
        runMigrations(database)
    }

    @AfterTest
    fun teardown() {
        container.stop()
    }

    @Test
    fun `geo query uses GiST index`() = runTest {
        val repository = PostRepository(database)
        
        // Insert 1000 posts
        repeat(1000) {
            repository.create(createRandomPost())
        }

        val start = System.currentTimeMillis()
        val posts = repository.findNearbyPosts(0.0, 0.0, 5000)
        val duration = System.currentTimeMillis() - start

        assertTrue(duration < 100) // Should be < 100ms
        assertTrue(posts.isNotEmpty())
    }
}
```

### Frontend Integration Tests

**Location:** `composeApp/src/androidTest/kotlin/` (Android)

#### 1. Screen Navigation Tests
```kotlin
@Test
fun `login flow navigates to timeline on success`() {
    composeTestRule.setContent {
        NearYouApp()
    }

    composeTestRule.onNodeWithText("Login with Email").performClick()
    composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
    composeTestRule.onNodeWithText("Send OTP").performClick()
    
    // Wait for OTP screen
    composeTestRule.waitUntil(5000) {
        composeTestRule.onAllNodesWithText("Enter OTP").fetchSemanticsNodes().isNotEmpty()
    }
    
    composeTestRule.onNodeWithTag("otp_input").performTextInput("123456")
    composeTestRule.onNodeWithText("Verify").performClick()
    
    // Should navigate to timeline
    composeTestRule.onNodeWithText("Nearby").assertIsDisplayed()
}
```

---

## End-to-End Testing

### Scope
Test complete user flows from UI to backend to database.

### Tools
- **Framework:** Maestro (mobile UI testing)
- **Alternative:** Appium, Detox

### E2E Test Scenarios

#### 1. User Registration Flow
```yaml
# maestro/register.yaml
appId: com.nearyou.id
---
- launchApp
- tapOn: "Sign Up"
- inputText: "test@example.com"
- tapOn: "Send OTP"
- waitForAnimationToEnd
- inputText: "123456"
- tapOn: "Verify"
- assertVisible: "Welcome to NearYou"
```

#### 2. Post Creation Flow
```yaml
# maestro/create_post.yaml
appId: com.nearyou.id
---
- launchApp
- tapOn: "Create Post"
- inputText: "Hello, world!"
- tapOn: "Post"
- waitForAnimationToEnd
- assertVisible: "Hello, world!"
```

#### 3. Messaging Flow
```yaml
# maestro/send_message.yaml
appId: com.nearyou.id
---
- launchApp
- tapOn: "Nearby"
- tapOn:
    id: "post_card_0"
- tapOn: "Message"
- inputText: "Hi there!"
- tapOn: "Send"
- assertVisible: "Hi there!"
```

---

## Performance Testing

### Scope
Test system performance under load, focusing on geo queries and API response times.

### Tools
- **Load Testing:** k6, Gatling
- **Database Profiling:** EXPLAIN ANALYZE
- **APM:** Prometheus + Grafana

### Performance Test Scenarios

#### 1. Geo Query Performance Test
```kotlin
@Test
fun `geo query performance with 1M posts`() = runTest {
    val repository = PostRepository(database)
    
    // Insert 1M posts
    repeat(1_000_000) {
        repository.create(createRandomPost())
    }

    // Measure p95 latency
    val latencies = mutableListOf<Long>()
    repeat(100) {
        val start = System.nanoTime()
        repository.findNearbyPosts(randomLat(), randomLon(), 5000)
        val duration = (System.nanoTime() - start) / 1_000_000 // Convert to ms
        latencies.add(duration)
    }

    val p95 = latencies.sorted()[94]
    assertTrue(p95 < 100, "p95 latency: ${p95}ms (expected < 100ms)")
}
```

#### 2. Load Test (k6)
```javascript
// k6/load_test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '2m', target: 100 },  // Ramp up to 100 users
        { duration: '5m', target: 100 },  // Stay at 100 users
        { duration: '2m', target: 1000 }, // Ramp up to 1000 users
        { duration: '5m', target: 1000 }, // Stay at 1000 users
        { duration: '2m', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<200'], // 95% of requests < 200ms
        http_req_failed: ['rate<0.01'],   // Error rate < 1%
    },
};

export default function () {
    const token = 'Bearer ' + __ENV.AUTH_TOKEN;
    const response = http.get('http://localhost:8080/posts/nearby?lat=0.0&lon=0.0&radius=5000', {
        headers: { 'Authorization': token },
    });

    check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);
}
```

**Run Load Test:**
```bash
k6 run k6/load_test.js
```

---

## Test Automation

### CI/CD Integration

**GitHub Actions Workflow:**
```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Run Unit Tests
        run: ./gradlew test
        
      - name: Run Integration Tests
        run: ./gradlew integrationTest
        
      - name: Generate Coverage Report
        run: ./gradlew koverHtmlReport
        
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./build/reports/kover/html/index.html
```

---

## Test Data Management

### Test Fixtures
```kotlin
object TestFixtures {
    fun createMockUser(
        id: String = UUID.randomUUID().toString(),
        username: String = "testuser",
        email: String = "test@example.com"
    ) = User(
        id = id,
        username = username,
        displayName = "Test User",
        email = email,
        isVerified = true,
        subscriptionTier = "free"
    )

    fun createMockPost(
        id: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        content: String = "Test post",
        lat: Double = 0.0,
        lon: Double = 0.0
    ) = Post(
        id = id,
        userId = userId,
        content = content,
        location = Location(lat, lon),
        likeCount = 0,
        commentCount = 0,
        createdAt = Clock.System.now()
    )
}
```

---

## Testing Best Practices

1. **Arrange-Act-Assert:** Structure tests clearly
2. **Test Naming:** Use descriptive names (e.g., `should return error when username is invalid`)
3. **Isolation:** Each test should be independent
4. **Fast Tests:** Unit tests should run in milliseconds
5. **Deterministic:** Tests should produce consistent results
6. **Clean Up:** Always clean up test data and resources
7. **Mock External Services:** Don't call real APIs in tests
8. **Test Edge Cases:** Test boundary conditions and error paths
9. **Coverage:** Aim for >80% coverage, but focus on critical paths
10. **Continuous Testing:** Run tests on every commit

---

## Test Execution

### Run All Tests
```bash
./gradlew test
```

### Run Specific Module Tests
```bash
./gradlew :shared:test
./gradlew :server:test
./gradlew :composeApp:testDebugUnitTest
```

### Run Integration Tests
```bash
./gradlew integrationTest
```

### Run with Coverage
```bash
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

### Run E2E Tests
```bash
maestro test maestro/
```

### Run Load Tests
```bash
k6 run k6/load_test.js
```

---

## Test Reporting

### Coverage Reports
- **Location:** `build/reports/kover/html/index.html`
- **Format:** HTML, XML, JSON
- **Upload:** Codecov, Coveralls

### Test Results
- **Location:** `build/test-results/`
- **Format:** JUnit XML
- **CI Integration:** GitHub Actions, GitLab CI

---

## Continuous Improvement

1. **Monitor Coverage:** Track coverage trends over time
2. **Review Flaky Tests:** Identify and fix unstable tests
3. **Performance Benchmarks:** Track performance metrics
4. **Test Maintenance:** Regularly update and refactor tests
5. **Team Training:** Ensure team follows testing best practices

