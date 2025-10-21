package data

import domain.model.auth.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Repository for authentication operations
 */
class AuthRepository(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String = "http://localhost:8080"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }
    }

    /**
     * Register a new user
     * @return OtpSentResponse with OTP details
     */
    suspend fun register(request: RegisterRequest): Result<OtpSentResponse> {
        return try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<OtpSentResponse>())
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login existing user (sends OTP)
     * @return OtpSentResponse with OTP details
     */
    suspend fun login(identifier: String, identifierType: String): Result<OtpSentResponse> {
        return try {
            val request = LoginRequest(
                email = if (identifierType == "email") identifier else null,
                phone = if (identifierType == "phone") identifier else null
            )
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<OtpSentResponse>())
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify OTP code
     * @return AuthResponse with tokens and user info
     */
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                // Save tokens
                tokenStorage.saveAccessToken(authResponse.accessToken)
                tokenStorage.saveRefreshToken(authResponse.refreshToken)
                Result.success(authResponse)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with Google
     * @return AuthResponse with tokens and user info
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/auth/login/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleLoginRequest(idToken))
            }
            
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                // Save tokens
                tokenStorage.saveAccessToken(authResponse.accessToken)
                tokenStorage.saveRefreshToken(authResponse.refreshToken)
                Result.success(authResponse)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refresh access token
     * @return TokenResponse with new tokens
     */
    suspend fun refreshToken(): Result<TokenResponse> {
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))
            
            val response = client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }
            
            if (response.status.isSuccess()) {
                val tokenResponse = response.body<TokenResponse>()
                // Save new tokens
                tokenStorage.saveAccessToken(tokenResponse.accessToken)
                tokenStorage.saveRefreshToken(tokenResponse.refreshToken)
                Result.success(tokenResponse)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user (clear tokens)
     */
    suspend fun logout() {
        tokenStorage.clearTokens()
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return tokenStorage.isAuthenticated()
    }

    /**
     * Get current access token
     */
    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }

    /**
     * Close HTTP client
     */
    fun close() {
        client.close()
    }
}

