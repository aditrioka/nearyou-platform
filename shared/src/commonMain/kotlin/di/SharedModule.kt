package di

import data.AuthRepository
import data.TokenStorage
import data.UserRepository
import domain.model.auth.RefreshTokenRequest
import domain.model.auth.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import util.AppConfig
import util.AppLogger

/**
 * Platform-specific module - implemented in each platform
 */
expect val platformModule: Module

/**
 * Common DI module for shared dependencies
 */
val sharedModule = module {
    // Shared HttpClient instance with automatic token refresh
    single {
        val tokenStorage = get<TokenStorage>()

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

            // Automatic token refresh on 401
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenStorage.getAccessToken()
                        val refreshToken = tokenStorage.getRefreshToken()

                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenStorage.getRefreshToken()
                            ?: return@refreshTokens null

                        try {
                            AppLogger.info("HttpClient", "Refreshing access token")

                            // Create a separate client for refresh to avoid recursion
                            val refreshClient = HttpClient {
                                install(ContentNegotiation) {
                                    json(Json {
                                        ignoreUnknownKeys = true
                                    })
                                }
                            }

                            val response = refreshClient.post("${AppConfig.baseUrl}/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken))
                            }

                            refreshClient.close()

                            if (response.status.isSuccess()) {
                                val tokenResponse = response.body<TokenResponse>()

                                // Save new tokens
                                tokenStorage.saveAccessToken(tokenResponse.accessToken)
                                tokenStorage.saveRefreshToken(tokenResponse.refreshToken)

                                AppLogger.info("HttpClient", "Token refresh successful")

                                BearerTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                            } else {
                                AppLogger.error("HttpClient", "Token refresh failed: ${response.status}")
                                // Clear tokens on refresh failure
                                tokenStorage.clearTokens()
                                null
                            }
                        } catch (e: Exception) {
                            AppLogger.error("HttpClient", "Token refresh error: ${e.message}")
                            tokenStorage.clearTokens()
                            null
                        }
                    }
                }
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

