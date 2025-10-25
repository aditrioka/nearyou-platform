# NearYou ID - System Architecture

**Version:** 1.1
**Last Updated:** 2025-10-22
**Status:** Active
**Best Practices Compliance:** See [BEST_PRACTICES_EVALUATION.md](BEST_PRACTICES_EVALUATION.md)

---

## Architecture Overview

NearYou ID follows a **Clean/Hexagonal Architecture** pattern with **Kotlin Multiplatform (KMP)** to share business logic across Android, iOS, and backend layers. The architecture emphasizes separation of concerns, testability, and maintainability.

### Architecture Principles

1. **Separation of Concerns:** Clear boundaries between layers
2. **Dependency Inversion:** Dependencies point inward toward domain
3. **Platform Independence:** Shared business logic across platforms
4. **Testability:** Each layer independently testable
5. **Scalability:** Horizontal scaling for backend services

### Technology Stack Compliance

This architecture follows the latest best practices for:
- **Kotlin Multiplatform 2.2.20** - Official KMP architecture patterns
- **Ktor 3.3.0** - Modern server-side Kotlin framework
- **Jetpack Compose Multiplatform 1.9.0** - Declarative UI framework
- **Koin 4.0.1** - Dependency injection for multiplatform

For detailed compliance analysis, see [BEST_PRACTICES_EVALUATION.md](BEST_PRACTICES_EVALUATION.md).

---

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
├──────────────────────────┬──────────────────────────────────────┤
│   Android App            │         iOS App                       │
│   (Compose Multiplatform)│   (Compose Multiplatform)            │
│   - UI Components        │   - UI Components                     │
│   - Platform Services    │   - Platform Services                 │
│   - Navigation           │   - Navigation                        │
└──────────────┬───────────┴──────────────┬───────────────────────┘
               │                          │
               └──────────┬───────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Shared Module (KMP)                           │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer                                                    │
│  - Models (User, Post, Message, etc.)                           │
│  - Validation Logic                                              │
│  - Business Rules                                                │
├─────────────────────────────────────────────────────────────────┤
│  Data Layer                                                      │
│  - Repositories (Auth, Post, Chat, User)                        │
│  - Network Client (Ktor)                                         │
│  - Local Database (SQLDelight)                                   │
│  - Sync Service                                                  │
└──────────────┬──────────────────────────────────────────────────┘
               │
               │ HTTP/REST API
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Backend (Ktor Server)                       │
├─────────────────────────────────────────────────────────────────┤
│  API Layer                                                       │
│  - Routes (Auth, Post, Chat, User, etc.)                        │
│  - Middleware (Auth, Rate Limiting, Logging)                    │
│  - Request/Response DTOs                                         │
├─────────────────────────────────────────────────────────────────┤
│  Service Layer                                                   │
│  - AuthService, PostService, ChatService                        │
│  - NotificationService, SubscriptionService                     │
│  - Business Logic                                                │
├─────────────────────────────────────────────────────────────────┤
│  Data Layer                                                      │
│  - Repositories (Database Access)                                │
│  - PostgreSQL + PostGIS                                          │
│  - Redis (Caching, Rate Limiting)                               │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  - PostgreSQL 15+ with PostGIS                                  │
│  - Redis (Caching, Sessions)                                     │
│  - S3/GCS (Media Storage)                                        │
│  - FCM (Push Notifications)                                      │
│  - Monitoring (Prometheus, Grafana)                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Module Structure

### 1. `/composeApp` - Frontend Application

**Purpose:** Platform-specific UI implementation using Compose Multiplatform

**Structure:**
```
composeApp/
├── src/
│   ├── commonMain/kotlin/
│   │   ├── ui/
│   │   │   ├── auth/          # Login, Signup, OTP screens
│   │   │   ├── timeline/      # Nearby, Following feeds
│   │   │   ├── post/          # Post detail, create post
│   │   │   ├── chat/          # Conversation list, chat screen
│   │   │   ├── profile/       # Profile, edit profile
│   │   │   ├── search/        # Search screen
│   │   │   ├── subscription/  # Subscription management
│   │   │   ├── settings/      # Settings, blocked users
│   │   │   └── components/    # Reusable UI components
│   │   ├── navigation/        # Navigation graph
│   │   └── theme/             # Theme, colors, typography
│   ├── androidMain/kotlin/
│   │   ├── platform/          # Android-specific implementations
│   │   ├── services/          # FCM, location services
│   │   └── MainActivity.kt
│   └── iosMain/kotlin/
│       ├── platform/          # iOS-specific implementations
│       └── services/          # APNS, location services
```

**Responsibilities:**
- UI rendering and user interactions
- Navigation between screens
- Platform-specific services (location, notifications)
- Secure token storage (Keystore/Keychain)

### 2. `/shared` - Shared Business Logic (KMP)

**Purpose:** Cross-platform business logic, domain models, and data layer. **Single source of truth for all DTOs and domain models.**

**Structure:**
```
shared/
├── src/
│   ├── commonMain/kotlin/
│   │   ├── domain/
│   │   │   ├── model/         # User, Post, Message, etc.
│   │   │   │   └── auth/      # Auth DTOs (RegisterRequest, LoginRequest, etc.)
│   │   │   └── validation/    # Validation logic
│   │   ├── data/
│   │   │   ├── AuthRepository.kt      # Auth API client
│   │   │   ├── PostRepository.kt      # Post API client
│   │   │   ├── TokenStorage.kt        # Secure token storage interface
│   │   │   ├── network/               # Ktor HTTP client
│   │   │   ├── local/                 # SQLDelight DAOs
│   │   │   └── sync/                  # Sync service
│   │   └── util/              # Utilities, extensions
│   ├── commonTest/kotlin/     # Shared tests
│   ├── androidMain/kotlin/    # Android-specific implementations
│   │   └── data/
│   │       └── TokenStorageAndroid.kt # Android Keystore implementation
│   ├── iosMain/kotlin/        # iOS-specific implementations
│   │   └── data/
│   │       └── TokenStorageIOS.kt     # iOS Keychain implementation
│   └── jvmMain/kotlin/        # JVM-specific implementations
│       └── data/
│           └── TokenStorageJVM.kt     # In-memory for testing
```

**Responsibilities:**
- **Domain models and DTOs** - Shared across client and server
- Data repositories (network + local)
- Validation logic
- Sync service for offline support
- Platform-specific secure storage implementations

**Key Principle:** All data models and DTOs are defined once in this module and imported by both `composeApp` and `server` modules to ensure type consistency and prevent serialization errors.

### 3. `/server` - Backend API (Ktor)

**Purpose:** RESTful API server with business logic and database access. **Imports all DTOs from `/shared` module.**

**Structure:**
```
server/
├── src/
│   └── main/kotlin/
│       ├── Application.kt     # Main entry point
│       ├── plugins/           # Ktor plugins configuration
│       ├── auth/
│       │   ├── AuthRoutes.kt  # Auth endpoints
│       │   ├── AuthService.kt # Auth business logic
│       │   └── JwtConfig.kt   # JWT token generation
│       ├── routes/
│       │   ├── PostRoutes.kt
│       │   ├── ChatRoutes.kt
│       │   ├── UserRoutes.kt
│       │   ├── NotificationRoutes.kt
│       │   ├── SubscriptionRoutes.kt
│       │   ├── SearchRoutes.kt
│       │   └── AdminRoutes.kt
│       ├── service/
│       │   ├── PostService.kt
│       │   ├── ChatService.kt
│       │   ├── NotificationService.kt
│       │   └── SubscriptionService.kt
│       ├── repository/
│       │   ├── UserRepository.kt
│       │   ├── PostRepository.kt
│       │   ├── ChatRepository.kt
│       │   └── NotificationRepository.kt
│       ├── middleware/
│       │   ├── AuthMiddleware.kt
│       │   ├── RateLimiter.kt
│       │   └── ErrorHandler.kt
│       ├── config/
│       │   └── EnvironmentConfig.kt
│       └── util/              # Utilities
```

**Responsibilities:**
- HTTP API endpoints
- Authentication and authorization
- Business logic execution
- Database operations (using Exposed ORM)
- External service integration (FCM, S3/GCS)
- Database-to-domain model mapping

**Key Principle:** Server imports all request/response DTOs from `shared/domain/model/auth/` to ensure API contract consistency with clients. Database entities may use internal enums that are converted to shared models.

### 4. `/iosApp` - iOS Application Wrapper

**Purpose:** iOS-specific app configuration and entry point

**Structure:**
```
iosApp/
├── iosApp/
│   ├── ContentView.swift      # SwiftUI wrapper for Compose
│   ├── iOSApp.swift           # App entry point
│   └── Info.plist
└── Configuration/
    ├── Config.xcconfig
    └── GoogleService-Info.plist
```

---

## Data Flow

### 1. User Authentication Flow

```
User → Login/Signup Screen → AuthViewModel → AuthRepository (shared/)
                                                    ↓
                                              Ktor Client
                                                    ↓
                                              POST /auth/login or /auth/register
                                                    ↓
                                              Backend API (server/)
                                                    ↓
                                              AuthService
                                                    ↓
                                              Generate OTP → Store in Redis
                                                    ↓
                                              Return OtpSentResponse
                                                    ↓
User → OTP Screen → AuthViewModel → POST /auth/verify-otp
                                                    ↓
                                              Verify OTP from Redis
                                                    ↓
                                              Generate JWT Tokens
                                                    ↓
                                              Return AuthResponse
                                                    ↓
                                    TokenStorage (Keystore/Keychain/Memory)
                                                    ↓
                                              Authenticated State
```

**Key Points:**
- All DTOs (LoginRequest, RegisterRequest, AuthResponse, etc.) are defined in `shared/domain/model/auth/`
- Both client and server import these models to ensure type consistency
- OTP is stored in Redis with expiration
- JWT tokens are stored securely using platform-specific implementations

### 2. Post Creation Flow

```
User → Create Post Screen → PostRepository → Backend API
                                                  ↓
                                            Validate Quota
                                                  ↓
                                            Save to Database
                                                  ↓
                                            Return Post
                                                  ↓
                              Update Local Cache ← PostRepository
                                                  ↓
                                            Update UI
```

### 3. Nearby Feed Flow

```
User → Timeline Screen → PostRepository → Backend API
                                              ↓
                                        Geo Query (PostGIS)
                                              ↓
                                        Filter by Distance
                                              ↓
                                        Return Posts
                                              ↓
                          Cache Locally ← PostRepository
                                              ↓
                                        Display in UI
```

### 4. Messaging Flow

```
User → Chat Screen → ChatRepository → Backend API
                                          ↓
                                    Save Message
                                          ↓
                                    Send FCM Notification
                                          ↓
                                    Return Message
                                          ↓
                      Update Local Cache ← ChatRepository
                                          ↓
                                    Update UI
```

---

## Database Schema


## Database Schema

See **[INFRA.md](./INFRA.md#database-schema)** for complete database schema and PostGIS configuration.

---
---

## API Design

### RESTful Endpoints

#### Authentication
- `POST /auth/register` - Register new user (sends OTP)
- `POST /auth/login` - Login existing user (sends OTP)
- `POST /auth/verify-otp` - Verify OTP and get JWT tokens
- `POST /auth/resend-otp` - Resend OTP code
- `POST /auth/login/google` - Google Sign-In
- `POST /auth/refresh` - Refresh JWT token

#### Posts
- `GET /posts/nearby?lat={lat}&lon={lon}&radius={radius}` - Get nearby posts
- `GET /posts/following` - Get following feed
- `POST /posts` - Create post
- `GET /posts/:id` - Get post details
- `PUT /posts/:id` - Update post
- `DELETE /posts/:id` - Delete post
- `POST /posts/:id/like` - Toggle like
- `GET /posts/:id/comments` - Get comments
- `POST /posts/:id/comments` - Add comment

#### Chat
- `GET /conversations` - List conversations
- `GET /conversations/:id/messages` - Get messages
- `POST /conversations/:id/messages` - Send message
- `POST /conversations` - Start new conversation

#### Users
- `GET /users/me` - Get current user profile
- `PUT /users/me` - Update profile
- `GET /users/:id` - Get user profile
- `POST /users/:id/follow` - Toggle follow

---

## Technology Stack

### Frontend
- **UI Framework:** Compose Multiplatform 1.9.0
- **Language:** Kotlin 2.2.20
- **Navigation:** Compose Navigation
- **Image Loading:** Coil (Android), Kamel (KMP)
- **Local Database:** SQLDelight
- **HTTP Client:** Ktor Client

### Backend
- **Framework:** Ktor 3.3.0
- **Language:** Kotlin 2.2.20
- **Database:** PostgreSQL 15+ with PostGIS
- **Caching:** Redis
- **Authentication:** JWT (ktor-server-auth-jwt)
- **Serialization:** kotlinx.serialization

### Infrastructure
- **Container:** Docker
- **Orchestration:** Kubernetes
- **Cloud Storage:** S3/GCS
- **Push Notifications:** FCM
- **Monitoring:** Prometheus + Grafana
- **CI/CD:** GitHub Actions

---

## Security Architecture

### Authentication
- JWT tokens with 7-day expiry
- Refresh tokens with 30-day expiry
- Secure token storage (Keystore/Keychain)
- Password hashing with BCrypt

### Authorization
- Role-based access control (user, admin, moderator)
- Ownership verification for user-generated content
- Rate limiting per user and IP

### Data Protection
- HTTPS for all API communication
- Encrypted data at rest (database encryption)
- Location data anonymization
- Chat metadata privacy (participants only)

---

## Scalability Considerations

### Horizontal Scaling
- Stateless backend services
- Load balancer for traffic distribution
- Database read replicas for read-heavy operations

### Caching Strategy
- Redis for session storage
- Query result caching for geo queries
- CDN for media assets

### Database Optimization
- GiST indexes for geospatial queries
- Partitioning for large tables (posts, messages)
- Connection pooling (HikariCP)

---

## Monitoring & Observability

### Metrics
- Request count, latency, error rate
- Database query performance
- Geo query execution time
- User activity metrics

### Logging
- Structured logging (JSON format)
- Log levels: DEBUG, INFO, WARN, ERROR
- Request/response logging (excluding sensitive data)

### Alerting
- High error rate (>5%)
- Slow queries (>100ms for geo queries)
- Service downtime
- Database connection issues

