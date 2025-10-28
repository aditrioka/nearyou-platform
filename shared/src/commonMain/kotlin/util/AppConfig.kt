package util

/**
 * Application configuration
 * Set up logging and other app-wide settings based on environment
 */
object AppConfig {

    /**
     * Base URL for API requests
     * TODO: Make this configurable per environment
     */
    const val baseUrl = "http://localhost:8080"

    /**
     * Initialize app configuration
     * Call this at app startup
     *
     * @param isDevelopment Whether the app is running in development mode
     */
    fun initialize(isDevelopment: Boolean = false) {
        if (isDevelopment) {
            // Development mode: verbose logging, show sensitive data
            LoggerConfig.minLogLevel = LoggerConfig.LogLevel.DEBUG
            LoggerConfig.logSensitiveData = true
            AppLogger.info("AppConfig", "🔧 Running in DEVELOPMENT mode")
            AppLogger.warn("AppConfig", "⚠️ Sensitive data logging is ENABLED")
        } else {
            // Production mode: minimal logging, hide sensitive data
            LoggerConfig.minLogLevel = LoggerConfig.LogLevel.INFO
            LoggerConfig.logSensitiveData = false
            AppLogger.info("AppConfig", "🚀 Running in PRODUCTION mode")
        }
    }
    
    /**
     * Check if running in development mode
     */
    fun isDevelopment(): Boolean {
        return LoggerConfig.logSensitiveData
    }
}

