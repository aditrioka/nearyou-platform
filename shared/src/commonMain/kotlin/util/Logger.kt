package util

/**
 * Multiplatform logger interface
 * Platform-specific implementations will handle actual logging
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Logger configuration
 */
object LoggerConfig {
    /**
     * Minimum log level to display
     * DEBUG = 0, INFO = 1, WARN = 2, ERROR = 3
     */
    var minLogLevel: LogLevel = LogLevel.INFO
    
    /**
     * Whether to log sensitive data (tokens, passwords, etc.)
     * Should ALWAYS be false in production
     */
    var logSensitiveData: Boolean = false
    
    enum class LogLevel(val value: Int) {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3)
    }
}

/**
 * Platform-specific logger factory
 * Implemented in each platform (Android, iOS, JVM)
 */
expect fun createPlatformLogger(): Logger

/**
 * Convenience logger object for easy access
 */
object AppLogger {
    private val logger: Logger = createPlatformLogger()
    
    fun debug(tag: String, message: String) {
        if (LoggerConfig.minLogLevel.value <= LoggerConfig.LogLevel.DEBUG.value) {
            logger.debug(tag, message)
        }
    }
    
    fun info(tag: String, message: String) {
        if (LoggerConfig.minLogLevel.value <= LoggerConfig.LogLevel.INFO.value) {
            logger.info(tag, message)
        }
    }
    
    fun warn(tag: String, message: String) {
        if (LoggerConfig.minLogLevel.value <= LoggerConfig.LogLevel.WARN.value) {
            logger.warn(tag, message)
        }
    }
    
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (LoggerConfig.minLogLevel.value <= LoggerConfig.LogLevel.ERROR.value) {
            logger.error(tag, message, throwable)
        }
    }
    
    /**
     * Log sensitive data (only if enabled in config)
     * Use this for tokens, passwords, etc.
     */
    fun debugSensitive(tag: String, message: String) {
        if (LoggerConfig.logSensitiveData) {
            debug(tag, "[SENSITIVE] $message")
        } else {
            debug(tag, "[SENSITIVE DATA REDACTED]")
        }
    }
}

