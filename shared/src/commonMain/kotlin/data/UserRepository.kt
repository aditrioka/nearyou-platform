package data

import domain.model.User
import domain.model.UpdateUserRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import util.AppLogger
import util.LoggerConfig

/**
 * Repository for user profile operations
 *
 * @param tokenStorage Storage for access and refresh tokens
 * @param baseUrl Base URL for the API
 * @param httpClient Optional HTTP client for testing (if null, creates default client)
 */
class UserRepository(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String = "http://localhost:8080",
    httpClient: HttpClient? = null
) {
    companion object {
        private const val TAG = "UserRepository"
    }
    private val client = httpClient ?: HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = util.KtorLogger  // Use our custom logger that outputs to platform logs
            // Use BODY in development, INFO in production
            level = if (LoggerConfig.logSensitiveData) LogLevel.BODY else LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    /**
     * Parse error response from API
     */
    private suspend fun parseErrorMessage(response: HttpResponse): String {
        return try {
            val errorResponse = response.body<ApiErrorResponse>()
            errorResponse.message
        } catch (e: Exception) {
            response.status.description
        }
    }

    /**
     * Get current user's profile
     * @return User profile or error
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            AppLogger.debug(TAG, "Fetching current user profile")

            val accessToken = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = client.get("$baseUrl/users/me") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                AppLogger.info(TAG, "User profile fetched: ${user.username}")
                Result.success(user)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Failed to fetch user profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Error fetching user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update current user's profile
     * @param request Update request with optional fields
     * @return Updated user profile or error
     */
    suspend fun updateCurrentUser(request: UpdateUserRequest): Result<User> {
        return try {
            AppLogger.info(TAG, "Updating user profile")

            val accessToken = tokenStorage.getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = client.put("$baseUrl/users/me") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                AppLogger.info(TAG, "User profile updated successfully: ${user.username}")
                Result.success(user)
            } else {
                val errorMessage = parseErrorMessage(response)
                AppLogger.error(TAG, "Failed to update user profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            AppLogger.error(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}

