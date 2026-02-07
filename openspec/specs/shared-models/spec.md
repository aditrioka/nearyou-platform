# Shared Models Specification

## Purpose
The system defines core domain models shared between frontend and backend using kotlinx.serialization for cross-platform type safety.

## Requirements

### Requirement: Location Model
The system SHALL provide a Location model with validated geographic coordinates and distance calculation.

#### Scenario: Create location with valid coordinates
- GIVEN latitude within -90 to 90 and longitude within -180 to 180
- WHEN a Location instance is created
- THEN the system accepts the coordinates

#### Scenario: Calculate Haversine distance between locations
- GIVEN two Location instances with valid coordinates
- WHEN distanceTo() is called
- THEN the system returns the distance in kilometers using the Haversine formula

### Requirement: User Model
The system SHALL enforce that users have either email or phone number.

#### Scenario: Create user with email
- GIVEN a user with email and no phone number
- WHEN the User model is instantiated
- THEN the system accepts the user

#### Scenario: Create user with phone number
- GIVEN a user with phone number and no email
- WHEN the User model is instantiated
- THEN the system accepts the user

#### Scenario: User with subscription tier
- GIVEN a user with subscriptionTier field
- WHEN querying the user
- THEN the system returns the SubscriptionTier enum value (FREE or PREMIUM)

### Requirement: UserSummary Model
The system SHALL provide a lightweight UserSummary model for user references.

#### Scenario: Use UserSummary in lists
- GIVEN a list of users needs to be displayed
- WHEN UserSummary instances are used
- THEN the system provides id, name, and profilePhotoUrl without full User object overhead

### Requirement: Post Model
The system SHALL support posts with soft delete, media attachments, and distance display.

#### Scenario: Create post with media
- GIVEN a user creates a post with mediaUrls
- WHEN the Post is saved
- THEN the system stores the list of media URLs

#### Scenario: Soft delete post
- GIVEN a post exists with deletedAt = null
- WHEN the post is deleted
- THEN the system sets deletedAt to the current Instant

#### Scenario: Display post with distance
- GIVEN a post with location and viewer location
- WHEN rendering the post
- THEN the system calculates and displays distance from viewer

### Requirement: Message and Conversation Models
The system SHALL support messaging with status tracking and conversation management.

#### Scenario: Create message with status
- GIVEN a message is created
- WHEN the Message model is instantiated
- THEN the system includes MessageStatus enum (SENT, DELIVERED, READ)

#### Scenario: Group messages in conversation
- GIVEN multiple messages between users
- WHEN organizing messages
- THEN the system groups them in Conversation models with participants

### Requirement: Subscription and Quota System
The system SHALL enforce subscription-based quotas for posts and chats.

#### Scenario: FREE tier quota limits
- GIVEN a user with subscriptionTier = FREE
- WHEN checking quotas
- THEN the system enforces 100 posts per day and 500 chats per day

#### Scenario: PREMIUM tier unlimited access
- GIVEN a user with subscriptionTier = PREMIUM
- WHEN checking quotas
- THEN the system allows unlimited posts and chats

### Requirement: Serialization Support
The system SHALL use @Serializable annotation for all shared models.

#### Scenario: Serialize model to JSON
- GIVEN any shared model instance
- WHEN serializing with kotlinx.serialization
- THEN the system produces valid JSON

#### Scenario: Deserialize JSON to model
- GIVEN valid JSON for a shared model
- WHEN deserializing with kotlinx.serialization
- THEN the system creates a valid model instance

### Requirement: Timestamp Handling
The system SHALL use kotlinx.datetime.Instant for all timestamp fields.

#### Scenario: Store creation timestamp
- GIVEN a model with createdAt field
- WHEN the model is created
- THEN the system stores the timestamp as Instant

#### Scenario: Serialize Instant to ISO-8601
- GIVEN a model with Instant timestamp
- WHEN serializing to JSON
- THEN the system produces ISO-8601 formatted string
