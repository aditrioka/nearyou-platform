# NearYou ID MVP - Complete Execution Plan

**Version:** 1.0  
**Created:** 2025-10-16  
**Status:** Ready for Execution  
**Project Type:** Kotlin Multiplatform (KMP) with Compose Multiplatform UI and Ktor Backend

---

## Executive Summary

### Product Vision
NearYou ID is a location-based social application that connects users within proximity (1–20 km) to share posts, engage through likes/comments, and communicate via direct messaging. Built with Kotlin Multiplatform, it shares business logic across Android, iOS, and backend layers, ensuring consistency and reducing duplication.

### Key Context (For Resumption)
- **Tech Stack:** Kotlin Multiplatform (KMP), Compose Multiplatform (UI), Ktor (Backend)
- **Platforms:** Android (minSdk 24, targetSdk 36), iOS, Server (JVM)
- **Database:** PostgreSQL 15+ with PostGIS extension for geospatial queries
- **Architecture:** Clean/Hexagonal architecture with shared domain logic
- **Current State:** Fresh KMP project scaffold with `/composeApp`, `/shared`, `/server`, `/iosApp` modules
- **Gradle:** Kotlin 2.2.20, Ktor 3.3.0, Compose Multiplatform 1.9.0

### Core Features
1. **Timeline:** Nearby (1–20 km, four distance levels, 0.1 km granularity from 1 km) and Following feeds
2. **Posts:** Text, media (premium), like/comment counts, reply, message actions
3. **Chat:** Two-way messaging with auto-attached post context, new message notifications
4. **Authentication:** Google Sign-In, Phone OTP, Email OTP with mandatory verification
5. **Subscription:** Free (100 posts, 500 chats/day, no images, ads) vs Premium (unlimited, images, filters, badge, no ads)
6. **Profile & Settings:** Edit profile, notifications, privacy, subscription management
7. **Search:** Subscriber-only search by name, username, post content
8. **Safety:** Report/block users and posts
9. **Offline-First:** Local caching with automatic sync
10. **Admin Panel:** User/post/message/report management, analytics, audit log

### Success Criteria
- Android & iOS apps functional with shared UI and business logic
- Backend API with PostGIS geo queries (<100ms p95 for nearby posts)
- Authentication with JWT, rate limiting, logging, monitoring
- Offline-first data handling with sync conflict resolution
- Admin panel for moderation and analytics
- CI/CD pipeline with automated testing
- Privacy-compliant chat metadata handling

---

## Roadmap

### Phase 0: Foundation & Setup (Weeks 1-2)
**Goal:** Establish project structure, documentation, infrastructure, and shared domain models.

**Deliverables:**
- `SPEC.md`: Restated product specification
- `ARCHITECTURE.md`: System architecture, module boundaries, data flow
- `DECISIONS.md`: Architectural Decision Records (ADRs)
- `INFRA.md`: Infrastructure setup, database schema, deployment
- `TESTING.md`: Testing strategy and guidelines
- `CHANGELOG.md`: Version history and changes
- PostgreSQL + PostGIS setup with Docker Compose
- Shared domain models in `/shared/commonMain`
- CI/CD skeleton (GitHub Actions or GitLab CI)

### Phase 1: Authentication & User Management (Weeks 3-4)
**Goal:** Implement authentication flows (Google, Phone OTP, Email OTP) with JWT, user registration, profile management.

**Deliverables:**
- Backend: Auth endpoints, JWT generation/validation, OTP service
- Shared: Auth domain models, validation logic
- Frontend: Login/signup screens, OTP verification, token storage
- Database: Users table with verification status
- Tests: Auth flow integration tests

### Phase 2: Core Timeline & Posts (Weeks 5-7)
**Goal:** Build timeline (Nearby, Following), post creation, like/comment, PostGIS geo queries.

**Deliverables:**
- Backend: Post CRUD, geo queries with PostGIS (GiST index), like/comment endpoints
- Shared: Post domain models, distance calculation logic
- Frontend: Timeline UI, post creation, pull-to-refresh, empty states
- Database: Posts, likes, comments tables with geospatial indexes
- Tests: Geo query performance tests, UI tests

### Phase 3: Messaging & Notifications (Weeks 8-9)
**Goal:** Implement two-way chat with post context, FCM notifications for likes, replies, follows, new messages.

**Deliverables:**
- Backend: Chat endpoints, FCM integration, notification service
- Shared: Message domain models, notification types
- Frontend: Chat UI, notification handling, badge counts
- Database: Messages, notifications tables
- Tests: Message delivery tests, notification tests

### Phase 4: Subscription & Monetization (Weeks 10-11)
**Goal:** Implement free vs premium tiers, ads integration, subscription management.

**Deliverables:**
- Backend: Subscription endpoints, usage tracking, ad serving logic
- Shared: Subscription domain models, quota validation
- Frontend: Subscription UI, paywall, ads display (timeline/message/profile)
- Database: Subscriptions, usage_logs tables
- Tests: Quota enforcement tests

### Phase 5: Search, Safety & Moderation (Weeks 12-13)
**Goal:** Implement search (subscriber-only), report/block, admin panel.

**Deliverables:**
- Backend: Search endpoints (PostgreSQL full-text or OpenSearch), report/block endpoints, admin API
- Shared: Search models, moderation types
- Frontend: Search UI, report/block flows, admin panel (web)
- Database: Reports, blocks tables, search indexes
- Tests: Search relevance tests, moderation workflow tests

### Phase 6: Offline-First & Sync (Weeks 14-15)
**Goal:** Implement offline caching, sync strategy, conflict resolution.

**Deliverables:**
- Shared: Sync logic, conflict resolution strategies
- Frontend: Local database (SQLDelight), sync service, offline indicators
- Backend: Sync endpoints, last-modified tracking
- Tests: Offline scenario tests, sync conflict tests

### Phase 7: Polish, Performance & Launch Prep (Weeks 16-18)
**Goal:** Optimize performance, finalize UI/UX, security audit, load testing, deployment.

**Deliverables:**
- Performance: Geo query optimization, caching (Redis), rate limiting
- Security: Audit, spam mitigation, privacy compliance
- Observability: Logging (structured), monitoring (Prometheus/Grafana), alerting
- Deployment: Docker images, Kubernetes manifests, CI/CD finalization
- Documentation: API docs, deployment guide, runbooks
- Tests: Load tests, security tests, E2E tests

---

## Milestones

| Milestone | Target Week | Deliverables | Success Criteria |
|-----------|-------------|--------------|------------------|
| M0: Foundation Complete | Week 2 | Docs, DB setup, shared models, CI skeleton | All docs created, DB running, CI green |
| M1: Auth Live | Week 4 | Auth flows, JWT, user profiles | Users can sign up/login on Android/iOS |
| M2: Timeline Functional | Week 7 | Posts, geo queries, like/comment | Users can view nearby posts, interact |
| M3: Messaging Active | Week 9 | Chat, notifications | Users can message, receive notifications |
| M4: Monetization Ready | Week 11 | Subscriptions, ads | Free/premium tiers enforced, ads shown |
| M5: Moderation Tools | Week 13 | Search, report/block, admin panel | Admins can moderate, users can search |
| M6: Offline Support | Week 15 | Offline caching, sync | App works offline, syncs on reconnect |
| M7: Production Ready | Week 18 | Performance, security, deployment | Load tested, deployed, monitored |

---

## Work Breakdown Structure (Tasks)

### Phase 0: Foundation & Setup

#### T-001: Create Project Documentation
**Purpose:** Establish foundational documentation for the project to enable resumable execution and knowledge sharing.

**Steps:**
1. Create `SPEC.md` with full product specification (restated from this plan)
2. Create `ARCHITECTURE.md` with system architecture, module boundaries, data flow diagrams
3. Create `DECISIONS.md` for ADRs (e.g., why PostGIS, why KMP, auth strategy)
4. Create `INFRA.md` with infrastructure setup, database schema, deployment strategy
5. Create `TESTING.md` with testing strategy (unit, integration, E2E, performance)
6. Create `CHANGELOG.md` with version 0.1.0 entry

**Expected Artifacts:**
- `docs/CORE/SPEC.md`
- `docs/CORE/ARCHITECTURE.md`
- `docs/CORE/DECISIONS.md`
- `docs/CORE/INFRA.md`
- `docs/CORE/TESTING.md`
- `docs/CORE/CHANGELOG.md`

**Definition of Done:**
- All six documents created with comprehensive content
- Documents reviewed for completeness and accuracy
- Committed to version control

**Testing:** N/A (documentation task)

**Rollback Strategy:** Delete created files if needed

**Dependencies:** None

---

#### T-002: Setup PostgreSQL with PostGIS
**Purpose:** Establish the database foundation with geospatial capabilities for location-based queries.

**Steps:**
1. Create `docker-compose.yml` with PostgreSQL 15+ and PostGIS extension
2. Create `database/init.sql` with PostGIS extension enablement
3. Create initial schema: `users`, `posts`, `likes`, `comments` tables
4. Add GiST index on `posts.location` (GEOGRAPHY type)
5. Create `database/migrations/` directory structure
6. Document connection strings in `INFRA.md`
7. Test database connectivity and PostGIS functions

**Expected Artifacts:**
- `docker-compose.yml`
- `database/init.sql`
- `database/migrations/001_initial_schema.sql`
- Updated `INFRA.md`

**Definition of Done:**
- PostgreSQL + PostGIS running via Docker Compose
- Initial schema applied successfully
- GiST index created and verified
- Connection tested from server module

**Testing:**
- Run `SELECT PostGIS_Version();` to verify extension
- Test geo query: `SELECT ST_DWithin(location, ST_MakePoint(lon, lat)::geography, 1000);`

**Rollback Strategy:** `docker-compose down -v` to remove volumes

**Dependencies:** Docker installed

---

#### T-003: Define Shared Domain Models
**Purpose:** Create shared domain models in `/shared/commonMain` for use across frontend and backend.

**Steps:**
1. Create `shared/src/commonMain/kotlin/domain/model/User.kt`
2. Create `shared/src/commonMain/kotlin/domain/model/Post.kt`
3. Create `shared/src/commonMain/kotlin/domain/model/Message.kt`
4. Create `shared/src/commonMain/kotlin/domain/model/Subscription.kt`
5. Create `shared/src/commonMain/kotlin/domain/model/Location.kt`
6. Add validation logic in `shared/src/commonMain/kotlin/domain/validation/`
7. Write unit tests for validation logic

**Expected Artifacts:**
- Domain models in `/shared/commonMain/kotlin/domain/model/`
- Validation logic in `/shared/commonMain/kotlin/domain/validation/`
- Unit tests in `/shared/commonTest/kotlin/domain/`

**Definition of Done:**
- All core domain models defined with data classes
- Validation logic implemented and tested
- Models compile for all targets (Android, iOS, JVM)
- Unit tests passing with >80% coverage

**Testing:**
- Unit tests for validation (e.g., username format, post length limits)
- Compilation test for all platforms

**Rollback Strategy:** Revert commits

**Dependencies:** T-001 (ARCHITECTURE.md defines models)

---

#### T-004: Setup CI/CD Pipeline
**Purpose:** Automate build, test, and deployment processes.

**Steps:**
1. Create `.github/workflows/ci.yml` (or GitLab CI equivalent)
2. Configure jobs: lint, test (shared, composeApp, server), build
3. Add code coverage reporting (Kover or JaCoCo)
4. Configure Docker image build for server
5. Add deployment job (manual trigger initially)
6. Document CI/CD process in `INFRA.md`

**Expected Artifacts:**
- `.github/workflows/ci.yml`
- Updated `INFRA.md`

**Definition of Done:**
- CI pipeline runs on every push/PR
- All tests execute successfully
- Code coverage report generated
- Docker image builds successfully

**Testing:**
- Trigger CI manually and verify all jobs pass
- Test failure scenarios (failing test, lint error)

**Rollback Strategy:** Disable workflow or revert changes

**Dependencies:** T-003 (needs code to test)

---

### Phase 1: Authentication & User Management

#### T-101: Implement Backend Auth Service
**Purpose:** Create authentication service with JWT generation, OTP handling, and user registration.

**Steps:**
1. Add dependencies: `ktor-server-auth`, `ktor-server-auth-jwt`, `kotlinx-serialization`
2. Create `server/src/main/kotlin/auth/AuthService.kt`
3. Implement OTP generation and verification (6-digit, 5-minute expiry)
4. Implement JWT generation with 7-day expiry, refresh token logic
5. Create `server/src/main/kotlin/auth/AuthRoutes.kt` with endpoints:
   - `POST /auth/register` (email/phone)
   - `POST /auth/verify-otp`
   - `POST /auth/login/google`
   - `POST /auth/refresh`
6. Add password hashing (BCrypt) for email auth
7. Integrate with PostgreSQL users table
8. Add rate limiting (5 OTP requests per hour per user)

**Expected Artifacts:**
- `server/src/main/kotlin/auth/AuthService.kt`
- `server/src/main/kotlin/auth/AuthRoutes.kt`
- `server/src/main/kotlin/auth/JwtConfig.kt`
- Database migration: `002_auth_tables.sql`

**Definition of Done:**
- All auth endpoints functional and tested
- JWT tokens generated and validated correctly
- OTP sent via email/SMS (mock for MVP, integrate Twilio/SendGrid later)
- Rate limiting enforced
- Integration tests passing

**Testing:**
- Integration tests for each auth flow
- Test OTP expiry and invalid OTP handling
- Test JWT validation and refresh
- Test rate limiting

**Rollback Strategy:** Revert migration and code changes

**Dependencies:** T-002 (database), T-003 (User model)

---

#### T-102: Implement Frontend Auth Flows
**Purpose:** Create login/signup UI with OTP verification and token storage.

**Steps:**
1. Create `composeApp/src/commonMain/kotlin/ui/auth/LoginScreen.kt`
2. Create `composeApp/src/commonMain/kotlin/ui/auth/SignupScreen.kt`
3. Create `composeApp/src/commonMain/kotlin/ui/auth/OtpVerificationScreen.kt`
4. Implement Google Sign-In (platform-specific in `androidMain` and `iosMain`)
5. Create `shared/src/commonMain/kotlin/data/AuthRepository.kt`
6. Implement secure token storage (Keystore on Android, Keychain on iOS)
7. Add navigation logic (authenticated vs unauthenticated routes)
8. Handle auth errors and display user-friendly messages

**Expected Artifacts:**
- Auth screens in `composeApp/src/commonMain/kotlin/ui/auth/`
- `shared/src/commonMain/kotlin/data/AuthRepository.kt`
- Platform-specific token storage in `androidMain` and `iosMain`

**Definition of Done:**
- Users can sign up with email/phone and verify OTP
- Users can log in with Google
- Tokens stored securely and persisted across app restarts
- Navigation redirects based on auth state
- UI tests passing

**Testing:**
- UI tests for each screen
- Test token persistence
- Test Google Sign-In flow (manual on device)

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-101 (backend auth), T-003 (User model)

---

#### T-103: Implement User Profile Management
**Purpose:** Allow users to view and edit their profiles.

**Steps:**
1. Create backend endpoint: `GET /users/me`, `PUT /users/me`
2. Create `composeApp/src/commonMain/kotlin/ui/profile/ProfileScreen.kt`
3. Create `composeApp/src/commonMain/kotlin/ui/profile/EditProfileScreen.kt`
4. Implement profile photo upload (S3/GCS integration)
5. Add validation for name, bio, username (unique check)
6. Create `shared/src/commonMain/kotlin/data/UserRepository.kt`
7. Handle profile update errors

**Expected Artifacts:**
- Backend: `server/src/main/kotlin/user/UserRoutes.kt`
- Frontend: Profile screens in `composeApp/src/commonMain/kotlin/ui/profile/`
- `shared/src/commonMain/kotlin/data/UserRepository.kt`

**Definition of Done:**
- Users can view their profile
- Users can edit name, bio, username, photo
- Profile photo uploaded to cloud storage
- Validation enforced (unique username)
- Tests passing

**Testing:**
- Integration tests for profile endpoints
- UI tests for profile screens
- Test photo upload

**Rollback Strategy:** Revert changes

**Dependencies:** T-101 (auth), T-002 (database)

---

### Phase 2: Core Timeline & Posts

#### T-201: Implement PostGIS Geo Queries
**Purpose:** Create high-performance geospatial queries for nearby posts.

**Steps:**
1. Create database migration: `003_posts_geo.sql`
2. Add `posts` table with `location GEOGRAPHY(Point, 4326)` column
3. Create GiST index: `CREATE INDEX idx_posts_location ON posts USING GIST(location);`
4. Implement query function in `server/src/main/kotlin/post/PostRepository.kt`:
   ```sql
   SELECT * FROM posts
   WHERE ST_DWithin(location, ST_MakePoint($lon, $lat)::geography, $radius)
   ORDER BY created_at DESC LIMIT 50;
   ```
5. Add distance calculation in query results
6. Implement four distance levels (1, 5, 10, 20 km) with 0.1 km granularity from 1 km
7. Optimize query performance (target <100ms p95)
8. Add query cost monitoring

**Expected Artifacts:**
- `database/migrations/003_posts_geo.sql`
- `server/src/main/kotlin/post/PostRepository.kt`
- Performance test results

**Definition of Done:**
- Geo queries return correct results within radius
- GiST index used (verify with EXPLAIN ANALYZE)
- Query performance <100ms p95 for 10k posts
- Distance levels implemented correctly
- Performance tests passing

**Testing:**
- Unit tests for distance calculation
- Performance tests with 10k, 100k, 1M posts
- Verify index usage with EXPLAIN ANALYZE

**Rollback Strategy:** Revert migration

**Dependencies:** T-002 (database)

---

#### T-202: Implement Post Creation & CRUD
**Purpose:** Allow users to create, read, update, delete posts.

**Steps:**
1. Create backend endpoints:
   - `POST /posts` (create)
   - `GET /posts/:id` (read)
   - `PUT /posts/:id` (update, owner only)
   - `DELETE /posts/:id` (delete, owner only)
2. Implement media upload for premium users (S3/GCS)
3. Add validation: text length (max 500 chars), media size (max 10MB)
4. Enforce quota: free users 100 posts/day
5. Create `shared/src/commonMain/kotlin/data/PostRepository.kt`
6. Add post ownership verification
7. Implement soft delete (mark as deleted, don't remove)

**Expected Artifacts:**
- `server/src/main/kotlin/post/PostRoutes.kt`
- `server/src/main/kotlin/post/PostService.kt`
- `shared/src/commonMain/kotlin/data/PostRepository.kt`

**Definition of Done:**
- All CRUD endpoints functional
- Media upload works for premium users
- Quota enforced for free users
- Validation enforced
- Integration tests passing

**Testing:**
- Integration tests for each endpoint
- Test quota enforcement
- Test media upload
- Test ownership verification

**Rollback Strategy:** Revert changes

**Dependencies:** T-201 (geo queries), T-103 (user profile)

---

#### T-203: Implement Timeline UI
**Purpose:** Display Nearby and Following feeds with pull-to-refresh and empty states.

**Steps:**
1. Create `composeApp/src/commonMain/kotlin/ui/timeline/TimelineScreen.kt`
2. Implement tab navigation: Nearby, Following
3. Create `composeApp/src/commonMain/kotlin/ui/timeline/PostCard.kt`
4. Implement pull-to-refresh (Compose Multiplatform)
5. Add empty states (no posts nearby, not following anyone)
6. Implement infinite scroll (pagination)
7. Display post: profile photo, name, text, media, like/comment counts
8. Add actions: like, reply, message buttons
9. Implement location permission request (Android/iOS)
10. Handle location updates (background location for Nearby feed)

**Expected Artifacts:**
- Timeline screens in `composeApp/src/commonMain/kotlin/ui/timeline/`
- `PostCard.kt` component
- Location service in platform-specific code

**Definition of Done:**
- Timeline displays posts correctly
- Pull-to-refresh works
- Empty states shown appropriately
- Infinite scroll loads more posts
- Location permission requested and handled
- UI tests passing

**Testing:**
- UI tests for timeline rendering
- Test pull-to-refresh
- Test empty states
- Test location permission flow

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-202 (post CRUD), T-201 (geo queries)

---

#### T-204: Implement Like & Comment
**Purpose:** Allow users to like and comment on posts.

**Steps:**
1. Create backend endpoints:
   - `POST /posts/:id/like` (toggle like)
   - `GET /posts/:id/comments`
   - `POST /posts/:id/comments`
   - `DELETE /comments/:id`
2. Create database tables: `likes`, `comments`
3. Add like/comment counts to post responses
4. Implement comment threading (optional: single-level replies)
5. Create `composeApp/src/commonMain/kotlin/ui/post/PostDetailScreen.kt`
6. Create `composeApp/src/commonMain/kotlin/ui/post/CommentItem.kt`
7. Add optimistic UI updates (like button)

**Expected Artifacts:**
- Backend: Like/comment routes and services
- Frontend: Post detail screen, comment UI
- Database migration: `004_likes_comments.sql`

**Definition of Done:**
- Users can like/unlike posts
- Users can comment on posts
- Like/comment counts accurate
- Post detail screen shows comments
- Optimistic UI updates work
- Tests passing

**Testing:**
- Integration tests for like/comment endpoints
- UI tests for post detail screen
- Test optimistic updates

**Rollback Strategy:** Revert migration and code

**Dependencies:** T-202 (posts), T-203 (timeline UI)

---

### Phase 3: Messaging & Notifications

#### T-301: Implement Chat Backend
**Purpose:** Create two-way messaging with post context attachment.

**Steps:**
1. Create database tables: `conversations`, `messages`
2. Add `post_context_id` to messages table (nullable, references posts)
3. Create backend endpoints:
   - `GET /conversations` (list user's conversations)
   - `GET /conversations/:id/messages`
   - `POST /conversations/:id/messages`
   - `POST /conversations` (start new conversation with post context)
4. Implement message delivery status (sent, delivered, read)
5. Add pagination for message history
6. Enforce quota: free users 500 chats/day
7. Implement privacy: chat metadata (who, when) visible only to participants
8. Add WebSocket support for real-time messaging (optional for MVP)

**Expected Artifacts:**
- `server/src/main/kotlin/chat/ChatRoutes.kt`
- `server/src/main/kotlin/chat/ChatService.kt`
- Database migration: `005_chat.sql`

**Definition of Done:**
- All chat endpoints functional
- Post context attached to messages
- Quota enforced for free users
- Privacy enforced (participants only)
- Integration tests passing

**Testing:**
- Integration tests for chat endpoints
- Test quota enforcement
- Test privacy (unauthorized access blocked)
- Test post context attachment

**Rollback Strategy:** Revert migration and code

**Dependencies:** T-202 (posts), T-103 (users)

---

#### T-302: Implement Chat UI
**Purpose:** Create chat interface with message list and input.

**Steps:**
1. Create `composeApp/src/commonMain/kotlin/ui/chat/ConversationListScreen.kt`
2. Create `composeApp/src/commonMain/kotlin/ui/chat/ChatScreen.kt`
3. Display post context at top of chat (if present)
4. Implement message bubbles (sent vs received)
5. Add message input with send button
6. Implement auto-scroll to latest message
7. Add typing indicators (optional for MVP)
8. Handle message delivery status display
9. Add "Start Chat" action from post detail screen

**Expected Artifacts:**
- Chat screens in `composeApp/src/commonMain/kotlin/ui/chat/`
- `shared/src/commonMain/kotlin/data/ChatRepository.kt`

**Definition of Done:**
- Users can view conversation list
- Users can send/receive messages
- Post context displayed in chat
- Message bubbles styled correctly
- Auto-scroll works
- UI tests passing

**Testing:**
- UI tests for chat screens
- Test message sending/receiving
- Test post context display

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-301 (chat backend)

---

#### T-303: Implement FCM Notifications
**Purpose:** Send push notifications for likes, replies, follows, new messages.

**Steps:**
1. Add FCM dependencies to Android and iOS projects
2. Create `server/src/main/kotlin/notification/NotificationService.kt`
3. Integrate FCM Admin SDK in backend
4. Create database table: `notifications`, `device_tokens`
5. Implement notification types: like, comment, follow, new_message
6. Create backend endpoints:
   - `POST /notifications/register-token`
   - `GET /notifications`
   - `PUT /notifications/:id/read`
7. Send notifications on events (like, comment, follow, message)
8. Implement notification preferences (user can disable types)
9. Add badge count for unread notifications
10. Handle notification tap (deep link to relevant screen)

**Expected Artifacts:**
- `server/src/main/kotlin/notification/NotificationService.kt`
- `server/src/main/kotlin/notification/NotificationRoutes.kt`
- FCM integration in `androidMain` and `iosMain`
- Database migration: `006_notifications.sql`

**Definition of Done:**
- Notifications sent for all event types
- Users receive push notifications on device
- Notification preferences work
- Badge count accurate
- Deep linking works
- Tests passing

**Testing:**
- Integration tests for notification sending
- Manual testing on Android/iOS devices
- Test notification preferences
- Test deep linking

**Rollback Strategy:** Revert migration and code

**Dependencies:** T-204 (like/comment), T-302 (chat)

---

### Phase 4: Subscription & Monetization

#### T-401: Implement Subscription Backend
**Purpose:** Create subscription management with free vs premium tiers.

**Steps:**
1. Create database table: `subscriptions`, `usage_logs`
2. Define subscription tiers in config:
   - Free: 100 posts/day, 500 chats/day, no images, ads
   - Premium: unlimited posts/chats, images, filters, badge, no ads
3. Create backend endpoints:
   - `GET /subscriptions/me`
   - `POST /subscriptions/upgrade` (integrate payment gateway later)
   - `POST /subscriptions/cancel`
4. Implement usage tracking (posts, chats per day)
5. Add quota enforcement middleware
6. Create subscription badge logic (add to user profile)
7. Implement duration stats (premium feature)

**Expected Artifacts:**
- `server/src/main/kotlin/subscription/SubscriptionService.kt`
- `server/src/main/kotlin/subscription/SubscriptionRoutes.kt`
- Database migration: `007_subscriptions.sql`

**Definition of Done:**
- Subscription tiers defined and enforced
- Usage tracking functional
- Quota enforcement works
- Premium badge displayed
- Integration tests passing

**Testing:**
- Integration tests for subscription endpoints
- Test quota enforcement (free user exceeds limit)
- Test premium features (image upload)

**Rollback Strategy:** Revert migration and code

**Dependencies:** T-202 (posts), T-301 (chat)

---

#### T-402: Implement Ads Integration
**Purpose:** Display ads for free users on timeline, message, and profile screens.

**Steps:**
1. Integrate ad SDK (AdMob for Android, AdMob/AdColony for iOS)
2. Create `composeApp/src/commonMain/kotlin/ui/ads/AdBanner.kt` component
3. Add ads to timeline (every 5 posts)
4. Add ads to message screen (bottom banner)
5. Add ads to profile screen (top banner)
6. Implement ad-free logic for premium users
7. Add ad loading/error handling
8. Track ad impressions (analytics)

**Expected Artifacts:**
- Ad integration in `androidMain` and `iosMain`
- `AdBanner.kt` component
- Ad placement in timeline, message, profile screens

**Definition of Done:**
- Ads displayed for free users
- Ads hidden for premium users
- Ad loading/error handled gracefully
- Ad impressions tracked
- Manual testing on devices

**Testing:**
- Manual testing on Android/iOS devices
- Test ad-free for premium users
- Test ad loading errors

**Rollback Strategy:** Remove ad components

**Dependencies:** T-401 (subscription), T-203 (timeline), T-302 (chat)

---

#### T-403: Implement Subscription UI
**Purpose:** Create subscription management UI with upgrade/cancel flows.

**Steps:**
1. Create `composeApp/src/commonMain/kotlin/ui/subscription/SubscriptionScreen.kt`
2. Display current tier, usage stats, benefits comparison
3. Implement upgrade flow (payment integration placeholder)
4. Implement cancel flow with confirmation
5. Add subscription badge to user profile
6. Display duration stats for premium users
7. Add paywall for premium features (search, image upload)

**Expected Artifacts:**
- Subscription screens in `composeApp/src/commonMain/kotlin/ui/subscription/`
- Paywall components

**Definition of Done:**
- Users can view subscription status
- Users can upgrade (placeholder payment)
- Users can cancel subscription
- Premium badge displayed
- Paywall blocks premium features for free users
- UI tests passing

**Testing:**
- UI tests for subscription screens
- Test paywall enforcement
- Test upgrade/cancel flows

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-401 (subscription backend)

---

### Phase 5: Search, Safety & Moderation

#### T-501: Implement Search Backend
**Purpose:** Create search functionality for subscribers (name, username, post content).

**Steps:**
1. Choose search approach: PostgreSQL full-text search or OpenSearch
2. If PostgreSQL: Create GIN index on `users.name`, `users.username`, `posts.content`
3. If OpenSearch: Setup OpenSearch cluster, create indexes, sync data
4. Create backend endpoints:
   - `GET /search/users?q=query`
   - `GET /search/posts?q=query`
5. Implement combined search: `GET /search?q=query&type=all|users|posts`
6. Add geo-filtering for post search (within radius)
7. Enforce subscriber-only access
8. Implement search ranking/relevance
9. Add pagination

**Expected Artifacts:**
- `server/src/main/kotlin/search/SearchService.kt`
- `server/src/main/kotlin/search/SearchRoutes.kt`
- Database migration: `008_search_indexes.sql` (if PostgreSQL)
- OpenSearch setup (if chosen)

**Definition of Done:**
- Search returns relevant results
- Subscriber-only access enforced
- Geo-filtering works for posts
- Pagination functional
- Performance acceptable (<200ms p95)
- Integration tests passing

**Testing:**
- Integration tests for search endpoints
- Test relevance ranking
- Test subscriber-only enforcement
- Performance tests

**Rollback Strategy:** Revert migration/code, remove OpenSearch if used

**Dependencies:** T-401 (subscription), T-202 (posts), T-103 (users)

---

#### T-502: Implement Search UI
**Purpose:** Create search interface for subscribers.

**Steps:**
1. Create `composeApp/src/commonMain/kotlin/ui/search/SearchScreen.kt`
2. Implement search bar with debounced input
3. Display search results (users, posts) with tabs
4. Add filters: distance (for posts), date range
5. Implement result item click (navigate to profile/post detail)
6. Add empty state (no results)
7. Show paywall for non-subscribers

**Expected Artifacts:**
- Search screen in `composeApp/src/commonMain/kotlin/ui/search/`
- `shared/src/commonMain/kotlin/data/SearchRepository.kt`

**Definition of Done:**
- Search UI functional with debounced input
- Results displayed correctly
- Filters work
- Paywall shown for non-subscribers
- UI tests passing

**Testing:**
- UI tests for search screen
- Test debouncing
- Test filters
- Test paywall

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-501 (search backend), T-403 (subscription UI)

---

#### T-503: Implement Report & Block
**Purpose:** Allow users to report and block other users or posts.

**Steps:**
1. Create database tables: `reports`, `blocks`
2. Create backend endpoints:
   - `POST /reports` (report user or post)
   - `POST /blocks` (block user)
   - `GET /blocks` (list blocked users)
   - `DELETE /blocks/:id` (unblock)
3. Implement report types: spam, harassment, inappropriate_content, other
4. Add report reason (text field)
5. Filter blocked users from timeline, search, chat
6. Prevent blocked users from messaging/interacting
7. Create moderation queue (admin panel)

**Expected Artifacts:**
- `server/src/main/kotlin/moderation/ModerationRoutes.kt`
- `server/src/main/kotlin/moderation/ModerationService.kt`
- Database migration: `009_moderation.sql`

**Definition of Done:**
- Users can report users/posts
- Users can block/unblock users
- Blocked users filtered from content
- Blocked users cannot interact
- Integration tests passing

**Testing:**
- Integration tests for report/block endpoints
- Test filtering of blocked users
- Test interaction prevention

**Rollback Strategy:** Revert migration and code

**Dependencies:** T-202 (posts), T-301 (chat)

---

#### T-504: Implement Report & Block UI
**Purpose:** Create UI for reporting and blocking.

**Steps:**
1. Add "Report" and "Block" options to post/profile menus
2. Create `composeApp/src/commonMain/kotlin/ui/moderation/ReportDialog.kt`
3. Create `composeApp/src/commonMain/kotlin/ui/settings/BlockedUsersScreen.kt`
4. Implement report form (type, reason)
5. Add confirmation dialogs for block/unblock
6. Display blocked users list in settings
7. Show feedback after report/block (toast/snackbar)

**Expected Artifacts:**
- Report/block UI components
- Blocked users screen

**Definition of Done:**
- Users can report from post/profile
- Users can block from profile
- Blocked users list accessible in settings
- Confirmation dialogs work
- UI tests passing

**Testing:**
- UI tests for report/block flows
- Test confirmation dialogs

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-503 (report/block backend)

---

#### T-505: Implement Admin Panel
**Purpose:** Create web-based admin panel for moderation and analytics.

**Steps:**
1. Create separate admin web app (Ktor + HTML/JS or React)
2. Implement admin authentication (separate from user auth)
3. Create admin dashboard:
   - User management (list, suspend, unblock, delete)
   - Post management (list, delete)
   - Message management (view, delete)
   - Report queue (list, resolve, action)
   - Analytics (user count, post count, active users, revenue)
   - Audit log (admin actions)
4. Create backend endpoints:
   - `GET /admin/users`
   - `PUT /admin/users/:id/suspend`
   - `DELETE /admin/posts/:id`
   - `GET /admin/reports`
   - `PUT /admin/reports/:id/resolve`
   - `GET /admin/analytics`
   - `GET /admin/audit-log`
5. Implement role-based access control (admin, moderator)
6. Add audit logging for all admin actions

**Expected Artifacts:**
- Admin web app in `admin/` directory
- Admin backend routes in `server/src/main/kotlin/admin/`
- Database migration: `010_admin.sql`

**Definition of Done:**
- Admin panel accessible via web
- All management features functional
- Analytics displayed correctly
- Audit log tracks all actions
- Role-based access enforced
- Integration tests passing

**Testing:**
- Integration tests for admin endpoints
- Manual testing of admin panel
- Test role-based access

**Rollback Strategy:** Revert admin code and migration

**Dependencies:** T-503 (moderation), T-401 (subscription for analytics)

---

### Phase 6: Offline-First & Sync

#### T-601: Implement Local Database
**Purpose:** Setup SQLDelight for offline data storage.

**Steps:**
1. Add SQLDelight dependencies to `shared` module
2. Create database schema in `shared/src/commonMain/sqldelight/`
3. Define tables: `LocalUser`, `LocalPost`, `LocalMessage`, `LocalNotification`
4. Add sync metadata: `last_synced_at`, `is_dirty`, `sync_status`
5. Implement DAO layer in `shared/src/commonMain/kotlin/data/local/`
6. Create platform-specific database drivers (Android: AndroidSqliteDriver, iOS: NativeSqliteDriver)
7. Implement database migrations

**Expected Artifacts:**
- SQLDelight schema in `shared/src/commonMain/sqldelight/`
- DAO layer in `shared/src/commonMain/kotlin/data/local/`
- Platform-specific drivers

**Definition of Done:**
- SQLDelight database functional on Android and iOS
- All tables created with sync metadata
- DAO layer implemented
- Database migrations work
- Unit tests passing

**Testing:**
- Unit tests for DAO operations
- Test database migrations
- Test on Android and iOS

**Rollback Strategy:** Remove SQLDelight dependencies and code

**Dependencies:** T-003 (domain models)

---

#### T-602: Implement Sync Service
**Purpose:** Create sync logic to synchronize local and remote data.

**Steps:**
1. Create `shared/src/commonMain/kotlin/sync/SyncService.kt`
2. Implement sync strategies:
   - Pull: Fetch updates from server (last_modified > last_synced_at)
   - Push: Upload local changes (is_dirty = true)
   - Conflict resolution: Last-write-wins or user prompt
3. Add sync endpoints in backend:
   - `GET /sync/posts?since=timestamp`
   - `GET /sync/messages?since=timestamp`
   - `POST /sync/posts` (batch upload)
4. Implement incremental sync (only changed data)
5. Add sync status tracking (syncing, success, error)
6. Implement background sync (periodic, on network change)
7. Handle sync errors (retry with exponential backoff)
8. Add sync conflict resolution UI (optional for MVP)

**Expected Artifacts:**
- `shared/src/commonMain/kotlin/sync/SyncService.kt`
- Sync endpoints in backend
- Background sync workers (Android: WorkManager, iOS: Background Tasks)

**Definition of Done:**
- Sync pulls updates from server
- Sync pushes local changes to server
- Conflict resolution works
- Background sync functional
- Sync errors handled gracefully
- Integration tests passing

**Testing:**
- Integration tests for sync endpoints
- Test conflict resolution
- Test background sync
- Test sync error handling

**Rollback Strategy:** Revert sync code

**Dependencies:** T-601 (local database), T-202 (posts), T-301 (chat)

---

#### T-603: Implement Offline Indicators
**Purpose:** Display offline status and sync progress to users.

**Steps:**
1. Create `shared/src/commonMain/kotlin/network/NetworkMonitor.kt`
2. Implement network connectivity detection (Android: ConnectivityManager, iOS: NWPathMonitor)
3. Add offline indicator in app bar (banner or icon)
4. Display sync status (syncing, synced, error)
5. Show cached data indicator on posts/messages
6. Add manual sync trigger (pull-to-refresh)
7. Handle offline actions (queue for sync)

**Expected Artifacts:**
- `shared/src/commonMain/kotlin/network/NetworkMonitor.kt`
- Offline indicator UI components
- Platform-specific network monitoring

**Definition of Done:**
- Offline indicator displayed when offline
- Sync status visible to users
- Cached data indicated
- Manual sync trigger works
- Offline actions queued
- UI tests passing

**Testing:**
- UI tests for offline indicators
- Test network connectivity detection
- Test manual sync trigger

**Rollback Strategy:** Revert UI changes

**Dependencies:** T-602 (sync service)

---

### Phase 7: Polish, Performance & Launch Prep

#### T-701: Optimize Geo Query Performance
**Purpose:** Ensure geo queries meet <100ms p95 target.

**Steps:**
1. Run performance tests with 1M posts
2. Analyze EXPLAIN ANALYZE output for geo queries
3. Optimize GiST index parameters (fillfactor, buffering)
4. Consider SP-GiST index for better performance
5. Implement query result caching (Redis)
6. Add database connection pooling (HikariCP)
7. Optimize query: use covering index, limit columns
8. Implement geo query cost monitoring (log slow queries)
9. Add query timeout (5 seconds)
10. Document optimization decisions in `DECISIONS.md`

**Expected Artifacts:**
- Performance test results
- Optimized database indexes
- Redis caching layer
- Updated `DECISIONS.md`

**Definition of Done:**
- Geo queries <100ms p95 with 1M posts
- Caching reduces database load
- Slow queries logged
- Query timeout enforced
- Performance tests passing

**Testing:**
- Performance tests with 1M posts
- Load tests with concurrent queries
- Verify caching effectiveness

**Rollback Strategy:** Revert index/caching changes

**Dependencies:** T-201 (geo queries)

---

#### T-702: Implement Rate Limiting
**Purpose:** Protect API from abuse and ensure fair usage.

**Steps:**
1. Add rate limiting middleware (Ktor: ktor-server-rate-limit or custom)
2. Define rate limits per endpoint:
   - Auth: 5 requests/hour (OTP)
   - Posts: 100 requests/hour (free), unlimited (premium)
   - Chat: 500 requests/hour (free), unlimited (premium)
   - Search: 50 requests/hour (subscribers only)
3. Implement rate limit storage (Redis)
4. Return 429 Too Many Requests with Retry-After header
5. Add rate limit headers (X-RateLimit-Limit, X-RateLimit-Remaining)
6. Implement IP-based and user-based rate limiting
7. Add rate limit bypass for admin users
8. Log rate limit violations

**Expected Artifacts:**
- Rate limiting middleware in `server/src/main/kotlin/middleware/RateLimiter.kt`
- Redis integration for rate limit storage

**Definition of Done:**
- Rate limits enforced per endpoint
- 429 responses returned correctly
- Rate limit headers included
- IP and user-based limiting work
- Admin bypass functional
- Integration tests passing

**Testing:**
- Integration tests for rate limiting
- Test 429 responses
- Test rate limit headers
- Test admin bypass

**Rollback Strategy:** Disable rate limiting middleware

**Dependencies:** T-101 (auth), T-202 (posts), T-301 (chat)

---

#### T-703: Implement Logging & Monitoring
**Purpose:** Setup structured logging and monitoring for observability.

**Steps:**
1. Configure structured logging (Logback with JSON encoder)
2. Add log levels: DEBUG, INFO, WARN, ERROR
3. Log key events: auth, post creation, chat, errors
4. Implement request/response logging (exclude sensitive data)
5. Setup monitoring (Prometheus + Grafana or Datadog)
6. Add metrics: request count, latency, error rate, database query time
7. Create dashboards: API health, geo query performance, user activity
8. Setup alerting: high error rate, slow queries, service down
9. Implement distributed tracing (optional: Jaeger or Zipkin)
10. Document logging/monitoring in `INFRA.md`

**Expected Artifacts:**
- Structured logging configuration
- Prometheus/Grafana setup
- Dashboards and alerts
- Updated `INFRA.md`

**Definition of Done:**
- Structured logs generated
- Metrics collected and visualized
- Dashboards functional
- Alerts configured
- Distributed tracing working (if implemented)
- Documentation complete

**Testing:**
- Verify logs generated correctly
- Test metrics collection
- Trigger alerts manually

**Rollback Strategy:** Disable monitoring integrations

**Dependencies:** T-002 (database), T-101 (auth), T-201 (geo queries)

---

#### T-704: Implement Spam Mitigation
**Purpose:** Prevent spam posts, messages, and abuse.

**Steps:**
1. Implement content filtering (basic keyword blacklist)
2. Add CAPTCHA for suspicious activity (optional)
3. Implement velocity checks (max 10 posts/hour, 50 messages/hour)
4. Add duplicate content detection (hash-based)
5. Implement user reputation system (trust score)
6. Auto-flag suspicious content for moderation
7. Add honeypot fields in forms (bot detection)
8. Implement IP-based blocking for repeat offenders
9. Log spam attempts for analysis

**Expected Artifacts:**
- Spam filtering logic in `server/src/main/kotlin/spam/SpamFilter.kt`
- Reputation system in database
- Updated moderation queue

**Definition of Done:**
- Content filtering blocks spam keywords
- Velocity checks enforced
- Duplicate content detected
- Suspicious content flagged
- IP blocking functional
- Integration tests passing

**Testing:**
- Integration tests for spam filtering
- Test velocity checks
- Test duplicate detection

**Rollback Strategy:** Disable spam filtering

**Dependencies:** T-202 (posts), T-301 (chat), T-503 (moderation)

---

#### T-705: Security Audit
**Purpose:** Conduct security review and fix vulnerabilities.

**Steps:**
1. Review authentication: JWT security, token expiry, refresh logic
2. Review authorization: endpoint access control, ownership verification
3. Review input validation: SQL injection, XSS, CSRF
4. Review data privacy: chat metadata, location data, PII handling
5. Review encryption: data at rest, data in transit (HTTPS)
6. Review dependencies: update vulnerable libraries
7. Implement security headers (CSP, HSTS, X-Frame-Options)
8. Add CORS configuration (restrict origins)
9. Implement CSRF protection (for web admin panel)
10. Run automated security scan (OWASP ZAP or similar)
11. Document security measures in `DECISIONS.md`

**Expected Artifacts:**
- Security audit report
- Fixed vulnerabilities
- Security headers configured
- Updated `DECISIONS.md`

**Definition of Done:**
- All critical vulnerabilities fixed
- Security headers implemented
- CORS configured correctly
- CSRF protection added
- Automated scan passes
- Documentation complete

**Testing:**
- Manual security testing
- Automated security scan
- Penetration testing (optional)

**Rollback Strategy:** Revert security changes if issues arise

**Dependencies:** T-101 (auth), T-301 (chat), T-505 (admin panel)

---

#### T-706: Load Testing
**Purpose:** Verify system performance under load.

**Steps:**
1. Setup load testing tool (Gatling, k6, or JMeter)
2. Create load test scenarios:
   - 1000 concurrent users browsing timeline
   - 500 concurrent users creating posts
   - 200 concurrent users chatting
   - 100 concurrent geo queries
3. Run load tests and collect metrics
4. Identify bottlenecks (database, API, network)
5. Optimize based on results (caching, indexing, scaling)
6. Test auto-scaling (if using Kubernetes)
7. Document load test results and optimizations in `INFRA.md`

**Expected Artifacts:**
- Load test scripts
- Load test results report
- Performance optimizations
- Updated `INFRA.md`

**Definition of Done:**
- Load tests run successfully
- System handles target load (1000 concurrent users)
- Bottlenecks identified and addressed
- Auto-scaling tested (if applicable)
- Documentation complete

**Testing:**
- Run load tests
- Verify metrics during load
- Test auto-scaling

**Rollback Strategy:** Revert performance optimizations if issues arise

**Dependencies:** T-701 (geo optimization), T-702 (rate limiting), T-703 (monitoring)

---

#### T-707: Deployment Setup
**Purpose:** Prepare production deployment infrastructure.

**Steps:**
1. Create Dockerfile for server application
2. Create Docker Compose for local development (server, PostgreSQL, Redis)
3. Create Kubernetes manifests (Deployment, Service, Ingress, ConfigMap, Secret)
4. Setup cloud infrastructure (AWS, GCP, or Azure):
   - Kubernetes cluster (EKS, GKE, or AKS)
   - PostgreSQL (RDS, Cloud SQL, or Azure Database)
   - Redis (ElastiCache, Memorystore, or Azure Cache)
   - S3/GCS for media storage
   - Load balancer
5. Configure CI/CD for deployment (GitHub Actions or GitLab CI)
6. Setup environment variables and secrets management
7. Configure SSL/TLS certificates (Let's Encrypt or cloud provider)
8. Setup database backups (automated daily backups)
9. Create deployment runbook in `INFRA.md`
10. Test deployment to staging environment

**Expected Artifacts:**
- `Dockerfile`
- `docker-compose.yml`
- Kubernetes manifests in `k8s/`
- CI/CD deployment pipeline
- Updated `INFRA.md`

**Definition of Done:**
- Docker image builds successfully
- Kubernetes manifests deploy correctly
- Cloud infrastructure provisioned
- CI/CD deploys to staging
- SSL/TLS configured
- Database backups automated
- Runbook complete

**Testing:**
- Test Docker build and run
- Test Kubernetes deployment
- Test staging environment
- Verify SSL/TLS

**Rollback Strategy:** Rollback deployment via CI/CD

**Dependencies:** T-703 (monitoring), T-705 (security)

---

#### T-708: API Documentation
**Purpose:** Create comprehensive API documentation.

**Steps:**
1. Choose documentation tool (OpenAPI/Swagger, Postman, or custom)
2. Document all API endpoints with:
   - Method, path, description
   - Request parameters, headers, body
   - Response codes, body, examples
   - Authentication requirements
   - Rate limits
3. Add code examples (cURL, JavaScript, Kotlin)
4. Generate interactive API docs (Swagger UI or Redoc)
5. Host API docs (GitHub Pages or dedicated server)
6. Create API versioning strategy
7. Document in `docs/API.md`

**Expected Artifacts:**
- OpenAPI specification (`openapi.yaml`)
- Interactive API docs (Swagger UI)
- `docs/API.md`

**Definition of Done:**
- All endpoints documented
- Interactive docs accessible
- Code examples provided
- Versioning strategy defined
- Documentation hosted

**Testing:**
- Verify all endpoints documented
- Test interactive docs
- Validate OpenAPI spec

**Rollback Strategy:** N/A (documentation only)

**Dependencies:** All backend tasks (T-101 to T-505)

---

#### T-709: Final Testing & QA
**Purpose:** Comprehensive testing before launch.

**Steps:**
1. Run full test suite (unit, integration, E2E)
2. Manual testing on Android and iOS devices
3. Test all user flows: signup, post, chat, subscribe, search, report
4. Test edge cases: offline, poor network, low storage
5. Test accessibility (screen readers, font scaling)
6. Test internationalization (if implemented)
7. Perform regression testing
8. Fix all critical and high-priority bugs
9. Create bug report in issue tracker
10. Update `TESTING.md` with test results

**Expected Artifacts:**
- Test results report
- Bug report
- Updated `TESTING.md`

**Definition of Done:**
- All tests passing
- All critical bugs fixed
- Manual testing complete
- Accessibility tested
- Test results documented

**Testing:**
- Full test suite execution
- Manual testing on devices
- Accessibility testing

**Rollback Strategy:** N/A (testing phase)

**Dependencies:** All tasks (T-001 to T-708)

---

#### T-710: Launch Preparation
**Purpose:** Final steps before production launch.

**Steps:**
1. Deploy to production environment
2. Verify production deployment (smoke tests)
3. Setup monitoring and alerting for production
4. Prepare rollback plan
5. Create launch checklist
6. Notify stakeholders of launch
7. Monitor production for first 24 hours
8. Prepare incident response plan
9. Update `CHANGELOG.md` with version 1.0.0
10. Celebrate launch! 🎉

**Expected Artifacts:**
- Production deployment
- Launch checklist
- Incident response plan
- Updated `CHANGELOG.md`

**Definition of Done:**
- Production deployment successful
- Smoke tests passing
- Monitoring active
- Rollback plan ready
- Stakeholders notified
- Incident response plan documented

**Testing:**
- Smoke tests in production
- Monitor production metrics

**Rollback Strategy:** Execute rollback plan if critical issues arise

**Dependencies:** T-709 (final testing), T-707 (deployment)

---

## Progress Ledger (PL-###)

**Instructions:** Update this ledger after completing each task. Add new entries with incremental PL numbers. Status: TODO, IN_PROGRESS, DONE, BLOCKED, CANCELLED.

| PL-ID | Task ID | Task Name | Status | Started | Completed | Notes |
|-------|---------|-----------|--------|---------|-----------|-------|
| PL-001 | T-001 | Create Project Documentation | DONE | 2025-10-16 | 2025-10-16 | All 6 documentation files created |
| PL-002 | T-002 | Setup PostgreSQL with PostGIS | DONE | 2025-10-16 | 2025-10-16 | Docker Compose, init.sql, migrations created |
| PL-003 | T-003 | Define Shared Domain Models | DONE | 2025-10-16 | 2025-10-16 | Domain models and validation logic with tests |
| PL-004 | T-004 | Setup CI/CD Pipeline | DONE | 2025-10-16 | 2025-10-16 | GitHub Actions workflow and Dockerfile created |
| PL-005 | T-101 | Implement Backend Auth Service | TODO | - | - | - |
| PL-006 | T-102 | Implement Frontend Auth Flows | TODO | - | - | - |
| PL-007 | T-103 | Implement User Profile Management | TODO | - | - | - |
| PL-008 | T-201 | Implement PostGIS Geo Queries | TODO | - | - | - |
| PL-009 | T-202 | Implement Post Creation & CRUD | TODO | - | - | - |
| PL-010 | T-203 | Implement Timeline UI | TODO | - | - | - |
| PL-011 | T-204 | Implement Like & Comment | TODO | - | - | - |
| PL-012 | T-301 | Implement Chat Backend | TODO | - | - | - |
| PL-013 | T-302 | Implement Chat UI | TODO | - | - | - |
| PL-014 | T-303 | Implement FCM Notifications | TODO | - | - | - |
| PL-015 | T-401 | Implement Subscription Backend | TODO | - | - | - |
| PL-016 | T-402 | Implement Ads Integration | TODO | - | - | - |
| PL-017 | T-403 | Implement Subscription UI | TODO | - | - | - |
| PL-018 | T-501 | Implement Search Backend | TODO | - | - | - |
| PL-019 | T-502 | Implement Search UI | TODO | - | - | - |
| PL-020 | T-503 | Implement Report & Block | TODO | - | - | - |
| PL-021 | T-504 | Implement Report & Block UI | TODO | - | - | - |
| PL-022 | T-505 | Implement Admin Panel | TODO | - | - | - |
| PL-023 | T-601 | Implement Local Database | TODO | - | - | - |
| PL-024 | T-602 | Implement Sync Service | TODO | - | - | - |
| PL-025 | T-603 | Implement Offline Indicators | TODO | - | - | - |
| PL-026 | T-701 | Optimize Geo Query Performance | TODO | - | - | - |
| PL-027 | T-702 | Implement Rate Limiting | TODO | - | - | - |
| PL-028 | T-703 | Implement Logging & Monitoring | TODO | - | - | - |
| PL-029 | T-704 | Implement Spam Mitigation | TODO | - | - | - |
| PL-030 | T-705 | Security Audit | TODO | - | - | - |
| PL-031 | T-706 | Load Testing | TODO | - | - | - |
| PL-032 | T-707 | Deployment Setup | TODO | - | - | - |
| PL-033 | T-708 | API Documentation | TODO | - | - | - |
| PL-034 | T-709 | Final Testing & QA | TODO | - | - | - |
| PL-035 | T-710 | Launch Preparation | TODO | - | - | - |

---

## Changelog (CL-###)

**Instructions:** Add entries when significant changes occur (task completion, architecture changes, blockers resolved). Use incremental CL numbers.

| CL-ID | Date | Type | Description | Related Tasks |
|-------|------|------|-------------|---------------|
| CL-001 | 2025-10-16 | PLAN_CREATED | Initial MVP execution plan created | All |
| CL-002 | 2025-10-16 | PHASE_COMPLETE | Phase 0 (M0) Foundation & Setup completed | T-001, T-002, T-003, T-004 |

---

## Deliverables per Phase

### Phase 0: Foundation & Setup ✅ COMPLETE
- [x] `docs/CORE/SPEC.md` - Product specification
- [x] `docs/CORE/ARCHITECTURE.md` - System architecture
- [x] `docs/CORE/DECISIONS.md` - Architectural decisions
- [x] `docs/CORE/INFRA.md` - Infrastructure documentation
- [x] `docs/CORE/TESTING.md` - Testing strategy
- [x] `docs/CORE/CHANGELOG.md` - Version history
- [x] PostgreSQL + PostGIS running in Docker
- [x] Shared domain models in `/shared/commonMain`
- [x] CI/CD pipeline functional

### Phase 1: Authentication & User Management
- [ ] Backend auth endpoints (register, login, verify OTP, refresh)
- [ ] Frontend auth screens (login, signup, OTP verification)
- [ ] JWT token generation and validation
- [ ] Secure token storage (Keystore/Keychain)
- [ ] User profile management (view, edit)
- [ ] Profile photo upload (S3/GCS)

### Phase 2: Core Timeline & Posts
- [ ] PostGIS geo queries with GiST index
- [ ] Post CRUD endpoints
- [ ] Timeline UI (Nearby, Following)
- [ ] Post creation UI with media upload (premium)
- [ ] Like/comment functionality
- [ ] Post detail screen

### Phase 3: Messaging & Notifications
- [ ] Chat backend with post context
- [ ] Chat UI with message bubbles
- [ ] FCM integration (Android, iOS)
- [ ] Notification types (like, comment, follow, new_message)
- [ ] Notification preferences
- [ ] Deep linking from notifications

### Phase 4: Subscription & Monetization
- [ ] Subscription backend (free vs premium)
- [ ] Usage tracking and quota enforcement
- [ ] Ads integration (AdMob)
- [ ] Subscription UI (upgrade, cancel)
- [ ] Premium badge and features
- [ ] Paywall for premium features

### Phase 5: Search, Safety & Moderation
- [ ] Search backend (PostgreSQL or OpenSearch)
- [ ] Search UI with filters
- [ ] Report/block backend
- [ ] Report/block UI
- [ ] Admin panel (web)
- [ ] Moderation queue and analytics

### Phase 6: Offline-First & Sync
- [ ] SQLDelight local database
- [ ] Sync service (pull, push, conflict resolution)
- [ ] Background sync (Android: WorkManager, iOS: Background Tasks)
- [ ] Offline indicators
- [ ] Manual sync trigger

### Phase 7: Polish, Performance & Launch Prep
- [ ] Geo query optimization (<100ms p95)
- [ ] Rate limiting (per endpoint, per user)
- [ ] Structured logging and monitoring
- [ ] Spam mitigation
- [ ] Security audit and fixes
- [ ] Load testing (1000 concurrent users)
- [ ] Production deployment (Kubernetes)
- [ ] API documentation (OpenAPI)
- [ ] Final QA and bug fixes
- [ ] Launch! 🚀

---

## Architecture & Design

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│  ┌──────────────────┐              ┌──────────────────┐     │
│  │   Android App    │              │     iOS App      │     │
│  │  (Compose UI)    │              │  (Compose UI)    │     │
│  └────────┬─────────┘              └────────┬─────────┘     │
│           │                                  │               │
│           └──────────────┬───────────────────┘               │
│                          │                                   │
│                ┌─────────▼─────────┐                         │
│                │  Shared Module    │                         │
│                │  (Domain Logic,   │                         │
│                │   Data Models,    │                         │
│                │   Validation)     │                         │
│                └─────────┬─────────┘                         │
└──────────────────────────┼───────────────────────────────────┘
                           │
                           │ HTTP/WebSocket
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                      Backend (Ktor)                          │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  API Layer (Routes, Controllers)                       │  │
│  └────────────────────┬───────────────────────────────────┘  │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  Application Layer (Services, Use Cases)               │  │
│  └────────────────────┬───────────────────────────────────┘  │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  Domain Layer (Business Logic, Entities)               │  │
│  └────────────────────┬───────────────────────────────────┘  │
│  ┌────────────────────▼───────────────────────────────────┐  │
│  │  Infrastructure Layer (Repositories, External Services)│  │
│  └────────────────────┬───────────────────────────────────┘  │
└───────────────────────┼────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬──────────────┐
        │               │               │              │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼─────┐ ┌─────▼─────┐
│ PostgreSQL   │ │   Redis     │ │  S3/GCS   │ │    FCM    │
│  + PostGIS   │ │  (Cache)    │ │  (Media)  │ │ (Notif.)  │
└──────────────┘ └─────────────┘ └───────────┘ └───────────┘
```

### Module Boundaries

#### `/shared` Module
- **Purpose:** Shared business logic, domain models, validation
- **Targets:** commonMain (Android, iOS, JVM)
- **Contents:**
  - `domain/model/` - Data classes (User, Post, Message, etc.)
  - `domain/validation/` - Validation logic
  - `data/` - Repository interfaces
  - `sync/` - Sync logic
  - `network/` - Network monitoring

#### `/composeApp` Module
- **Purpose:** UI layer with Compose Multiplatform
- **Targets:** Android, iOS
- **Contents:**
  - `ui/` - Screens, components, ViewModels
  - `navigation/` - Navigation logic
  - Platform-specific code in `androidMain` and `iosMain`

#### `/server` Module
- **Purpose:** Backend API with Ktor
- **Target:** JVM
- **Contents:**
  - `auth/` - Authentication service
  - `user/` - User management
  - `post/` - Post CRUD and geo queries
  - `chat/` - Messaging service
  - `notification/` - FCM integration
  - `subscription/` - Subscription management
  - `search/` - Search service
  - `moderation/` - Report/block, admin panel
  - `admin/` - Admin API
  - `middleware/` - Rate limiting, logging
  - `config/` - Configuration

### Data Flow

1. **User Action** → UI (Compose) → ViewModel
2. **ViewModel** → Repository (shared) → API call (Ktor client)
3. **API** → Backend (Ktor server) → Service layer
4. **Service** → Repository → Database (PostgreSQL)
5. **Response** → Service → API → Repository → ViewModel → UI

### Database Schema (Key Tables)

#### users
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  name VARCHAR(100) NOT NULL,
  bio TEXT,
  photo_url TEXT,
  is_verified BOOLEAN DEFAULT FALSE,
  is_premium BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

#### posts
```sql
CREATE TABLE posts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  media_url TEXT,
  location GEOGRAPHY(Point, 4326) NOT NULL,
  like_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  is_deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_posts_location ON posts USING GIST(location);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
```

#### messages
```sql
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  post_context_id UUID REFERENCES posts(id) ON DELETE SET NULL,
  status VARCHAR(20) DEFAULT 'sent', -- sent, delivered, read
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
```

### Caching Strategy

- **Redis:** Cache geo query results (5-minute TTL), user sessions, rate limit counters
- **Local (SQLDelight):** Cache posts, messages, user profiles for offline access
- **CDN:** Cache media files (S3/GCS with CloudFront/Cloud CDN)

### Authentication Flow

1. User enters email/phone → Backend generates OTP → OTP sent via email/SMS
2. User enters OTP → Backend verifies → JWT access token (7-day expiry) + refresh token (30-day expiry) returned
3. Client stores tokens securely (Keystore/Keychain)
4. Client includes access token in Authorization header for API requests
5. Backend validates JWT on each request
6. Client refreshes access token using refresh token when expired

### Privacy & Security

- **Chat Metadata:** Only conversation participants can view messages and metadata (who, when)
- **Location Data:** Stored as GEOGRAPHY type, never exposed in raw form (only distance shown)
- **PII:** Email/phone hashed in logs, never logged in plaintext
- **Encryption:** HTTPS for data in transit, database encryption at rest
- **JWT:** Signed with HS256, includes user ID and expiry

### Sync Policy

- **Pull Sync:** Fetch updates every 15 minutes or on app open (last_modified > last_synced_at)
- **Push Sync:** Upload local changes immediately when online, queue when offline
- **Conflict Resolution:** Last-write-wins (server timestamp authoritative)
- **Incremental Sync:** Only fetch changed records (delta sync)

---

## Quality & Operations

### Testing Strategy

#### Unit Tests
- **Shared Module:** Domain models, validation logic, sync logic
- **Backend:** Services, repositories, utilities
- **Target Coverage:** >80%

#### Integration Tests
- **Backend:** API endpoints, database operations, external service integrations
- **Auth flows, CRUD operations, geo queries, notifications**

#### UI Tests
- **Compose Multiplatform:** Screen rendering, user interactions, navigation
- **Key flows:** Login, post creation, chat, subscription

#### E2E Tests
- **Full user journeys:** Signup → Post → Chat → Subscribe
- **Tools:** Appium or Maestro for mobile

#### Performance Tests
- **Geo queries:** <100ms p95 with 1M posts
- **Load tests:** 1000 concurrent users
- **Tools:** Gatling, k6, or JMeter

#### Security Tests
- **Automated scans:** OWASP ZAP
- **Manual testing:** Auth, authorization, input validation

### CI/CD Pipeline

```yaml
# .github/workflows/ci.yml
name: CI/CD

on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run ktlint
        run: ./gradlew ktlintCheck

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Android
        run: ./gradlew :composeApp:assembleDebug
      - name: Build Server
        run: ./gradlew :server:build
      - name: Build Docker image
        run: docker build -t nearyou-server:latest ./server

  deploy:
    runs-on: ubuntu-latest
    needs: [lint, test, build]
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to staging
        run: kubectl apply -f k8s/staging/
```

### Observability

#### Logging
- **Format:** Structured JSON logs
- **Levels:** DEBUG (dev), INFO (prod), WARN, ERROR
- **Key Events:** Auth, post creation, chat, errors, slow queries
- **Tool:** Logback with JSON encoder

#### Monitoring
- **Metrics:** Request count, latency (p50, p95, p99), error rate, database query time
- **Dashboards:** API health, geo query performance, user activity, subscription revenue
- **Tool:** Prometheus + Grafana or Datadog

#### Alerting
- **Triggers:** Error rate >5%, p95 latency >500ms, service down, database connection pool exhausted
- **Channels:** Slack, PagerDuty, email

#### Distributed Tracing (Optional)
- **Tool:** Jaeger or Zipkin
- **Trace:** Request flow from client → API → database

### Spam Mitigation

- **Content Filtering:** Keyword blacklist (configurable)
- **Velocity Checks:** Max 10 posts/hour, 50 messages/hour per user
- **Duplicate Detection:** Hash-based (SHA-256 of content)
- **Reputation System:** Trust score based on user activity, reports
- **Auto-Flagging:** Suspicious content sent to moderation queue
- **IP Blocking:** Repeat offenders blocked by IP

### Geo Query Cost Control

- **Caching:** Cache geo query results in Redis (5-minute TTL)
- **Rate Limiting:** Max 100 geo queries/hour per user
- **Index Optimization:** GiST or SP-GiST index on location column
- **Query Timeout:** 5-second timeout to prevent long-running queries
- **Monitoring:** Log slow queries (>100ms), alert on high query volume

---

## Appendix

### Technology Stack Summary

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Kotlin | 2.2.20 |
| UI Framework | Compose Multiplatform | 1.9.0 |
| Backend Framework | Ktor | 3.3.0 |
| Database | PostgreSQL + PostGIS | 15+ |
| Cache | Redis | 7+ |
| Mobile Platforms | Android (minSdk 24), iOS | - |
| Build Tool | Gradle | 8.11.2 |
| Containerization | Docker | - |
| Orchestration | Kubernetes | - |
| CI/CD | GitHub Actions | - |
| Monitoring | Prometheus + Grafana | - |
| Notifications | Firebase Cloud Messaging | - |
| Media Storage | S3 / Google Cloud Storage | - |
| Search (Optional) | OpenSearch | - |

### Key Dependencies

#### Shared Module
- `kotlinx-serialization` - JSON serialization
- `kotlinx-coroutines` - Async operations
- `kotlinx-datetime` - Date/time handling
- `ktor-client` - HTTP client

#### ComposeApp Module
- `compose-multiplatform` - UI framework
- `androidx-lifecycle` - ViewModel, lifecycle
- `androidx-navigation` - Navigation
- `sqldelight` - Local database
- `coil` - Image loading

#### Server Module
- `ktor-server-core` - Web framework
- `ktor-server-netty` - Server engine
- `ktor-server-auth` - Authentication
- `ktor-server-auth-jwt` - JWT support
- `exposed` or `ktorm` - Database ORM
- `postgresql` - PostgreSQL driver
- `postgis-jdbc` - PostGIS support
- `lettuce` or `jedis` - Redis client
- `firebase-admin` - FCM integration
- `logback` - Logging

### Glossary

- **KMP:** Kotlin Multiplatform
- **PostGIS:** PostgreSQL extension for geospatial data
- **GiST:** Generalized Search Tree (index type)
- **SP-GiST:** Space-Partitioned Generalized Search Tree
- **JWT:** JSON Web Token
- **OTP:** One-Time Password
- **FCM:** Firebase Cloud Messaging
- **ADR:** Architectural Decision Record
- **p95:** 95th percentile (performance metric)
- **TTL:** Time To Live (cache expiry)
- **CRUD:** Create, Read, Update, Delete

### Useful Commands

```bash
# Start local development environment
docker-compose up -d

# Run backend server
./gradlew :server:run

# Run Android app
./gradlew :composeApp:assembleDebug

# Run tests
./gradlew test

# Run linter
./gradlew ktlintCheck

# Build Docker image
docker build -t nearyou-server:latest ./server

# Deploy to Kubernetes
kubectl apply -f k8s/production/

# View logs
kubectl logs -f deployment/nearyou-server

# Database migration
./gradlew flywayMigrate  # or custom migration tool
```

### References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor Documentation](https://ktor.io/docs/)
- [PostGIS Documentation](https://postgis.net/documentation/)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

---

## How to Resume This Plan

**This document is designed to be self-contained and resumable.** If you need to pause and resume work later, follow these steps:

1. **Open this file** (`NearYou_ID_MVP_Plan.md`) in a new chat session or after a break.

2. **Locate the Progress Ledger** section above and find the **first task with status `TODO`** or the task currently marked `IN_PROGRESS`.

3. **Read the task details** in the Work Breakdown Structure section:
   - Review the **Purpose**, **Steps**, **Expected Artifacts**, and **Definition of Done**
   - Check **Dependencies** to ensure prerequisite tasks are complete
   - Review **Testing** and **Rollback Strategy**

4. **Execute the task** by following the steps outlined.

5. **Update the Progress Ledger** when you start the task:
   - Change status from `TODO` to `IN_PROGRESS`
   - Add the start date in the `Started` column
   - Add any relevant notes

6. **Update the Progress Ledger** when you complete the task:
   - Change status from `IN_PROGRESS` to `DONE`
   - Add the completion date in the `Completed` column
   - Add any relevant notes (e.g., "Added extra validation", "Skipped OpenSearch, used PostgreSQL")

7. **Add a Changelog entry** (CL-###) for significant milestones:
   - Task completion
   - Architecture changes
   - Blockers encountered and resolved
   - Deviations from the plan

8. **Move to the next task** by repeating steps 2-7.

9. **If blocked:** Mark the task as `BLOCKED` in the Progress Ledger, add notes explaining the blocker, and move to a non-dependent task if possible.

10. **If you need to deviate from the plan:** Document the change in the Changelog and update the relevant task description if needed.

**Example Update:**

After completing T-001 (Create Project Documentation):

**Progress Ledger:**
```
| PL-001 | T-001 | Create Project Documentation | DONE | 2025-10-16 | 2025-10-16 | All docs created in /docs |
```

**Changelog:**
```
| CL-002 | 2025-10-16 | TASK_COMPLETE | Completed T-001: Created all project documentation | T-001 |
```

**That's it!** Simply paste this document into a new chat, locate the first `TODO` task in the Progress Ledger, follow its steps, then update PL and CL accordingly. Repeat until all tasks are `DONE`. 🚀

---

**End of NearYou ID MVP Execution Plan**


