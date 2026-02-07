package data

/**
 * JVM implementation of TokenStorage (for testing/development)
 * Uses in-memory storage - NOT SECURE, only for testing
 */
class TokenStorageJVM : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun saveAccessToken(token: String) {
        accessToken = token
    }

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    override suspend fun isAuthenticated(): Boolean = accessToken != null && refreshToken != null
}
