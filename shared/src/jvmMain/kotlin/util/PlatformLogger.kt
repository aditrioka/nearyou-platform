package util

import org.slf4j.LoggerFactory

/**
 * JVM implementation of Logger using SLF4J
 * This will use Logback configuration from server/src/main/resources/logback.xml
 */
private class JVMLogger : Logger {
    private val logger = LoggerFactory.getLogger("NearYouApp")

    override fun debug(tag: String, message: String) {
        logger.debug("[$tag] $message")
    }

    override fun info(tag: String, message: String) {
        logger.info("[$tag] $message")
    }

    override fun warn(tag: String, message: String) {
        logger.warn("[$tag] $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.error("[$tag] $message", throwable)
        } else {
            logger.error("[$tag] $message")
        }
    }
}

actual fun createPlatformLogger(): Logger = JVMLogger()

