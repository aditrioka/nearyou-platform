package id.nearyou.app.config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

/**
 * Environment configuration for the NearYou ID server
 * Loads configuration from .env file (if exists) or environment variables with sensible defaults
 */
object EnvironmentConfig {

    // Load .env file if it exists, otherwise use system environment
    private val dotenv: Dotenv? = try {
        dotenv {
            ignoreIfMissing = true
            systemProperties = false
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Get environment variable from .env file or system environment
     * Priority: .env file > system environment > default value
     */
    private fun getEnv(key: String, default: String? = null): String? {
        return dotenv?.get(key) ?: System.getenv(key) ?: default
    }

    // Database Configuration
    val databaseUrl: String = getEnv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5433/nearyou_db"
    val databaseUser: String = getEnv("DATABASE_USER")
        ?: "nearyou_user"
    val databasePassword: String = getEnv("DATABASE_PASSWORD")
        ?: "nearyou_password"

    // Redis Configuration
    val redisUrl: String = getEnv("REDIS_URL")
        ?: "redis://:nearyou_redis_password@localhost:6379"

    // JWT Configuration
    val jwtSecret: String = getEnv("JWT_SECRET")
        ?: "your-secret-key-change-in-production"
    val jwtIssuer: String = getEnv("JWT_ISSUER")
        ?: "nearyou-id"
    val jwtAudience: String = getEnv("JWT_AUDIENCE")
        ?: "nearyou-api"
    val jwtRealm: String = getEnv("JWT_REALM")
        ?: "nearyou"

    // Token Expiry (in milliseconds)
    val accessTokenExpiry: Long = 7 * 24 * 60 * 60 * 1000L // 7 days
    val refreshTokenExpiry: Long = 30 * 24 * 60 * 60 * 1000L // 30 days

    // OTP Configuration
    val otpProvider: String = getEnv("OTP_PROVIDER")
        ?: "mock" // mock, twilio, sendgrid
    val otpExpiry: Long = 5 * 60 * 1000L // 5 minutes
    val otpLength: Int = 6

    // Rate Limiting
    val otpRateLimit: Int = 5 // requests per hour
    val otpRateLimitWindow: Long = 60 * 60 * 1000L // 1 hour

    // Server Configuration
    val serverPort: Int = getEnv("SERVER_PORT")?.toIntOrNull() ?: 8080
    val serverHost: String = getEnv("SERVER_HOST") ?: "0.0.0.0"
    val baseUrl: String = getEnv("BASE_URL") ?: "http://localhost:$serverPort"

    // File Upload Configuration
    val uploadDir: String = getEnv("UPLOAD_DIR") ?: "uploads"
    val maxFileSize: Long = 5 * 1024 * 1024 // 5MB

    // BCrypt Configuration
    val bcryptRounds: Int = 12
    
    /**
     * Validate that all required configuration is present
     * Throws IllegalStateException if critical configuration is missing
     */
    fun validate() {
        require(jwtSecret != "your-secret-key-change-in-production" || otpProvider == "mock") {
            "JWT_SECRET must be set in production environment"
        }
        require(databaseUrl.isNotBlank()) { "DATABASE_URL must be set" }
        require(databaseUser.isNotBlank()) { "DATABASE_USER must be set" }
        require(databasePassword.isNotBlank()) { "DATABASE_PASSWORD must be set" }
    }
    
    /**
     * Print configuration summary (without sensitive data)
     */
    fun printSummary() {
        println("""
            |=== NearYou ID Server Configuration ===
            |Server: $serverHost:$serverPort
            |Database: ${databaseUrl.substringBefore("?")}
            |Redis: ${redisUrl.substringBefore("@").substringAfter("//").let { if (it.contains(":")) "redis://***:***@${redisUrl.substringAfter("@")}" else redisUrl }}
            |JWT Issuer: $jwtIssuer
            |JWT Audience: $jwtAudience
            |OTP Provider: $otpProvider
            |Access Token Expiry: ${accessTokenExpiry / (24 * 60 * 60 * 1000)} days
            |Refresh Token Expiry: ${refreshTokenExpiry / (24 * 60 * 60 * 1000)} days
            |OTP Expiry: ${otpExpiry / 60 / 1000} minutes
            |OTP Rate Limit: $otpRateLimit requests per hour
            |=======================================
        """.trimMargin())
    }
}

