package data

import domain.model.User
import domain.model.SubscriptionTier
import domain.model.auth.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Simple error response for testing purposes
 */
@Serializable
private data class ErrorResponse(
    val error: String,
    val message: String
)

/**
 * Comprehensive tests for AuthRepository
 * Tests all authentication operations with mocked HTTP client
 */
class AuthRepositoryTest {
    
    private lateinit var mockTokenStorage: MockTokenStorage
    private lateinit var repository: AuthRepository
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
    
    @BeforeTest
    fun setup() {
        mockTokenStorage = MockTokenStorage()
    }
    
    @AfterTest
    fun tearDown() {
        // HttpClient is now managed by DI, no need to close repository
    }
    
    // ========== Register Tests ==========
    
    @Test
    fun `register should return success when API returns 200`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/auth/register", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            
            respond(
                content = json.encodeToString(
                    OtpSentResponse(
                        message = "OTP sent successfully",
                        identifier = "test@example.com",
                        type = "email",
                        expiresInSeconds = 300
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val request = RegisterRequest(
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            password = "password123"
        )
        
        val result = repository.register(request)
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("OTP sent successfully", response.message)
        assertEquals("test@example.com", response.identifier)
        assertEquals("email", response.type)
        assertEquals(300, response.expiresInSeconds)
    }
    
    @Test
    fun `register should return failure when API returns error`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    ErrorResponse(
                        error = "EMAIL_EXISTS",
                        message = "Email already registered"
                    )
                ),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val request = RegisterRequest(
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            password = "password123"
        )
        
        val result = repository.register(request)
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("Email already registered", exception.message)
    }
    
    // ========== Login Tests ==========
    
    @Test
    fun `login with email should return success`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/auth/login", request.url.encodedPath)
            
            respond(
                content = json.encodeToString(
                    OtpSentResponse(
                        message = "OTP sent successfully",
                        identifier = "test@example.com",
                        type = "email",
                        expiresInSeconds = 300
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val result = repository.login("test@example.com", "email")
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("test@example.com", response.identifier)
        assertEquals("email", response.type)
    }
    
    @Test
    fun `login with phone should return success`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    OtpSentResponse(
                        message = "OTP sent successfully",
                        identifier = "+1234567890",
                        type = "phone",
                        expiresInSeconds = 300
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val result = repository.login("+1234567890", "phone")
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("+1234567890", response.identifier)
        assertEquals("phone", response.type)
    }
    
    @Test
    fun `login should return failure when user not found`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    ErrorResponse(
                        error = "USER_NOT_FOUND",
                        message = "User not found"
                    )
                ),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val result = repository.login("nonexistent@example.com", "email")
        
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }
    
    // ========== Verify OTP Tests ==========
    
    @Test
    fun `verifyOtp should save tokens and return success`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/auth/verify-otp", request.url.encodedPath)
            
            respond(
                content = json.encodeToString(
                    AuthResponse(
                        accessToken = "access_token_123",
                        refreshToken = "refresh_token_456",
                        user = User(
                            id = "user_id_789",
                            username = "testuser",
                            displayName = "Test User",
                            email = "test@example.com",
                            phone = null,
                            isVerified = true,
                            subscriptionTier = SubscriptionTier.FREE,
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now()
                        )
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val request = VerifyOtpRequest(
            identifier = "test@example.com",
            code = "123456",
            type = "email"
        )
        
        val result = repository.verifyOtp(request)
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("access_token_123", response.accessToken)
        assertEquals("refresh_token_456", response.refreshToken)
        assertEquals("testuser", response.user.username)
        
        // Verify tokens were saved
        assertEquals("access_token_123", mockTokenStorage.getAccessToken())
        assertEquals("refresh_token_456", mockTokenStorage.getRefreshToken())
    }
    
    @Test
    fun `verifyOtp should return failure when OTP is invalid`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    ErrorResponse(
                        error = "INVALID_OTP",
                        message = "Invalid or expired OTP"
                    )
                ),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        repository = createRepository(mockEngine)
        
        val request = VerifyOtpRequest(
            identifier = "test@example.com",
            code = "wrong_code",
            type = "email"
        )
        
        val result = repository.verifyOtp(request)
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired OTP", result.exceptionOrNull()?.message)
        
        // Verify tokens were NOT saved
        assertNull(mockTokenStorage.getAccessToken())
        assertNull(mockTokenStorage.getRefreshToken())
    }
    
    // ========== Refresh Token Tests ==========

    @Test
    fun `refreshToken should save new tokens and return success`() = runTest {
        // Setup: Save initial refresh token
        mockTokenStorage.saveRefreshToken("old_refresh_token")

        val mockEngine = MockEngine { request ->
            assertEquals("/auth/refresh", request.url.encodedPath)

            respond(
                content = json.encodeToString(
                    TokenResponse(
                        accessToken = "new_access_token",
                        refreshToken = "new_refresh_token"
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = createRepository(mockEngine)

        val result = repository.refreshToken()

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("new_access_token", response.accessToken)
        assertEquals("new_refresh_token", response.refreshToken)

        // Verify new tokens were saved
        assertEquals("new_access_token", mockTokenStorage.getAccessToken())
        assertEquals("new_refresh_token", mockTokenStorage.getRefreshToken())
    }

    @Test
    fun `refreshToken should return failure when no refresh token available`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request when no refresh token")
        }

        repository = createRepository(mockEngine)

        val result = repository.refreshToken()

        assertTrue(result.isFailure)
        assertEquals("No refresh token available", result.exceptionOrNull()?.message)
    }

    // ========== Logout Tests ==========

    @Test
    fun `logout should clear all tokens`() = runTest {
        // Setup: Save tokens
        mockTokenStorage.saveAccessToken("access_token")
        mockTokenStorage.saveRefreshToken("refresh_token")

        val mockEngine = MockEngine { request ->
            // Expect POST to /auth/logout
            assertEquals("/auth/logout", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)

            // Return success
            respond(
                content = json.encodeToString(mapOf("message" to "Logged out successfully")),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        repository = createRepository(mockEngine)

        repository.logout()

        // Verify tokens were cleared
        assertNull(mockTokenStorage.getAccessToken())
        assertNull(mockTokenStorage.getRefreshToken())
        assertFalse(mockTokenStorage.isAuthenticated())
    }

    // ========== Authentication Status Tests ==========

    @Test
    fun `isAuthenticated should return true when access token exists`() = runTest {
        mockTokenStorage.saveAccessToken("access_token")

        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request")
        }

        repository = createRepository(mockEngine)

        assertTrue(repository.isAuthenticated())
    }

    @Test
    fun `isAuthenticated should return false when no access token`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request")
        }

        repository = createRepository(mockEngine)

        assertFalse(repository.isAuthenticated())
    }

    @Test
    fun `getAccessToken should return token when available`() = runTest {
        mockTokenStorage.saveAccessToken("test_access_token")

        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request")
        }

        repository = createRepository(mockEngine)

        assertEquals("test_access_token", repository.getAccessToken())
    }

    @Test
    fun `getAccessToken should return null when not available`() = runTest {
        val mockEngine = MockEngine { request ->
            fail("Should not make HTTP request")
        }

        repository = createRepository(mockEngine)

        assertNull(repository.getAccessToken())
    }

    // Helper function to create repository with mock engine
    private fun createRepository(mockEngine: MockEngine): AuthRepository {
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url("http://localhost:8080")
            }
        }

        return AuthRepository(
            tokenStorage = mockTokenStorage,
            httpClient = client
        )
    }
}

/**
 * Mock implementation of TokenStorage for testing
 */
class MockTokenStorage : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    
    override suspend fun saveAccessToken(token: String) {
        accessToken = token
    }
    
    override suspend fun saveRefreshToken(token: String) {
        refreshToken = token
    }
    
    override suspend fun getAccessToken(): String? = accessToken
    
    override suspend fun getRefreshToken(): String? = refreshToken
    
    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return accessToken != null
    }
}

