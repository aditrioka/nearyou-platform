# Changelog

All notable changes to NearYou ID will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## Current Status (As of 2026-02-08)

**Version:** 0.2.0 - Authentication MVP
**Status:** MVP — Auth & basic posts implemented

### Quality Metrics
- **Test Coverage:** See CI for current counts
- **Security:** JWT with 30-min access tokens, BCrypt hashing, input validation, rate limiting
- **Documentation:** See docs/CORE/ for technical documentation

### Key Achievements
- HikariCP connection pooling for database connection management
- BCrypt hashing (12 rounds) for password security
- Koin 4.0.1 dependency injection across all modules
- StateFlow-based reactive state management
- Centralized error handling with consistent API error responses
- API documentation for implemented endpoints
- k6 load testing configured
- CORS, security headers, request body limits (Phase 2 hardening)
- Input validation (email, phone, lat/lng, media URLs)
- XSS sanitization and path traversal prevention

### Security Features
- JWT tokens (HMAC256)
- Access token: 30 minutes
- Refresh token: 30 days
- OTP-based verification
- BCrypt hashing (12 rounds)
- Rate limiting (login + OTP)
- Android Keystore integration
- iOS Keychain integration

### Technology Stack
- **Backend:** Ktor 3.3.0
- **Database:** PostgreSQL 15+ with PostGIS
- **Cache:** Redis (Lettuce client)
- **Frontend:** Compose Multiplatform 1.9.0
- **DI:** Koin 4.0.1
- **Testing:** kotlin-test, MockK, Turbine

---

## [Unreleased]

### Phase 2: Core Timeline & Posts (In Progress)

#### Added
- **PostGIS Geo Queries (T-201)** - 2025-10-29
  - PostRepository with PostGIS spatial queries (`ST_DWithin`, `ST_Distance`)
  - PostService with business logic and validation
  - PostRoutes with REST API endpoints (`/posts/nearby`, `/posts`, `/posts/:id`)
  - Custom GeographyColumnType for PostgreSQL GEOGRAPHY type
  - Support for 4 distance levels: 1km, 5km, 10km, 20km
  - CRUD operations with soft delete
  - Distance calculation and sorting
  - Premium user media upload restrictions

### Backend Hardening (Phase 2 — 2026-02)
- CORS configuration
- Security headers
- CallLogging for observability
- RequestBodyLimit plugin
- Input validation (email, phone, lat/lng, media URLs, profilePhotoUrl)
- XSS sanitization
- File upload whitelist
- Path traversal prevention
- Login rate limiting

### CI/CD & Testing Foundation (Phase 1 — 2026-02)
- ktlint integration
- Kover code coverage
- CI pipeline fixes
- Test assertion fixes

### Security Fixes (Phase 0 — 2026-02)
- SQL injection prevention
- SecureRandom for token generation
- JWT expiry fix (access token reduced from 7 days to 30 minutes)

---

## [0.2.0] - 2025-10-24

### Added - Authentication MVP
- Complete authentication system with JWT and OTP
- Backend API with Ktor 3.3.0
- Frontend authentication flows for Android and iOS
- API documentation
- Performance testing setup
- Database connection pooling with HikariCP
- Dependency injection with Koin
- Centralized error handling

### Security
- JWT authentication with 30-minute access tokens
- 30-day refresh tokens
- BCrypt password hashing (12 rounds)
- Rate limiting for OTP requests
- Secure token storage (Keystore/Keychain)

### Phase 1: Authentication & User Management

#### Added
- Frontend authentication flows (T-102) - 2025-10-20
  - Login screen with email/phone OTP
  - Signup screen with user registration
  - OTP verification screen
  - AuthViewModel for state management
  - Secure token storage (Android Keystore, iOS Keychain)
- Backend login endpoint (`POST /auth/login`) - 2025-10-18
- LoginRequest model in shared module - 2025-10-18
- API documentation - 2025-10-24
- Performance testing with k6 - 2025-10-24

#### Changed
- **MAJOR:** Refactored authentication UI to MVI (Model-View-Intent) pattern - 2025-10-28
  - Migrated from stateful, callback-driven UI to centralized ViewModel state management
  - Implemented `StateFlow` for reactive state updates instead of `mutableStateOf`
  - Introduced event-driven navigation with `AuthEvent` sealed class for one-time events
  - Made UI components stateless and "dumb" (presentation only)
  - Consolidated all business logic, validation, and state in `AuthViewModel`
  - Screens now observe state and delegate all actions to ViewModel methods
- Centralized UI resources for consistency and maintainability - 2025-10-28
  - Created `Dimensions.kt` for all size constants (button heights, spacing, etc.)
  - Created `Strings.kt` for all user-facing text (preparation for i18n)
  - Replaced all hardcoded values with references to centralized constants
- Enhanced screen layouts and user experience - 2025-10-28
  - Added proper keyboard handling with `imePadding()` modifier
  - Implemented `windowInsetsPadding(WindowInsets.systemBars)` for system bars
  - Added vertical scroll support for smaller devices when keyboard is active
  - Improved content centering with flexible spacers
- Improved accessibility and performance - 2025-10-28
  - Added `@Immutable` annotations to state classes for better Compose performance
  - Enhanced components with `contentDescription` for screen readers
  - Added semantic properties to interactive elements
  - Improved focus management in `OtpInput` component
- Implemented multiplatform-compatible email validation - 2025-10-28
  - Removed dependency on Android-specific `android.util.Patterns`
  - Created regex-based validation that works across all platforms
- Refactored `OtpInput` component for better UX - 2025-10-28
  - Uses single hidden `BasicTextField` controlling visual boxes
  - Fixed focus management and keyboard handling issues
  - Improved accessibility with proper focus requester
- **BREAKING:** Refactored to KMP best practices - shared models architecture - 2025-10-18
  - Removed duplicate model definitions between server and client
  - Server now imports all DTOs from `shared/` module (single source of truth)
  - Created database mapping layer for enum conversions
- Implemented HikariCP connection pooling (max 10, min 2) - 2025-10-24
- Fixed password security with BCrypt hashing (12 rounds) - 2025-10-24
- Added Koin dependency injection across all modules - 2025-10-24
- Migrated to StateFlow for reactive state management - 2025-10-24
- Implemented centralized error handling with StatusPages - 2025-10-24
- Updated `UserRepository` to return shared `User` model instead of `UserDto` - 2025-10-18
- Updated `JwtConfig` to accept `SubscriptionTier` enum instead of String - 2025-10-18

#### Fixed
- Android network security configuration for development (cleartext traffic) - 2025-10-20
- SignupScreen now properly calls backend API before navigation - 2025-10-28
- LoginScreen now properly calls backend API before navigation - 2025-10-28
- Smart cast issues with nullable properties in AuthService - 2025-10-18
- Timestamp conversion in UserRepository - 2025-10-18
- **Critical:** Password security vulnerability (plain password storage in Redis) - 2025-10-24
- Compilation errors with DateTimePeriod to Duration conversion - 2025-10-18
- Missing imports and serialization plugin - 2025-10-18
- PostgreSQL ENUM type handling for subscription_tier - 2025-10-18

---

## [0.1.0] - 2025-10-16

### Added
- Initial project setup with Kotlin Multiplatform (KMP)
- Project structure with `/composeApp`, `/shared`, `/server`, `/iosApp` modules
- Gradle configuration with Kotlin 2.2.20, Ktor 3.3.0, Compose Multiplatform 1.9.0
- Comprehensive project documentation
- Docker Compose configuration for local development
- PostgreSQL with PostGIS extension setup
- Redis configuration for caching
- Environment variable templates
- Database migrations structure

### Technical Decisions
- Kotlin Multiplatform for cross-platform development
- PostgreSQL 15+ with PostGIS for geospatial queries
- Ktor 3.3.0 for backend framework
- Compose Multiplatform for shared UI
- SQLDelight for local database
- JWT for authentication
- Redis for caching and rate limiting
- FCM for push notifications
- S3/GCS for media storage
- Clean/Hexagonal architecture pattern
- MVI (Model-View-Intent) for frontend architecture
- GitHub Actions for CI/CD

---

## Version History

### Version Numbering
- **Major (X.0.0):** Breaking changes, major feature releases
- **Minor (0.X.0):** New features, backward compatible
- **Patch (0.0.X):** Bug fixes, minor improvements

---

## Security Advisories

### Fixed in 0.2.0
- **CRITICAL (2025-10-24):** Fixed password security vulnerability where passwords were stored in plain text in Redis. Now using BCrypt hashing with 12 rounds before storage.

### Fixed in Phase 0 (2026-02)
- SQL injection vulnerabilities in repository queries
- JWT access token expiry changed from 7 days to 30 minutes
- Switched to SecureRandom for token generation

---

## Deprecations

### Deprecated in 0.2.0
- Direct password storage in Redis (replaced with BCrypt hashing)
- Stateful UI components (migrated to StateFlow and MVI pattern)
- Duplicate model definitions (consolidated in shared/ module)
- Android-specific email validation (replaced with multiplatform regex)

---

## Contributors

- **Adi Trioka** - Development and implementation
- **Augment Code** - AI assistant for development

---

## License

Proprietary - All rights reserved

---

## Links

- [Product Specification](SPEC.md)
- [Architecture Documentation](ARCHITECTURE.md)
- [Architectural Decisions](DECISIONS.md)
- [Infrastructure Guide](INFRA.md)
- [Testing Strategy](TESTING.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Quick Start Guide](../GUIDES/QUICK_START.md)
- [AI Workflow Guide](../PROMPTS/VIBECODE_SHORT_META_PROMPT.md)

---

**Last Updated:** 2026-02-08
