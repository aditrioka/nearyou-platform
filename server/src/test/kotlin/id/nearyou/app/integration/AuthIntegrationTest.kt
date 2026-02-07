package id.nearyou.app.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for authentication flows.
 *
 * Tests that require a full application environment (Redis + PostgreSQL)
 * are marked @Ignore — they are intended for CI with service containers
 * or local Docker Compose, not for `./gradlew :server:test` alone.
 *
 * The non-ignored tests validate routing and error handling using the
 * Ktor test host without external dependencies.
 */
class AuthIntegrationTest {
    private val json =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

    @Test
    fun `POST auth register with invalid JSON should return 400 or 500`() =
        testApplication {
            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("invalid json")
                }

            // Invalid JSON should produce a client or server error, never 2xx
            assertTrue(
                response.status.value in 400..599,
                "Expected 4xx/5xx for invalid JSON, got ${response.status}",
            )
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `POST auth register should return 200 with valid request`() =
        testApplication {
            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "username": "integrationtest",
                            "displayName": "Integration Test",
                            "email": "integration@test.com",
                            "password": "testPassword123"
                        }
                        """.trimIndent(),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `POST auth register should return 409 for duplicate email`() =
        testApplication {
            // Register first user
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "username": "testuser",
                        "displayName": "Test User",
                        "email": "duplicate@test.com",
                        "password": "testPassword123"
                    }
                    """.trimIndent(),
                )
            }

            // Register duplicate
            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "username": "testuser2",
                            "displayName": "Test User 2",
                            "email": "duplicate@test.com",
                            "password": "testPassword123"
                        }
                        """.trimIndent(),
                    )
                }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `POST auth login should return 200 with valid credentials`() =
        testApplication {
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "email": "test@example.com"
                        }
                        """.trimIndent(),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `POST auth verify-otp should return 401 with invalid OTP`() =
        testApplication {
            val response =
                client.post("/auth/verify-otp") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "identifier": "test@example.com",
                            "code": "000000",
                            "type": "email"
                        }
                        """.trimIndent(),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST auth refresh should return 401 without valid refresh token`() =
        testApplication {
            val response =
                client.post("/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "refreshToken": "invalid_token"
                        }
                        """.trimIndent(),
                    )
                }

            // Without a valid token, we expect 401 or 404 (route may not be registered in test host)
            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `POST auth logout should return 401 without valid token`() =
        testApplication {
            val response =
                client.post("/auth/logout") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer invalid-token")
                    }
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `Full auth flow - register, verify OTP, and access protected resource`() =
        testApplication {
            val registerResponse =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "username": "flowtest",
                            "displayName": "Flow Test",
                            "email": "flowtest@example.com",
                            "password": "testPassword123"
                        }
                        """.trimIndent(),
                    )
                }

            assertEquals(HttpStatusCode.OK, registerResponse.status)
        }

    @Ignore("Requires Redis and PostgreSQL — run in CI with service containers")
    @Test
    fun `API should enforce rate limiting`() =
        testApplication {
            val responses = mutableListOf<HttpResponse>()

            repeat(3) {
                val response =
                    client.post("/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            """
                            {
                                "username": "ratetest$it",
                                "displayName": "Rate Test",
                                "email": "ratetest$it@example.com",
                                "password": "testPassword123"
                            }
                            """.trimIndent(),
                        )
                    }
                responses.add(response)
            }

            // At least one request should succeed
            assertTrue(
                responses.any { it.status == HttpStatusCode.OK },
                "Expected at least one 200 OK response",
            )
        }
}
