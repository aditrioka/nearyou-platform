package domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Message domain model representing a chat message
 *
 * @property id Unique identifier for the message
 * @property conversationId ID of the conversation this message belongs to
 * @property senderId ID of the user who sent the message
 * @property sender User summary of the sender
 * @property content Text content of the message
 * @property status Delivery status of the message
 * @property isDeleted Whether the message has been deleted
 * @property createdAt Timestamp when the message was sent
 */
@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val sender: UserSummary,
    val content: String,
    val status: MessageStatus = MessageStatus.SENT,
    val isDeleted: Boolean = false,
    val createdAt: Instant
) {
    /**
     * Check if message was sent by the current user
     */
    fun isSentByUser(userId: String): Boolean = senderId == userId
}

/**
 * Message delivery status
 */
@Serializable
enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

/**
 * Conversation domain model
 *
 * @property id Unique identifier for the conversation
 * @property participant1 First participant
 * @property participant2 Second participant
 * @property postContext Optional post that initiated the conversation
 * @property lastMessage Last message in the conversation
 * @property unreadCount Number of unread messages for current user
 * @property lastMessageAt Timestamp of the last message
 * @property createdAt Timestamp when the conversation was created
 */
@Serializable
data class Conversation(
    val id: String,
    val participant1: UserSummary,
    val participant2: UserSummary,
    val postContext: Post? = null,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val lastMessageAt: Instant,
    val createdAt: Instant
) {
    /**
     * Get the other participant in the conversation
     */
    fun getOtherParticipant(currentUserId: String): UserSummary {
        return if (participant1.id == currentUserId) participant2 else participant1
    }

    /**
     * Check if conversation has unread messages
     */
    val hasUnread: Boolean
        get() = unreadCount > 0

    /**
     * Get preview text for conversation list
     */
    val previewText: String
        get() = lastMessage?.content?.take(50) ?: "No messages yet"
}

/**
 * Send message request model
 */
@Serializable
data class SendMessageRequest(
    val conversationId: String? = null,
    val recipientId: String? = null,
    val postContextId: String? = null,
    val content: String
) {
    init {
        require(conversationId != null || recipientId != null) {
            "Either conversationId or recipientId must be provided"
        }
    }
}

/**
 * Message list response with pagination
 */
@Serializable
data class MessageListResponse(
    val messages: List<Message>,
    val hasMore: Boolean,
    val nextCursor: String? = null
)

/**
 * Conversation list response
 */
@Serializable
data class ConversationListResponse(
    val conversations: List<Conversation>,
    val hasMore: Boolean,
    val nextCursor: String? = null
)

