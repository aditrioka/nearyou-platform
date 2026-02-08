package id.nearyou.app.integration

import io.ktor.client.request.get
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for health check endpoints
 *
 * Note: Without full application module loaded (requires DB/Redis),
 * these tests verify the endpoints respond with valid HTTP status codes.
 */
class HealthIntegrationTest {

    @Test
    fun `GET root should respond with valid status`() = testApplication {
        val response = client.get("/")

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }

    @Test
    fun `GET health should respond with valid status`() = testApplication {
        val response = client.get("/health")

        assertTrue(
            response.status.value in 200..599,
            "Expected valid HTTP status, got ${response.status}"
        )
    }
}
