package data

/**
 * Interface for secure token storage
 * Platform-specific implementations use Keystore (Android) and Keychain (iOS)
 */
interface TokenStorage {
    /**
     * Save access token securely
     */
    suspend fun saveAccessToken(token: String)

    /**
     * Get access token
     */
    suspend fun getAccessToken(): String?

    /**
     * Save refresh token securely
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String?

    /**
     * Clear all tokens (logout)
     */
    suspend fun clearTokens()

    /**
     * Check if user is authenticated (has valid tokens)
     */
    suspend fun isAuthenticated(): Boolean
}

/**
 * Expect declaration for platform-specific token storage
 */
expect fun createTokenStorage(): TokenStorage

