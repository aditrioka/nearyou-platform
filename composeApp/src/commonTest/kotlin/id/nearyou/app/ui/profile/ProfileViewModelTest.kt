package id.nearyou.app.ui.profile

import app.cash.turbine.test
import data.TokenStorage
import data.UserRepository
import domain.model.SubscriptionTier
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Serializable
private data class ProfileErrorResponse(
    val error: String,
    val message: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var mockTokenStorage: MockTokenStorage
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
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== loadProfile Tests ==========

    @Test
    fun `init should load user profile`() = runTest {
        val mockUser = createMockUser()
        mockTokenStorage.saveAccessToken("test_token")

        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(mockUser),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val vm = createViewModel(engine)

        vm.uiState.test {
            // Initial state
            val initial = awaitItem()

            // Advance to complete loadProfile
            testDispatcher.scheduler.advanceUntilIdle()

            val next = awaitItem()
            if (next.isLoading) {
                val loaded = awaitItem()
                assertNotNull(loaded.user)
                assertEquals("testuser", loaded.user?.username)
                assertFalse(loaded.isLoading)
            } else {
                assertNotNull(next.user)
                assertEquals("testuser", next.user?.username)
                assertFalse(next.isLoading)
            }
        }
    }

    @Test
    fun `loadProfile should show error on failure`() = runTest {
        mockTokenStorage.saveAccessToken("test_token")

        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    ProfileErrorResponse(error = "UNAUTHORIZED", message = "Invalid token")
                ),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val vm = createViewModel(engine)

        vm.uiState.test {
            val initial = awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()

            val next = awaitItem()
            if (next.isLoading) {
                val loaded = awaitItem()
                assertNull(loaded.user)
                assertNotNull(loaded.error)
                assertFalse(loaded.isLoading)
            } else {
                assertNull(next.user)
                assertNotNull(next.error)
                assertFalse(next.isLoading)
            }
        }
    }

    // ========== updateProfile Tests ==========

    @Test
    fun `updateProfile should update state on success`() = runTest {
        val mockUser = createMockUser()
        val updatedUser = mockUser.copy(displayName = "New Name")
        mockTokenStorage.saveAccessToken("test_token")

        val engine = MockEngine { request ->
            if (request.method == HttpMethod.Get) {
                respond(
                    content = json.encodeToString(mockUser),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(
                    content = json.encodeToString(updatedUser),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val vm = createViewModel(engine)

        vm.uiState.test {
            // Skip initial state
            skipItems(1)
            // Advance init loadProfile
            testDispatcher.scheduler.advanceUntilIdle()
            // Skip loadProfile result (may be loading + result)
            val initResult = awaitItem()
            if (initResult.isLoading) awaitItem()

            vm.updateProfile(displayName = "New Name")
            testDispatcher.scheduler.advanceUntilIdle()

            // Skip loading state if emitted
            val updateResult = awaitItem()
            val state = if (updateResult.isLoading) awaitItem() else updateResult

            assertEquals("New Name", state.user?.displayName)
            assertTrue(state.updateSuccess)
            assertFalse(state.isEditing)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `updateProfile should show error on failure`() = runTest {
        val mockUser = createMockUser()
        mockTokenStorage.saveAccessToken("test_token")

        var requestCount = 0
        val engine = MockEngine { request ->
            requestCount++
            if (requestCount == 1) {
                respond(
                    content = json.encodeToString(mockUser),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(
                    content = json.encodeToString(
                        ProfileErrorResponse(error = "VALIDATION_ERROR", message = "Invalid name")
                    ),
                    status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val vm = createViewModel(engine)

        vm.uiState.test {
            // Skip initial state
            skipItems(1)
            // Advance init loadProfile
            testDispatcher.scheduler.advanceUntilIdle()
            val initResult = awaitItem()
            if (initResult.isLoading) awaitItem()

            vm.updateProfile(displayName = "")
            testDispatcher.scheduler.advanceUntilIdle()

            val updateResult = awaitItem()
            val state = if (updateResult.isLoading) awaitItem() else updateResult

            assertNotNull(state.error)
            assertFalse(state.updateSuccess)
            assertFalse(state.isLoading)
        }
    }

    // ========== Editing State Tests ==========

    @Test
    fun `startEditing should set isEditing to true`() = runTest {
        val vm = createDefaultViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.startEditing()

        vm.uiState.test {
            assertTrue(awaitItem().isEditing)
        }
    }

    @Test
    fun `cancelEditing should set isEditing to false`() = runTest {
        val vm = createDefaultViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.startEditing()
        vm.cancelEditing()

        vm.uiState.test {
            assertFalse(awaitItem().isEditing)
        }
    }

    // ========== Clear State Tests ==========

    @Test
    fun `clearError should reset error`() = runTest {
        mockTokenStorage.saveAccessToken("test_token")

        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(
                    ProfileErrorResponse(error = "ERROR", message = "Something failed")
                ),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val vm = createViewModel(engine)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.clearError()

        vm.uiState.test {
            assertNull(awaitItem().error)
        }
    }

    @Test
    fun `clearSuccess should reset updateSuccess`() = runTest {
        val mockUser = createMockUser()
        mockTokenStorage.saveAccessToken("test_token")

        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(mockUser),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val vm = createViewModel(engine)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.updateProfile(displayName = "New")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.clearSuccess()

        vm.uiState.test {
            assertFalse(awaitItem().updateSuccess)
        }
    }

    // ========== Helpers ==========

    private suspend fun createDefaultViewModel(): ProfileViewModel {
        mockTokenStorage.saveAccessToken("test_token")
        val engine = MockEngine { request ->
            respond(
                content = json.encodeToString(createMockUser()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return createViewModel(engine)
    }

    private fun createViewModel(engine: MockEngine): ProfileViewModel {
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url("http://localhost:8080")
            }
        }
        val repository = UserRepository(
            tokenStorage = mockTokenStorage,
            httpClient = client
        )
        return ProfileViewModel(repository)
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
