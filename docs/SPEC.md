# NearYou ID - Product Specification

**Version:** 1.0  
**Last Updated:** 2025-10-16  
**Status:** Active

---

## Product Overview

### Vision
NearYou ID is a location-based social application that connects users within proximity (1–20 km) to share posts, engage through likes/comments, and communicate via direct messaging. The platform fosters local community building and real-time social interactions based on geographic proximity.

### Mission
Enable meaningful connections between people in the same geographic area through a privacy-focused, subscription-based social platform.

### Target Audience
- Urban and suburban residents aged 18-45
- People interested in local community engagement
- Users seeking location-based social interactions
- Premium users wanting enhanced features and ad-free experience

---

## Core Features

### 1. Timeline Feeds

#### 1.1 Nearby Feed
- **Description:** Display posts from users within configurable distance radius
- **Distance Levels:** Four levels with 0.1 km granularity from 1 km
  - Level 1: 1 km radius
  - Level 2: 5 km radius
  - Level 3: 10 km radius
  - Level 4: 20 km radius
- **Sorting:** Chronological (newest first)
- **Pagination:** 50 posts per page
- **Refresh:** Pull-to-refresh support
- **Empty State:** "No posts nearby" message with suggestion to expand radius

#### 1.2 Following Feed
- **Description:** Display posts from users the current user follows
- **Sorting:** Chronological (newest first)
- **Pagination:** 50 posts per page
- **Empty State:** "Not following anyone yet" with suggestion to explore Nearby feed

### 2. Posts

#### 2.1 Post Creation
- **Text Content:** 
  - Maximum 500 characters
  - Minimum 1 character
  - Plain text with basic formatting
- **Media Attachments (Premium Only):**
  - Images: JPEG, PNG, WebP
  - Maximum size: 10 MB per image
  - Maximum 4 images per post
- **Location:** Automatically attached based on user's current location
- **Quota:**
  - Free users: 100 posts per day
  - Premium users: Unlimited

#### 2.2 Post Display
- **Components:**
  - Profile photo (circular, 40x40 dp)
  - Username and display name
  - Post timestamp (relative: "2h ago")
  - Distance from viewer (e.g., "1.2 km away")
  - Text content
  - Media attachments (if present)
  - Like count
  - Comment count
  - Action buttons: Like, Reply, Message

#### 2.3 Post Interactions
- **Like:** Toggle like/unlike, optimistic UI update
- **Reply:** Navigate to post detail with comment section
- **Message:** Start chat with post author, post context auto-attached
- **Report:** Report inappropriate content
- **Delete:** Post owner can delete their own posts (soft delete)

### 3. Messaging

#### 3.1 Chat Features
- **Two-Way Messaging:** Direct messages between users
- **Post Context:** When initiated from a post, the post is attached as context at the top of the chat
- **Message Types:** Text only (MVP), media in future versions
- **Delivery Status:** Sent, delivered, read indicators
- **Quota:**
  - Free users: 500 chats per day
  - Premium users: Unlimited
- **Privacy:** Chat metadata (who, when) visible only to participants

#### 3.2 Conversation List
- **Display:** List of all conversations
- **Sorting:** Most recent message first
- **Preview:** Last message preview (truncated to 50 characters)
- **Unread Badge:** Count of unread messages
- **Empty State:** "No conversations yet"

### 4. Authentication

#### 4.1 Sign-Up Methods
- **Google Sign-In:** OAuth 2.0 integration
- **Phone OTP:** 6-digit code, 5-minute expiry
- **Email OTP:** 6-digit code, 5-minute expiry

#### 4.2 Verification
- **Mandatory:** All users must verify their account
- **OTP Delivery:** 
  - Email: Via SendGrid (production) or mock (MVP)
  - Phone: Via Twilio (production) or mock (MVP)
- **Rate Limiting:** 5 OTP requests per hour per user

#### 4.3 Session Management
- **JWT Tokens:** 7-day expiry
- **Refresh Tokens:** 30-day expiry
- **Secure Storage:** 
  - Android: Keystore
  - iOS: Keychain

### 5. Subscription Tiers

#### 5.1 Free Tier
- **Posts:** 100 per day
- **Chats:** 500 per day
- **Media:** No image uploads
- **Ads:** Display ads on timeline, message, and profile screens
- **Search:** Not available
- **Filters:** Not available
- **Badge:** None

#### 5.2 Premium Tier
- **Posts:** Unlimited
- **Chats:** Unlimited
- **Media:** Image uploads enabled
- **Ads:** No ads
- **Search:** Full search access (users, posts)
- **Filters:** Advanced filters for timeline
- **Badge:** Premium badge on profile
- **Duration Stats:** View post duration statistics

### 6. User Profile

#### 6.1 Profile Information
- **Display Name:** 1-50 characters
- **Username:** 3-20 characters, alphanumeric + underscore, unique
- **Bio:** 0-200 characters
- **Profile Photo:** Upload from device or camera
- **Subscription Badge:** Visible for premium users
- **Join Date:** Display when user joined

#### 6.2 Profile Actions
- **Edit Profile:** Update name, username, bio, photo
- **View Posts:** List of user's posts
- **Follow/Unfollow:** Toggle follow status
- **Message:** Start chat with user
- **Report:** Report user for inappropriate behavior
- **Block:** Block user from interactions

### 7. Search (Premium Only)

#### 7.1 Search Scope
- **Users:** Search by name or username
- **Posts:** Search by content text
- **Combined:** Search across both users and posts

#### 7.2 Search Features
- **Geo-Filtering:** Filter posts by distance
- **Date Range:** Filter posts by date (premium feature)
- **Relevance Ranking:** Results sorted by relevance
- **Pagination:** 20 results per page
- **Debounced Input:** 300ms delay before search

### 8. Safety & Moderation

#### 8.1 Report System
- **Report Types:**
  - Spam
  - Harassment
  - Inappropriate content
  - Other (with text reason)
- **Report Targets:** Users and posts
- **Moderation Queue:** Reports sent to admin panel

#### 8.2 Block System
- **Block User:** Prevent all interactions
- **Effects:**
  - Blocked user's posts hidden from timeline
  - Blocked user cannot message
  - Blocked user cannot see blocker's posts
  - Blocked user removed from search results
- **Unblock:** Reversible action

### 9. Notifications

#### 9.1 Notification Types
- **Like:** "X liked your post"
- **Comment:** "X commented on your post"
- **Follow:** "X started following you"
- **New Message:** "New message from X"

#### 9.2 Notification Delivery
- **Push Notifications:** FCM for Android and iOS
- **In-App Badge:** Unread count on notification icon
- **Notification List:** View all notifications in-app
- **Preferences:** User can disable specific notification types

### 10. Offline Support

#### 10.1 Offline Capabilities
- **View Cached Content:** Posts, messages, profile
- **Queue Actions:** Like, post, message queued for sync
- **Offline Indicator:** Banner showing offline status
- **Sync on Reconnect:** Automatic sync when network available

#### 10.2 Conflict Resolution
- **Strategy:** Last-write-wins for most conflicts
- **User Prompt:** For critical conflicts (e.g., profile updates)

---

## Technical Requirements

### Platform Support
- **Android:** minSdk 24 (Android 7.0), targetSdk 36
- **iOS:** iOS 14.0+
- **Backend:** JVM (Kotlin/Ktor)

### Performance Targets
- **Geo Queries:** <100ms p95 latency
- **API Response:** <200ms p95 latency
- **App Launch:** <2 seconds cold start
- **Timeline Load:** <1 second for 50 posts

### Security Requirements
- **Authentication:** JWT with secure token storage
- **Data Encryption:** HTTPS for all API calls
- **Privacy:** Location data anonymized, chat metadata private
- **Rate Limiting:** Prevent abuse and spam

### Scalability Requirements
- **Concurrent Users:** Support 1000+ concurrent users
- **Database:** Handle 1M+ posts with efficient geo queries
- **Storage:** Cloud storage for media (S3/GCS)

---

## Success Metrics

### User Engagement
- **Daily Active Users (DAU):** Target 1000+ within 3 months
- **Posts per Day:** Target 5000+ within 3 months
- **Messages per Day:** Target 10000+ within 3 months

### Technical Performance
- **API Uptime:** 99.9%
- **Geo Query Performance:** <100ms p95
- **Crash Rate:** <1%

### Business Metrics
- **Premium Conversion:** Target 5% conversion rate
- **Ad Revenue:** Target $X per 1000 impressions
- **User Retention:** 40% 30-day retention

---

## Future Enhancements (Post-MVP)

1. **Media Support:** Video posts, voice messages
2. **Events:** Location-based event creation and discovery
3. **Groups:** Private groups for communities
4. **Stories:** Temporary 24-hour posts
5. **Live Location Sharing:** Real-time location updates
6. **Advanced Filters:** Filter by interests, tags
7. **Internationalization:** Multi-language support
8. **Dark Mode:** Theme customization
9. **Accessibility:** Enhanced screen reader support
10. **Analytics Dashboard:** User-facing analytics

---

## Glossary

- **Nearby Feed:** Timeline showing posts from users within proximity
- **Following Feed:** Timeline showing posts from followed users
- **Post Context:** Reference to a post attached to a chat conversation
- **Geo Query:** Database query using geographic coordinates
- **Premium Badge:** Visual indicator of premium subscription status
- **Soft Delete:** Marking content as deleted without removing from database

