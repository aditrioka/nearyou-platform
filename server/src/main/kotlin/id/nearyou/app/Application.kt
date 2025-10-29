package id.nearyou.app

import id.nearyou.app.auth.authRoutes
import id.nearyou.app.user.userRoutes
import id.nearyou.app.post.postRoutes
import id.nearyou.app.config.DatabaseConfig
import id.nearyou.app.config.EnvironmentConfig
import id.nearyou.app.di.serverModule
import id.nearyou.app.plugins.configureAuthentication
import id.nearyou.app.plugins.configureErrorHandling
import id.nearyou.app.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import util.AppConfig

fun main() {
    // Initialize logging configuration
    val isDevelopment = System.getenv("ENVIRONMENT") != "production"
    AppConfig.initialize(isDevelopment = isDevelopment)

    // Validate and print configuration
    EnvironmentConfig.validate()
    EnvironmentConfig.printSummary()

    // Initialize database
    DatabaseConfig.init()

    // Start server
    embeddedServer(
        Netty,
        port = EnvironmentConfig.serverPort,
        host = EnvironmentConfig.serverHost,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Install Koin for dependency injection
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }

    // Configure plugins
    configureErrorHandling()  // Must be installed early to catch errors from other plugins
    configureSerialization()
    configureAuthentication()

    // Configure routing
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("NearYou ID API - Running")
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "healthy",
                "service" to "nearyou-id-api",
                "version" to "1.0.0"
            ))
        }

        // Auth routes (services injected via Koin)
        authRoutes()

        // User routes (services injected via Koin)
        userRoutes()

        // Post routes (services injected via Koin)
        postRoutes()
    }

    // Shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        try {
            // Get Koin instance
            val koin = getKoin()

            // Close Redis connection
            koin.get<StatefulRedisConnection<String, String>>().close()
            koin.get<RedisClient>().shutdown()

            // Close database connection pool
            DatabaseConfig.close()

            println("✓ Server shutdown complete")
        } catch (e: Exception) {
            println("Error during shutdown: ${e.message}")
        }
    }
}