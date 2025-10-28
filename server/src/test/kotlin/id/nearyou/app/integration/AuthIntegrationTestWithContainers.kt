package id.nearyou.app.integration

import domain.model.auth.*
import id.nearyou.app.auth.AuthService
import id.nearyou.app.repository.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for authentication using Testcontainers
 * These tests use real PostgreSQL and Redis containers
 * No manual Docker setup required - containers are managed automatically
 */
class AuthIntegrationTestWithContainers : BaseIntegrationTest() {
    
    private lateinit var authService: AuthService
    
    @AfterEach
    fun cleanup() {
        clearDatabase()
        clearRedis()
    }
    
    @Test
    fun `should register user and send OTP`() {
        authService = AuthService(redis)
        
        val request = RegisterRequest(
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            password = "testPassword123"
        )
        
        val result = authService.registerUser(request)
        
        assertTrue(result.isSuccess, "Registration should succeed")
        
        val response = result.getOrNull()
        assertNotNull(response, "Response should not be null")
        assertEquals("test@example.com", response.identifier)
        assertEquals("OTP sent successfully", response.message)
        
        // Verify pending registration is stored in Redis
        val pendingData = redis.get("pending_registration:test@example.com")
        assertNotNull(pendingData, "Pending registration should be stored in Redis")
        assertTrue(pendingData.contains("testuser"), "Pending data should contain username")
    }
    
    @Test
    fun `should reject duplicate email registration`() {
        authService = AuthService(redis)
        
        // Create first user
        transaction(database) {
            UserRepository.createUser(
                username = "existinguser",
                displayName = "Existing User",
                email = "existing@example.com",
                phone = null,
                passwordHash = "hashedpassword"
            )
        }
        
        // Try to register with same email
        val request = RegisterRequest(
            username = "newuser",
            displayName = "New User",
            email = "existing@example.com",
            phone = null,
            password = "testPassword123"
        )
        
        val result = authService.registerUser(request)
        
        assertTrue(result.isFailure, "Registration should fail for duplicate email")
        assertTrue(
            result.exceptionOrNull()?.message?.contains("already registered") == true,
            "Error message should mention duplicate email"
        )
    }
    
    @Test
    fun `should reject duplicate username registration`() {
        authService = AuthService(redis)
        
        // Create first user
        transaction(database) {
            UserRepository.createUser(
                username = "existinguser",
                displayName = "Existing User",
                email = "existing@example.com",
                phone = null,
                passwordHash = "hashedpassword"
            )
        }
        
        // Try to register with same username
        val request = RegisterRequest(
            username = "existinguser",
            displayName = "New User",
            email = "new@example.com",
            phone = null,
            password = "testPassword123"
        )
        
        val result = authService.registerUser(request)
        
        assertTrue(result.isFailure, "Registration should fail for duplicate username")
        assertTrue(
            result.exceptionOrNull()?.message?.contains("already taken") == true,
            "Error message should mention duplicate username"
        )
    }
    
    @Test
    fun `should verify OTP and create user`() {
        authService = AuthService(redis)
        
        // First register
        val registerRequest = RegisterRequest(
            username = "otptest",
            displayName = "OTP Test",
            email = "otp@example.com",
            phone = null,
            password = "testPassword123"
        )
        
        val registerResult = authService.registerUser(registerRequest)
        assertTrue(registerResult.isSuccess, "Registration should succeed")
        
        // Get OTP from database
        val otp = transaction(database) {
            AuthService.OtpCodes
                .select(AuthService.OtpCodes.code)
                .where { AuthService.OtpCodes.userIdentifier eq "otp@example.com" }
                .map { it[AuthService.OtpCodes.code] }
                .firstOrNull()
        }
        
        assertNotNull(otp, "OTP should be stored in database")
        
        // Verify OTP
        val verifyRequest = VerifyOtpRequest(
            identifier = "otp@example.com",
            code = otp,
            type = "email"
        )
        
        val verifyResult = authService.verifyOtp(verifyRequest)
        assertTrue(verifyResult.isSuccess, "OTP verification should succeed")
        
        val tokenResponse = verifyResult.getOrNull()
        assertNotNull(tokenResponse, "Token response should not be null")
        assertNotNull(tokenResponse.accessToken, "Access token should be present")
        assertNotNull(tokenResponse.refreshToken, "Refresh token should be present")
        
        // Verify user was created in database
        val user = UserRepository.findByEmail("otp@example.com")
        assertNotNull(user, "User should be created in database")
        assertEquals("otptest", user.username)
        assertEquals("OTP Test", user.displayName)
    }
    
    @Test
    fun `should login existing user and send OTP`() {
        authService = AuthService(redis)
        
        // Create user first
        transaction(database) {
            UserRepository.createUser(
                username = "logintest",
                displayName = "Login Test",
                email = "login@example.com",
                phone = null,
                passwordHash = "hashedpassword"
            )
        }
        
        // Login
        val loginRequest = LoginRequest(
            email = "login@example.com",
            phone = null
        )
        
        val result = authService.loginUser(loginRequest)
        
        assertTrue(result.isSuccess, "Login should succeed")
        
        val response = result.getOrNull()
        assertNotNull(response, "Response should not be null")
        assertEquals("login@example.com", response.identifier)
        assertEquals("OTP sent successfully", response.message)
    }
    
    @Test
    fun `should refresh access token with valid refresh token`() {
        authService = AuthService(redis)
        
        // Create user and get tokens through registration flow
        val registerRequest = RegisterRequest(
            username = "refreshtest",
            displayName = "Refresh Test",
            email = "refresh@example.com",
            phone = null,
            password = "testPassword123"
        )
        
        authService.registerUser(registerRequest)
        
        // Get OTP and verify
        val otp = transaction(database) {
            AuthService.OtpCodes
                .select(AuthService.OtpCodes.code)
                .where { AuthService.OtpCodes.userIdentifier eq "refresh@example.com" }
                .map { it[AuthService.OtpCodes.code] }
                .firstOrNull()
        }
        
        assertNotNull(otp)
        
        val verifyRequest = VerifyOtpRequest(
            identifier = "refresh@example.com",
            code = otp,
            type = "email"
        )
        
        val verifyResult = authService.verifyOtp(verifyRequest)
        val tokens = verifyResult.getOrNull()
        assertNotNull(tokens)
        
        // Now test refresh
        val refreshRequest = RefreshTokenRequest(tokens.refreshToken)
        val refreshResult = authService.refreshToken(refreshRequest)

        assertTrue(refreshResult.isSuccess, "Token refresh should succeed")

        val newTokens = refreshResult.getOrNull()
        assertNotNull(newTokens, "New tokens should not be null")
        assertNotNull(newTokens?.accessToken, "New access token should be present")
        assertNotNull(newTokens?.refreshToken, "New refresh token should be present")
    }
    
    @Test
    fun `should revoke all user tokens on logout`() {
        authService = AuthService(redis)
        
        // Create user
        val user = transaction(database) {
            UserRepository.createUser(
                username = "logouttest",
                displayName = "Logout Test",
                email = "logout@example.com",
                phone = null,
                passwordHash = "hashedpassword"
            )
        }
        
        assertNotNull(user)
        
        // Revoke tokens
        val result = authService.revokeAllUserTokens(user.id)
        
        assertTrue(result.isSuccess, "Token revocation should succeed")
    }
}

