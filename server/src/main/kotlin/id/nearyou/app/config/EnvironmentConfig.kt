package id.nearyou.app.config

/**
 * Environment configuration for the NearYou ID server
 * Loads configuration from environment variables with sensible defaults
 */
object EnvironmentConfig {
    
    // Database Configuration
    val databaseUrl: String = System.getenv("DATABASE_URL") 
        ?: "jdbc:postgresql://localhost:5433/nearyou_db"
    val databaseUser: String = System.getenv("DATABASE_USER") 
        ?: "nearyou_user"
    val databasePassword: String = System.getenv("DATABASE_PASSWORD") 
        ?: "nearyou_password"
    
    // Redis Configuration
    val redisUrl: String = System.getenv("REDIS_URL")
        ?: "redis://:nearyou_redis_password@localhost:6379"
    
    // JWT Configuration
    val jwtSecret: String = System.getenv("JWT_SECRET") 
        ?: "your-secret-key-change-in-production"
    val jwtIssuer: String = System.getenv("JWT_ISSUER") 
        ?: "nearyou-id"
    val jwtAudience: String = System.getenv("JWT_AUDIENCE") 
        ?: "nearyou-api"
    val jwtRealm: String = System.getenv("JWT_REALM") 
        ?: "nearyou"
    
    // Token Expiry (in milliseconds)
    val accessTokenExpiry: Long = 7 * 24 * 60 * 60 * 1000L // 7 days
    val refreshTokenExpiry: Long = 30 * 24 * 60 * 60 * 1000L // 30 days
    
    // OTP Configuration
    val otpProvider: String = System.getenv("OTP_PROVIDER") 
        ?: "mock" // mock, twilio, sendgrid
    val otpExpiry: Long = 5 * 60 * 1000L // 5 minutes
    val otpLength: Int = 6
    
    // Rate Limiting
    val otpRateLimit: Int = 5 // requests per hour
    val otpRateLimitWindow: Long = 60 * 60 * 1000L // 1 hour
    
    // Server Configuration
    val serverPort: Int = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 8080
    val serverHost: String = System.getenv("SERVER_HOST") ?: "0.0.0.0"
    
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

