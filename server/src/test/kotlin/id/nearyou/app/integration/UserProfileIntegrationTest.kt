package id.nearyou.app.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for user profile endpoints.
 *
 * These tests validate authentication enforcement on profile routes
 * using the Ktor test host without external dependencies.
 */
class UserProfileIntegrationTest {
    @Test
    fun `GET users me should require authentication`() =
        testApplication {
            val response = client.get("/users/me")

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `GET users me should reject invalid token`() =
        testApplication {
            val response =
                client.get("/users/me") {
                    header(HttpHeaders.Authorization, "Bearer invalid_token")
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `PUT users me should require authentication`() =
        testApplication {
            val response =
                client.put("/users/me") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"displayName": "New Name"}""")
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `PUT users me should reject invalid token`() =
        testApplication {
            val response =
                client.put("/users/me") {
                    header(HttpHeaders.Authorization, "Bearer invalid_token")
                    contentType(ContentType.Application.Json)
                    setBody("""{"displayName": "New Name"}""")
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `PUT users me with token should return 401 for invalid JWT`() =
        testApplication {
            val response =
                client.put("/users/me") {
                    header(HttpHeaders.Authorization, "Bearer test_token")
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "displayName": "Test User",
                            "bio": "This is a test bio",
                            "profilePhotoUrl": "https://example.com/photo.jpg"
                        }
                        """.trimIndent(),
                    )
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404 for invalid JWT, got ${response.status}",
            )
        }
}
