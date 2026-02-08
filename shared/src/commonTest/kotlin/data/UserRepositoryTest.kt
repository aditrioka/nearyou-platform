package data

import domain.model.SubscriptionTier
import domain.model.UpdateUserRequest
import domain.model.UploadResponse
import domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Serializable
private data class UserErrorResponse(
    val error: String,
    val message: String
)

class UserRepositoryTest {

    private lateinit var mockTokenStorage: MockTokenStorage
    private lateinit var repository: UserRepository
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    @BeforeTest
    fun setup() {
        mockTokenStorage = MockTokenStorage()
    }

    // ========== getCurrentUser Tests ==========

    @Test
    fun `getCurrentUser should return user on success`() = runTest {
        val mockUser = createMockUser()
        val mockEngine = MockEngine { request ->
            assertEquals("/users/me", request.url.encodedPath)
            assertEquals(HttpMethod.Get, request.method)

            respond(
                content = json.encodeToString(mockUser),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockTokenStorage.saveAccessToken("test_token")
        repository = createRepository(mockEngine)

        val result = repository.getCurrentUser()

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("testuser", user.username)
        assertEquals("Test User", user.displayName)
    }

    @Test
    fun `getCurrentUser should return failure on 401`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    UserErrorResponse(error = "UNAUTHORIZED", message = "Invalid token")
                ),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockTokenStorage.saveAccessToken("expired_token")
        repository = createRepository(mockEngine)

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertEquals("Invalid token", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCurrentUser should fail when not authenticated`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request when not authenticated")
        }

        repository = createRepository(mockEngine)

        val result = repository.getCurrentUser()

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()?.message)
    }

    // ========== updateCurrentUser Tests ==========

    @Test
    fun `updateCurrentUser should return updated user on success`() = runTest {
        val updatedUser = createMockUser().copy(displayName = "Updated Name")
        val mockEngine = MockEngine { request ->
            assertEquals("/users/me", request.url.encodedPath)
            assertEquals(HttpMethod.Put, request.method)

            respond(
                content = json.encodeToString(updatedUser),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockTokenStorage.saveAccessToken("test_token")
        repository = createRepository(mockEngine)

        val result = repository.updateCurrentUser(
            UpdateUserRequest(displayName = "Updated Name")
        )

        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.displayName)
    }

    @Test
    fun `updateCurrentUser should return failure on 400`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    UserErrorResponse(error = "VALIDATION_ERROR", message = "Invalid display name")
                ),
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockTokenStorage.saveAccessToken("test_token")
        repository = createRepository(mockEngine)

        val result = repository.updateCurrentUser(
            UpdateUserRequest(displayName = "")
        )

        assertTrue(result.isFailure)
        assertEquals("Invalid display name", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateCurrentUser should fail when not authenticated`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request when not authenticated")
        }

        repository = createRepository(mockEngine)

        val result = repository.updateCurrentUser(
            UpdateUserRequest(displayName = "New Name")
        )

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()?.message)
    }

    // ========== uploadProfilePhoto Tests ==========

    @Test
    fun `uploadProfilePhoto should return upload response on success`() = runTest {
        val uploadResponse = UploadResponse(
            url = "https://storage.example.com/photos/profile.jpg",
            fileName = "profile.jpg",
            contentType = "image/jpeg",
            size = 12345L
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/upload/profile-photo", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            respond(
                content = json.encodeToString(uploadResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockTokenStorage.saveAccessToken("test_token")
        repository = createRepository(mockEngine)

        val result = repository.uploadProfilePhoto(
            imageBytes = ByteArray(10) { 0xFF.toByte() },
            fileName = "profile.jpg"
        )

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("https://storage.example.com/photos/profile.jpg", response.url)
    }

    @Test
    fun `uploadProfilePhoto should fail when not authenticated`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request when not authenticated")
        }

        repository = createRepository(mockEngine)

        val result = repository.uploadProfilePhoto(
            imageBytes = ByteArray(10),
            fileName = "profile.jpg"
        )

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()?.message)
    }

    // ========== Helpers ==========

    private fun createRepository(mockEngine: MockEngine): UserRepository {
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url("http://localhost:8080")
            }
        }
        return UserRepository(
            tokenStorage = mockTokenStorage,
            httpClient = client
        )
    }

    private fun createMockUser(): User {
        return User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            isVerified = true,
            subscriptionTier = SubscriptionTier.FREE,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
}
