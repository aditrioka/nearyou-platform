package domain.validation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class UserValidationTest {

    @Test
    fun `username validation accepts valid username`() {
        val result = UserValidation.validateUsername("user_123")
        assertTrue(result.isValid)
    }

    @Test
    fun `username validation rejects empty username`() {
        val result = UserValidation.validateUsername("")
        assertFalse(result.isValid)
        assertEquals("Username cannot be empty", result.error)
    }

    @Test
    fun `username validation rejects too short username`() {
        val result = UserValidation.validateUsername("ab")
        assertFalse(result.isValid)
        assertEquals("Username must be at least 3 characters", result.error)
    }

    @Test
    fun `username validation rejects too long username`() {
        val result = UserValidation.validateUsername("a".repeat(21))
        assertFalse(result.isValid)
        assertEquals("Username must be at most 20 characters", result.error)
    }

    @Test
    fun `username validation rejects invalid characters`() {
        val result = UserValidation.validateUsername("user@123")
        assertFalse(result.isValid)
        assertEquals("Username can only contain alphanumeric characters and underscore", result.error)
    }

    @Test
    fun `display name validation accepts valid name`() {
        val result = UserValidation.validateDisplayName("John Doe")
        assertTrue(result.isValid)
    }

    @Test
    fun `display name validation rejects empty name`() {
        val result = UserValidation.validateDisplayName("")
        assertFalse(result.isValid)
    }

    @Test
    fun `display name validation rejects too long name`() {
        val result = UserValidation.validateDisplayName("a".repeat(51))
        assertFalse(result.isValid)
    }

    @Test
    fun `email validation accepts valid email`() {
        val result = UserValidation.validateEmail("test@example.com")
        assertTrue(result.isValid)
    }

    @Test
    fun `email validation rejects invalid email`() {
        val result = UserValidation.validateEmail("invalid-email")
        assertFalse(result.isValid)
        assertEquals("Invalid email format", result.error)
    }

    @Test
    fun `phone validation accepts valid phone`() {
        val result = UserValidation.validatePhone("+1234567890")
        assertTrue(result.isValid)
    }

    @Test
    fun `phone validation rejects invalid phone`() {
        val result = UserValidation.validatePhone("123")
        assertFalse(result.isValid)
    }

    @Test
    fun `bio validation accepts valid bio`() {
        val result = UserValidation.validateBio("This is my bio")
        assertTrue(result.isValid)
    }

    @Test
    fun `bio validation rejects too long bio`() {
        val result = UserValidation.validateBio("a".repeat(201))
        assertFalse(result.isValid)
    }

    @Test
    fun `create user validation requires email or phone`() {
        val result = UserValidation.validateCreateUser(
            username = "testuser",
            displayName = "Test User",
            email = null,
            phone = null,
            bio = null
        )
        assertFalse(result.isValid)
        assertEquals("Either email or phone must be provided", result.error)
    }

    @Test
    fun `create user validation accepts valid user with email`() {
        val result = UserValidation.validateCreateUser(
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            bio = "My bio"
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `create user validation accepts valid user with phone`() {
        val result = UserValidation.validateCreateUser(
            username = "testuser",
            displayName = "Test User",
            email = null,
            phone = "+1234567890",
            bio = null
        )
        assertTrue(result.isValid)
    }
}

