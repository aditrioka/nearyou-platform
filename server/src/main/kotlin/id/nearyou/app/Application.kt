package id.nearyou.app

import id.nearyou.app.auth.AuthService
import id.nearyou.app.auth.authRoutes
import id.nearyou.app.config.DatabaseConfig
import id.nearyou.app.config.EnvironmentConfig
import id.nearyou.app.plugins.configureAuthentication
import id.nearyou.app.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
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
    // Configure plugins
    configureSerialization()
    configureAuthentication()

    // Initialize services
    val authService = AuthService()

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

        // Auth routes
        authRoutes(authService)
    }

    // Shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        authService.close()
    }
}