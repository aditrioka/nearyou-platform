package di

import data.AuthRepository
import data.UserRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import util.AppConfig

/**
 * Platform-specific module - implemented in each platform
 */
expect val platformModule: Module

/**
 * Common DI module for shared dependencies
 */
val sharedModule = module {
    // Shared HttpClient instance
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        if (AppConfig.isDebug) {
                            println("HTTP Client: $message")
                        }
                    }
                }
                level = if (AppConfig.isDebug) LogLevel.ALL else LogLevel.NONE
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }

            defaultRequest {
                url(AppConfig.baseUrl)
            }
        }
    }

    // Repositories depend on TokenStorage (platform-specific) and HttpClient (shared)
    single { AuthRepository(get(), get()) }
    single { UserRepository(get(), get()) }
}

