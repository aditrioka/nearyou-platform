package id.nearyou.app.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import domain.model.SubscriptionTier
import id.nearyou.app.config.EnvironmentConfig
import java.util.*

/**
 * JWT configuration and token generation utilities
 */
object JwtConfig {
    
    private val algorithm = Algorithm.HMAC256(EnvironmentConfig.jwtSecret)
    
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(EnvironmentConfig.jwtIssuer)
        .withAudience(EnvironmentConfig.jwtAudience)
        .build()
    
    /**
     * Generate an access token for a user
     *
     * @param userId The user's unique identifier
     * @param subscriptionTier The user's subscription tier (FREE, PREMIUM)
     * @return JWT access token string
     */
    fun generateAccessToken(userId: String, subscriptionTier: SubscriptionTier): String {
        val now = Date()
        val expiresAt = Date(now.time + EnvironmentConfig.accessTokenExpiry)

        return JWT.create()
            .withIssuer(EnvironmentConfig.jwtIssuer)
            .withAudience(EnvironmentConfig.jwtAudience)
            .withSubject(userId)
            .withClaim("subscription_tier", subscriptionTier.name)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    /**
     * Generate a refresh token
     * 
     * @param userId The user's unique identifier
     * @return JWT refresh token string
     */
    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiresAt = Date(now.time + EnvironmentConfig.refreshTokenExpiry)
        
        return JWT.create()
            .withIssuer(EnvironmentConfig.jwtIssuer)
            .withAudience(EnvironmentConfig.jwtAudience)
            .withSubject(userId)
            .withClaim("type", "refresh")
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    /**
     * Extract user ID from a JWT token
     * 
     * @param token The JWT token string
     * @return User ID or null if invalid
     */
    fun getUserIdFromToken(token: String): String? {
        return try {
            val decodedJWT = verifier.verify(token)
            decodedJWT.subject
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validate a JWT token
     * 
     * @param token The JWT token string
     * @return true if valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}

