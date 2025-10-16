package domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.days

class SubscriptionTest {

    private val now = Clock.System.now()

    @Test
    fun `subscription creation with valid data should succeed`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertNotNull(subscription)
        assertEquals("sub123", subscription.id)
        assertEquals("user123", subscription.userId)
        assertEquals(SubscriptionTier.PREMIUM, subscription.tier)
        assertTrue(subscription.isActive)
    }

    @Test
    fun `isExpired returns false for subscription without expiry`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(subscription.isExpired(now))
    }

    @Test
    fun `isExpired returns false for subscription not yet expired`() {
        val futureExpiry = now + 30.days
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = futureExpiry,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(subscription.isExpired(now))
    }

    @Test
    fun `isExpired returns true for expired subscription`() {
        val pastExpiry = now - 1.days
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now - 30.days,
            expiresAt = pastExpiry,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertTrue(subscription.isExpired(now))
    }

    @Test
    fun `isPremium returns true for active premium subscription`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertTrue(subscription.isPremium)
    }

    @Test
    fun `isPremium returns false for inactive premium subscription`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = null,
            isActive = false,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(subscription.isPremium)
    }

    @Test
    fun `isPremium returns false for free tier subscription`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.FREE,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(subscription.isPremium)
    }

    @Test
    fun `subscription serialization and deserialization should work`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = now + 30.days,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val json = Json.encodeToString(subscription)
        val decoded = Json.decodeFromString<Subscription>(json)

        assertEquals(subscription, decoded)
    }
}

class SubscriptionQuotaTest {

    @Test
    fun `getPostsQuota returns correct quota for free tier`() {
        val quota = SubscriptionQuota.getPostsQuota(SubscriptionTier.FREE)
        assertEquals(100, quota)
    }

    @Test
    fun `getPostsQuota returns unlimited for premium tier`() {
        val quota = SubscriptionQuota.getPostsQuota(SubscriptionTier.PREMIUM)
        assertEquals(Int.MAX_VALUE, quota)
    }

    @Test
    fun `getChatsQuota returns correct quota for free tier`() {
        val quota = SubscriptionQuota.getChatsQuota(SubscriptionTier.FREE)
        assertEquals(500, quota)
    }

    @Test
    fun `getChatsQuota returns unlimited for premium tier`() {
        val quota = SubscriptionQuota.getChatsQuota(SubscriptionTier.PREMIUM)
        assertEquals(Int.MAX_VALUE, quota)
    }

    @Test
    fun `canUploadMedia returns false for free tier`() {
        val canUpload = SubscriptionQuota.canUploadMedia(SubscriptionTier.FREE)
        assertFalse(canUpload)
    }

    @Test
    fun `canUploadMedia returns true for premium tier`() {
        val canUpload = SubscriptionQuota.canUploadMedia(SubscriptionTier.PREMIUM)
        assertTrue(canUpload)
    }

    @Test
    fun `canSearch returns false for free tier`() {
        val canSearch = SubscriptionQuota.canSearch(SubscriptionTier.FREE)
        assertFalse(canSearch)
    }

    @Test
    fun `canSearch returns true for premium tier`() {
        val canSearch = SubscriptionQuota.canSearch(SubscriptionTier.PREMIUM)
        assertTrue(canSearch)
    }
}

class UsageLogTest {

    private val now = Clock.System.now()

    @Test
    fun `UsageLog creation should work`() {
        val usageLog = UsageLog(
            id = "log123",
            userId = "user123",
            actionType = UsageActionType.POST,
            actionDate = "2025-10-16",
            count = 5,
            createdAt = now
        )

        assertNotNull(usageLog)
        assertEquals("log123", usageLog.id)
        assertEquals("user123", usageLog.userId)
        assertEquals(UsageActionType.POST, usageLog.actionType)
        assertEquals("2025-10-16", usageLog.actionDate)
        assertEquals(5, usageLog.count)
    }

    @Test
    fun `UsageLog serialization should work`() {
        val usageLog = UsageLog(
            id = "log123",
            userId = "user123",
            actionType = UsageActionType.CHAT,
            actionDate = "2025-10-16",
            count = 10,
            createdAt = now
        )

        val json = Json.encodeToString(usageLog)
        val decoded = Json.decodeFromString<UsageLog>(json)

        assertEquals(usageLog, decoded)
    }

    @Test
    fun `UsageActionType should have all expected values`() {
        val types = UsageActionType.entries
        assertEquals(3, types.size)
        assertTrue(types.contains(UsageActionType.POST))
        assertTrue(types.contains(UsageActionType.CHAT))
        assertTrue(types.contains(UsageActionType.SEARCH))
    }
}

class SubscriptionUpgradeRequestTest {

    @Test
    fun `SubscriptionUpgradeRequest creation should work`() {
        val request = SubscriptionUpgradeRequest(
            tier = SubscriptionTier.PREMIUM,
            paymentToken = "token123"
        )

        assertNotNull(request)
        assertEquals(SubscriptionTier.PREMIUM, request.tier)
        assertEquals("token123", request.paymentToken)
    }

    @Test
    fun `SubscriptionUpgradeRequest without payment token should work`() {
        val request = SubscriptionUpgradeRequest(
            tier = SubscriptionTier.PREMIUM,
            paymentToken = null
        )

        assertNotNull(request)
        assertEquals(null, request.paymentToken)
    }

    @Test
    fun `SubscriptionUpgradeRequest serialization should work`() {
        val request = SubscriptionUpgradeRequest(
            tier = SubscriptionTier.PREMIUM,
            paymentToken = "token123"
        )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<SubscriptionUpgradeRequest>(json)

        assertEquals(request, decoded)
    }
}

class SubscriptionStatusResponseTest {

    private val now = Clock.System.now()

    @Test
    fun `SubscriptionStatusResponse creation should work`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.PREMIUM,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val response = SubscriptionStatusResponse(
            subscription = subscription,
            usage = mapOf(
                UsageActionType.POST to 10,
                UsageActionType.CHAT to 50
            ),
            quotas = mapOf(
                UsageActionType.POST to Int.MAX_VALUE,
                UsageActionType.CHAT to Int.MAX_VALUE
            )
        )

        assertNotNull(response)
        assertEquals(subscription, response.subscription)
        assertEquals(10, response.usage[UsageActionType.POST])
        assertEquals(50, response.usage[UsageActionType.CHAT])
    }

    @Test
    fun `SubscriptionStatusResponse serialization should work`() {
        val subscription = Subscription(
            id = "sub123",
            userId = "user123",
            tier = SubscriptionTier.FREE,
            startedAt = now,
            expiresAt = null,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val response = SubscriptionStatusResponse(
            subscription = subscription,
            usage = mapOf(UsageActionType.POST to 5),
            quotas = mapOf(UsageActionType.POST to 100)
        )

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<SubscriptionStatusResponse>(json)

        assertEquals(response, decoded)
    }
}

