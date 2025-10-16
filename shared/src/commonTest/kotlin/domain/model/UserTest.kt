package domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class UserTest {

    private val now = Clock.System.now()

    @Test
    fun `user creation with valid data should succeed`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            bio = "Test bio",
            profilePhotoUrl = null,
            isVerified = true,
            subscriptionTier = SubscriptionTier.FREE,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(user)
        assertEquals("user123", user.id)
        assertEquals("testuser", user.username)
        assertEquals("Test User", user.displayName)
        assertEquals("test@example.com", user.email)
        assertEquals("Test bio", user.bio)
        assertTrue(user.isVerified)
    }

    @Test
    fun `user creation with phone instead of email should succeed`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = null,
            phone = "+6281234567890",
            bio = null,
            profilePhotoUrl = null,
            isVerified = false,
            subscriptionTier = SubscriptionTier.FREE,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(user)
        assertEquals("+6281234567890", user.phone)
        assertFalse(user.isVerified)
    }

    @Test
    fun `user creation with both email and phone should succeed`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = "+6281234567890",
            bio = null,
            profilePhotoUrl = null,
            isVerified = false,
            subscriptionTier = SubscriptionTier.FREE,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(user)
        assertEquals("test@example.com", user.email)
        assertEquals("+6281234567890", user.phone)
    }

    @Test
    fun `user creation without email and phone should fail`() {
        assertFailsWith<IllegalArgumentException> {
            User(
                id = "user123",
                username = "testuser",
                displayName = "Test User",
                email = null,
                phone = null,
                bio = null,
                profilePhotoUrl = null,
                isVerified = false,
                subscriptionTier = SubscriptionTier.FREE,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `isPremium returns true for premium users`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            bio = null,
            profilePhotoUrl = null,
            isVerified = false,
            subscriptionTier = SubscriptionTier.PREMIUM,
            createdAt = now,
            updatedAt = now
        )

        assertTrue(user.isPremium)
        assertFalse(user.isFree)
    }

    @Test
    fun `isFree returns true for free users`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = null,
            bio = null,
            profilePhotoUrl = null,
            isVerified = false,
            subscriptionTier = SubscriptionTier.FREE,
            createdAt = now,
            updatedAt = now
        )

        assertTrue(user.isFree)
        assertFalse(user.isPremium)
    }

    @Test
    fun `user serialization and deserialization should work`() {
        val user = User(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = "+6281234567890",
            bio = "Test bio",
            profilePhotoUrl = "https://example.com/photo.jpg",
            isVerified = true,
            subscriptionTier = SubscriptionTier.PREMIUM,
            createdAt = now,
            updatedAt = now
        )

        val json = Json.encodeToString(user)
        val decoded = Json.decodeFromString<User>(json)

        assertEquals(user, decoded)
    }

    @Test
    fun `UserSummary creation should work`() {
        val summary = UserSummary(
            id = "user123",
            username = "testuser",
            displayName = "Test User",
            profilePhotoUrl = "https://example.com/photo.jpg",
            subscriptionTier = SubscriptionTier.PREMIUM
        )

        assertNotNull(summary)
        assertEquals("user123", summary.id)
        assertEquals("testuser", summary.username)
        assertEquals("Test User", summary.displayName)
    }

    @Test
    fun `CreateUserRequest serialization should work`() {
        val request = CreateUserRequest(
            username = "testuser",
            displayName = "Test User",
            email = "test@example.com",
            phone = "+6281234567890",
            bio = "Test bio"
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<CreateUserRequest>(json)

        assertEquals(request, decoded)
    }

    @Test
    fun `UpdateUserRequest serialization should work`() {
        val request = UpdateUserRequest(
            displayName = "Updated Name",
            bio = "Updated bio",
            profilePhotoUrl = "https://example.com/new-photo.jpg"
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<UpdateUserRequest>(json)

        assertEquals(request, decoded)
    }

    @Test
    fun `UpdateUserRequest with null values should work`() {
        val request = UpdateUserRequest(
            displayName = null,
            bio = null,
            profilePhotoUrl = null
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<UpdateUserRequest>(json)

        assertEquals(request, decoded)
    }
}

