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

class MessageTest {

    private val now = Clock.System.now()
    private val testUser1 = UserSummary(
        id = "user1",
        username = "user1",
        displayName = "User One",
        profilePhotoUrl = null,
        subscriptionTier = SubscriptionTier.FREE
    )
    private val testUser2 = UserSummary(
        id = "user2",
        username = "user2",
        displayName = "User Two",
        profilePhotoUrl = null,
        subscriptionTier = SubscriptionTier.FREE
    )

    @Test
    fun `message creation with valid data should succeed`() {
        val message = Message(
            id = "msg123",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Hello!",
            status = MessageStatus.SENT,
            isDeleted = false,
            createdAt = now
        )

        assertNotNull(message)
        assertEquals("msg123", message.id)
        assertEquals("conv123", message.conversationId)
        assertEquals("user1", message.senderId)
        assertEquals("Hello!", message.content)
        assertEquals(MessageStatus.SENT, message.status)
        assertFalse(message.isDeleted)
    }

    @Test
    fun `isSentByUser returns true for sender`() {
        val message = Message(
            id = "msg123",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Hello!",
            createdAt = now
        )

        assertTrue(message.isSentByUser("user1"))
        assertFalse(message.isSentByUser("user2"))
    }

    @Test
    fun `message with different statuses should work`() {
        val sentMessage = Message(
            id = "msg1",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Sent",
            status = MessageStatus.SENT,
            createdAt = now
        )

        val deliveredMessage = Message(
            id = "msg2",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Delivered",
            status = MessageStatus.DELIVERED,
            createdAt = now
        )

        val readMessage = Message(
            id = "msg3",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Read",
            status = MessageStatus.READ,
            createdAt = now
        )

        assertEquals(MessageStatus.SENT, sentMessage.status)
        assertEquals(MessageStatus.DELIVERED, deliveredMessage.status)
        assertEquals(MessageStatus.READ, readMessage.status)
    }

    @Test
    fun `message serialization and deserialization should work`() {
        val message = Message(
            id = "msg123",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Test message",
            status = MessageStatus.DELIVERED,
            isDeleted = false,
            createdAt = now
        )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<Message>(json)

        assertEquals(message, decoded)
    }

    @Test
    fun `SendMessageRequest with conversationId should succeed`() {
        val request = SendMessageRequest(
            conversationId = "conv123",
            recipientId = null,
            postContextId = null,
            content = "Hello!"
        )

        assertNotNull(request)
        assertEquals("conv123", request.conversationId)
        assertEquals("Hello!", request.content)
    }

    @Test
    fun `SendMessageRequest with recipientId should succeed`() {
        val request = SendMessageRequest(
            conversationId = null,
            recipientId = "user2",
            postContextId = "post123",
            content = "Hello!"
        )

        assertNotNull(request)
        assertEquals("user2", request.recipientId)
        assertEquals("post123", request.postContextId)
    }

    @Test
    fun `SendMessageRequest without conversationId or recipientId should fail`() {
        assertFailsWith<IllegalArgumentException> {
            SendMessageRequest(
                conversationId = null,
                recipientId = null,
                postContextId = null,
                content = "Hello!"
            )
        }
    }

    @Test
    fun `MessageListResponse serialization should work`() {
        val message = Message(
            id = "msg123",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "Test",
            createdAt = now
        )

        val response = MessageListResponse(
            messages = listOf(message),
            hasMore = true,
            nextCursor = "cursor123"
        )

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<MessageListResponse>(json)

        assertEquals(response, decoded)
    }
}

class ConversationTest {

    private val now = Clock.System.now()
    private val testLocation = Location(latitude = -6.2088, longitude = 106.8456)
    private val testUser1 = UserSummary(
        id = "user1",
        username = "user1",
        displayName = "User One",
        profilePhotoUrl = null,
        subscriptionTier = SubscriptionTier.FREE
    )
    private val testUser2 = UserSummary(
        id = "user2",
        username = "user2",
        displayName = "User Two",
        profilePhotoUrl = null,
        subscriptionTier = SubscriptionTier.FREE
    )

    @Test
    fun `conversation creation with valid data should succeed`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            postContext = null,
            lastMessage = null,
            unreadCount = 0,
            lastMessageAt = now,
            createdAt = now
        )

        assertNotNull(conversation)
        assertEquals("conv123", conversation.id)
        assertEquals(testUser1, conversation.participant1)
        assertEquals(testUser2, conversation.participant2)
        assertEquals(0, conversation.unreadCount)
    }

    @Test
    fun `getOtherParticipant returns correct participant`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            lastMessageAt = now,
            createdAt = now
        )

        assertEquals(testUser2, conversation.getOtherParticipant("user1"))
        assertEquals(testUser1, conversation.getOtherParticipant("user2"))
    }

    @Test
    fun `hasUnread returns true when unreadCount is greater than zero`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            unreadCount = 5,
            lastMessageAt = now,
            createdAt = now
        )

        assertTrue(conversation.hasUnread)
    }

    @Test
    fun `hasUnread returns false when unreadCount is zero`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            unreadCount = 0,
            lastMessageAt = now,
            createdAt = now
        )

        assertFalse(conversation.hasUnread)
    }

    @Test
    fun `previewText returns message content when lastMessage exists`() {
        val message = Message(
            id = "msg123",
            conversationId = "conv123",
            senderId = "user1",
            sender = testUser1,
            content = "This is a test message that is longer than 50 characters to test truncation",
            createdAt = now
        )

        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            lastMessage = message,
            lastMessageAt = now,
            createdAt = now
        )

        // The content is 75 characters, take(50) should return first 50 characters
        assertEquals("This is a test message that is longer than 50 char", conversation.previewText)
    }

    @Test
    fun `previewText returns default text when no lastMessage`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            lastMessage = null,
            lastMessageAt = now,
            createdAt = now
        )

        assertEquals("No messages yet", conversation.previewText)
    }

    @Test
    fun `conversation with post context should work`() {
        val post = Post(
            id = "post123",
            userId = "user1",
            user = testUser1,
            content = "Original post",
            location = testLocation,
            createdAt = now,
            updatedAt = now
        )

        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            postContext = post,
            lastMessageAt = now,
            createdAt = now
        )

        assertNotNull(conversation.postContext)
        assertEquals("post123", conversation.postContext?.id)
    }

    @Test
    fun `conversation serialization and deserialization should work`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            postContext = null,
            lastMessage = null,
            unreadCount = 3,
            lastMessageAt = now,
            createdAt = now
        )

        val json = Json.encodeToString(conversation)
        val decoded = Json.decodeFromString<Conversation>(json)

        assertEquals(conversation, decoded)
    }

    @Test
    fun `ConversationListResponse serialization should work`() {
        val conversation = Conversation(
            id = "conv123",
            participant1 = testUser1,
            participant2 = testUser2,
            lastMessageAt = now,
            createdAt = now
        )

        val response = ConversationListResponse(
            conversations = listOf(conversation),
            hasMore = false,
            nextCursor = null
        )

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<ConversationListResponse>(json)

        assertEquals(response, decoded)
    }
}

