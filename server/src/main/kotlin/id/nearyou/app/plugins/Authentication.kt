package id.nearyou.app.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import id.nearyou.app.config.EnvironmentConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

/**
 * Configure JWT authentication
 */
fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = EnvironmentConfig.jwtRealm
            
            verifier(
                JWT
                    .require(Algorithm.HMAC256(EnvironmentConfig.jwtSecret))
                    .withIssuer(EnvironmentConfig.jwtIssuer)
                    .withAudience(EnvironmentConfig.jwtAudience)
                    .build()
            )
            
            validate { credential ->
                // Validate that the token has required claims
                if (credential.payload.subject != null &&
                    credential.payload.getClaim("subscription_tier").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            
            challenge { _, _ ->
                call.respond(
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    mapOf("error" to "invalid_token", "message" to "Token is not valid or has expired")
                )
            }
        }
    }
}

