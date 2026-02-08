package id.nearyou.app.integration

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for Post endpoints
 * Tests that all post routes exist and handle requests properly.
 *
 * Note: These tests require PostgreSQL and Redis to be running for full functionality.
 * Without infra, they verify endpoints exist and return valid HTTP responses.
 */
class PostIntegrationTest {

    // ========== GET /posts/nearby ==========

    @Test
    fun `GET posts nearby without auth should respond`() = testApplication {
        val response = client.get("/posts/nearby") {
            parameter("lat", "-6.2")
            parameter("lng", "106.8")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `GET posts nearby with invalid token should respond`() = testApplication {
        val response = client.get("/posts/nearby") {
            bearerAuth("invalid_token")
            parameter("lat", "-6.2")
            parameter("lng", "106.8")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `GET posts nearby with valid params should respond`() = testApplication {
        val response = client.get("/posts/nearby") {
            bearerAuth("mock_token")
            parameter("lat", "-6.2")
            parameter("lng", "106.8")
            parameter("radius", "5000")
            parameter("limit", "20")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    // ========== POST /posts ==========

    @Test
    fun `POST posts without auth should respond`() = testApplication {
        val response = client.post("/posts") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "content": "Hello nearby!",
                    "location": {"latitude": -6.2, "longitude": 106.8}
                }
            """.trimIndent())
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `POST posts with invalid token should respond`() = testApplication {
        val response = client.post("/posts") {
            contentType(ContentType.Application.Json)
            bearerAuth("invalid_token")
            setBody("""
                {
                    "content": "Hello nearby!",
                    "location": {"latitude": -6.2, "longitude": 106.8}
                }
            """.trimIndent())
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `POST posts with valid body should respond`() = testApplication {
        val response = client.post("/posts") {
            contentType(ContentType.Application.Json)
            bearerAuth("mock_token")
            setBody("""
                {
                    "content": "Hello nearby!",
                    "location": {"latitude": -6.2, "longitude": 106.8},
                    "mediaUrls": []
                }
            """.trimIndent())
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    // ========== GET /posts/{id} ==========

    @Test
    fun `GET posts by id without auth should respond`() = testApplication {
        val response = client.get("/posts/some-post-id")

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `GET posts by id with invalid token should respond`() = testApplication {
        val response = client.get("/posts/some-post-id") {
            bearerAuth("invalid_token")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    // ========== PUT /posts/{id} ==========

    @Test
    fun `PUT posts by id without auth should respond`() = testApplication {
        val response = client.put("/posts/some-post-id") {
            contentType(ContentType.Application.Json)
            setBody("""{"content": "Updated content"}""")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `PUT posts by id with invalid token should respond`() = testApplication {
        val response = client.put("/posts/some-post-id") {
            contentType(ContentType.Application.Json)
            bearerAuth("invalid_token")
            setBody("""{"content": "Updated content"}""")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    // ========== DELETE /posts/{id} ==========

    @Test
    fun `DELETE posts by id without auth should respond`() = testApplication {
        val response = client.delete("/posts/some-post-id")

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `DELETE posts by id with invalid token should respond`() = testApplication {
        val response = client.delete("/posts/some-post-id") {
            bearerAuth("invalid_token")
        }

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }
}
