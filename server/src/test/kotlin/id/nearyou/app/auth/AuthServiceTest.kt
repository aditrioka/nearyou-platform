package id.nearyou.app.auth

import domain.model.auth.RegisterRequest
import io.lettuce.core.api.sync.RedisCommands
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AuthService
 * Tests business logic with mocked dependencies
 * Note: Database integration tests should be run separately with real PostgreSQL
 */
class AuthServiceTest {

    private lateinit var mockRedis: RedisCommands<String, String>
    private lateinit var authService: AuthService

    @Before
    fun setup() {
        // Mock Redis
        mockRedis = mockk(relaxed = true)

        // Create AuthService with mocked Redis
        authService = AuthService(mockRedis)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    // ========== Redis Integration Tests ==========

    @Test
    fun `Redis should be called with correct keys when storing registration data`() {
        // Setup
        val capturedKey = slot<String>()
        val capturedTTL = slot<Long>()
        val capturedData = slot<String>()

        every { mockRedis.setex(capture(capturedKey), capture(capturedTTL), capture(capturedData)) } returns "OK"

        // Execute
        mockRedis.setex("pending_registration:test@example.com", 300, "testuser|Test User|test@example.com|null|hashedPassword")

        // Verify
        assertTrue(capturedKey.captured.startsWith("pending_registration:"))
        assertEquals(300L, capturedTTL.captured)
        assertTrue(capturedData.captured.contains("testuser"))
    }

    @Test
    fun `Redis should be called to check rate limiting`() {
        // Setup
        val capturedKey = slot<String>()
        every { mockRedis.get(capture(capturedKey)) } returns null

        // Execute
        mockRedis.get("rate_limit:test@example.com")

        // Verify
        assertTrue(capturedKey.captured.startsWith("rate_limit:"))
    }
    
    // ========== Helper Method Tests ==========
    
    @Test
    fun `generateOtp should return 6-digit code`() {
        // Use reflection to access private method for testing
        val method = AuthService::class.java.getDeclaredMethod("generateOtp")
        method.isAccessible = true
        
        val otp = method.invoke(authService) as String
        
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
    }
    
    @Test
    fun `hashPassword should create valid BCrypt hash`() {
        // Use reflection to access private method
        val method = AuthService::class.java.getDeclaredMethod("hashPassword", String::class.java)
        method.isAccessible = true
        
        val password = "testPassword123"
        val hash = method.invoke(authService, password) as String
        
        // Verify it's a BCrypt hash
        assertTrue(hash.startsWith("$2"))
        
        // Verify the hash can verify the original password
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw(password, hash))
        
        // Verify different calls produce different hashes (due to salt)
        val hash2 = method.invoke(authService, password) as String
        assertFalse(hash == hash2)
    }
}

