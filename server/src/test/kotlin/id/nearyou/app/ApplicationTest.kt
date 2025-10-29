package id.nearyou.app

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    @Ignore("Requires Redis and PostgreSQL to be running. Use docker-compose up -d to start infrastructure, or use Testcontainers-based tests instead.")
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("NearYou ID API - Running", response.bodyAsText())
    }
}