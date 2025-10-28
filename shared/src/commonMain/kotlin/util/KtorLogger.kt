package util

import io.ktor.client.plugins.logging.Logger

/**
 * Custom Ktor Logger that bridges to our AppLogger
 * This ensures HTTP logs appear in platform-specific logs (Android Logcat, iOS Console, etc.)
 */
object KtorLogger : Logger {
    private const val TAG = "HttpClient"
    
    override fun log(message: String) {
        AppLogger.debug(TAG, message)
    }
}

