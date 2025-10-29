package id.nearyou.app.ui.auth

import app.cash.turbine.test
import data.AuthRepository
import data.TokenStorage
import domain.model.User
import domain.model.SubscriptionTier
import domain.model.auth.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Comprehensive tests for AuthViewModel
 * Tests StateFlow state management and all authentication operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var mockTokenStorage: MockTokenStorage
    private lateinit var mockEngine: MockEngine
    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTokenStorage = MockTokenStorage()
        // Default mock engine - will be overridden in tests that need specific responses
        mockEngine = MockEngine { request ->
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        repository = createRepository(mockEngine)
        viewModel = AuthViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        // HttpClient is now managed by DI, no need to close repository
    }

    private fun createRepository(engine: MockEngine): AuthRepository {
        val client = HttpClient(engine) {
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
    
    // ========== Initial State Tests ==========

    @Test
    fun `initial state should check authentication status when authenticated`() = runTest {
        // Setup: Save token to make repository return authenticated
        mockTokenStorage.saveAccessToken("test_token")

        // Create new repository and ViewModel
        val repo = createRepository(mockEngine)
        val vm = AuthViewModel(repo)

        // Advance coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state
        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAuthenticated)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Test
    fun `initial state should show not authenticated when no token`() = runTest {
        // Setup: No token in storage
        mockTokenStorage.clearTokens()

        // Create new repository and ViewModel
        val repo = createRepository(mockEngine)
        val vm = AuthViewModel(repo)

        // Advance coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state
        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isAuthenticated)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }
    
    // ========== Check Auth Status Tests ==========

    @Test
    fun `checkAuthStatus should update state correctly`() = runTest {
        // Save token
        mockTokenStorage.saveAccessToken("test_token")

        viewModel.uiState.test {
            // Skip initial state
            skipItems(1)

            // Call checkAuthStatus
            viewModel.checkAuthStatus()
            testDispatcher.scheduler.advanceUntilIdle()

            // Verify loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Verify final state
            val finalState = awaitItem()
            assertTrue(finalState.isAuthenticated)
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
        }
    }
    
    // ========== Register Tests ==========

    @Test
    fun `register with email should call repository correctly`() = runTest {
        // Setup mock engine to return success
        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    OtpSentResponse(
                        message = "OTP sent",
                        identifier = "test@example.com",
                        type = "email",
                        expiresInSeconds = 300
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repo = createRepository(engine)
        val vm = AuthViewModel(repo)

        vm.uiState.test {
            // Skip initial state and checkAuthStatus completion
            // Initial state has isLoading = true, checkAuthStatus sets it to false
            skipItems(2)

            // Set the state
            vm.updateUsername("testuser")
            val usernameState = awaitItem()
            assertEquals("testuser", usernameState.username)

            vm.updateIdentifier("test@example.com")
            val identifierState = awaitItem()
            assertEquals("test@example.com", identifierState.identifier)

            // Call register
            vm.register()

            // Wait for loading state
            val registerLoadingState = awaitItem()
            assertTrue(registerLoadingState.isLoading)

            // Wait for final state after register
            val finalState = awaitItem()
            assertFalse(finalState.isLoading, "Expected isLoading to be false but was: ${finalState.isLoading}")
            assertEquals(60, finalState.otpTimeRemaining)
            assertFalse(finalState.canResendOtp)
        }
    }
    
    // ========== Login Tests ==========

    @Test
    fun `login should call repository correctly`() = runTest {
        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    OtpSentResponse(
                        message = "OTP sent",
                        identifier = "test@example.com",
                        type = "email",
                        expiresInSeconds = 300
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repo = createRepository(engine)
        val vm = AuthViewModel(repo)

        vm.uiState.test {
            // Skip initial state and checkAuthStatus completion
            skipItems(2)

            // Set the state
            vm.updateIdentifier("test@example.com")
            val identifierState = awaitItem()
            assertEquals("test@example.com", identifierState.identifier)

            // Call login
            vm.login()

            // Wait for loading state
            val loginLoadingState = awaitItem()
            assertTrue(loginLoadingState.isLoading)

            // Wait for final state after login
            val finalState = awaitItem()
            assertFalse(finalState.isLoading, "Expected isLoading to be false but was: ${finalState.isLoading}")
            assertEquals(60, finalState.otpTimeRemaining)
            assertFalse(finalState.canResendOtp)
        }
    }
    
    // ========== Verify OTP Tests ==========

    @Test
    fun `verifyOtp should update state to authenticated on success`() = runTest {
        val mockUser = createMockUser()

        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    AuthResponse(
                        accessToken = "access_token",
                        refreshToken = "refresh_token",
                        user = mockUser
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repo = createRepository(engine)
        val vm = AuthViewModel(repo)

        vm.uiState.test {
            // Skip initial state and checkAuthStatus completion
            skipItems(2)

            // Set OTP code in state
            vm.updateOtpCode("123456")
            val otpState = awaitItem()
            assertEquals("123456", otpState.otpCode)

            // Call verifyOtp
            vm.verifyOtp(
                identifier = "test@example.com",
                identifierType = "email"
            )

            // Wait for loading state
            val verifyLoadingState = awaitItem()
            assertTrue(verifyLoadingState.isLoading)

            // Wait for final authenticated state
            val finalState = awaitItem()
            assertTrue(finalState.isAuthenticated, "Expected state to be authenticated but was: $finalState")
            assertFalse(finalState.isLoading)
        }
    }
    
    // ========== Logout Tests ==========

    @Test
    fun `logout should update state to not authenticated`() = runTest {
        // Setup: make user authenticated first
        mockTokenStorage.saveAccessToken("test_token")

        // Advance past init
        testDispatcher.scheduler.advanceUntilIdle()

        // Call logout
        viewModel.logout()

        // Advance to complete the coroutine
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state was updated
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isAuthenticated, "Expected state to be not authenticated but was: $state")
        }
    }
}

/**
 * Helper function to create a mock user for testing
 */
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

/**
 * Mock TokenStorage for testing
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

    override suspend fun isAuthenticated(): Boolean = accessToken != null
}

