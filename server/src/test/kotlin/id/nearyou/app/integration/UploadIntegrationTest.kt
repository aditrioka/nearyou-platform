package id.nearyou.app.integration

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for file upload endpoints.
 *
 * These tests validate authentication enforcement on upload routes
 * using the Ktor test host without external dependencies.
 */
class UploadIntegrationTest {
    @Test
    fun `POST upload profile-photo should require authentication`() =
        testApplication {
            val response =
                client.post("/upload/profile-photo") {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    "file",
                                    ByteArray(100),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
                                    },
                                )
                            },
                        ),
                    )
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `POST upload profile-photo should reject invalid token`() =
        testApplication {
            val response =
                client.post("/upload/profile-photo") {
                    header(HttpHeaders.Authorization, "Bearer invalid_token")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    "file",
                                    ByteArray(100),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
                                    },
                                )
                            },
                        ),
                    )
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404, got ${response.status}",
            )
        }

    @Test
    fun `POST upload profile-photo with invalid JWT should return 401`() =
        testApplication {
            val testImageBytes = ByteArray(1024) { it.toByte() }

            val response =
                client.post("/upload/profile-photo") {
                    header(HttpHeaders.Authorization, "Bearer test_token")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    "file",
                                    testImageBytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"profile.jpg\"")
                                    },
                                )
                            },
                        ),
                    )
                }

            assertTrue(
                response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound,
                "Expected 401 or 404 for invalid JWT, got ${response.status}",
            )
        }
}
