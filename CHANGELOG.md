# Changelog

All notable changes to NearYou ID will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Phase 0: Foundation & Setup (In Progress)
- Project documentation created
- Database infrastructure setup
- Shared domain models defined
- CI/CD pipeline configured

---

## [0.1.0] - 2025-10-16

### Added
- Initial project setup with Kotlin Multiplatform (KMP)
- Project structure with `/composeApp`, `/shared`, `/server`, `/iosApp` modules
- Gradle configuration with Kotlin 2.2.20, Ktor 3.3.0, Compose Multiplatform 1.9.0
- Comprehensive project documentation:
  - `docs/SPEC.md` - Product specification
  - `docs/ARCHITECTURE.md` - System architecture and design
  - `docs/DECISIONS.md` - Architectural Decision Records (ADRs)
  - `docs/INFRA.md` - Infrastructure setup and deployment
  - `docs/TESTING.md` - Testing strategy and guidelines
  - `CHANGELOG.md` - Version history (this file)

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
- GitHub Actions for CI/CD

### Infrastructure
- Docker Compose configuration for local development
- PostgreSQL with PostGIS extension setup
- Redis configuration for caching
- Environment variable templates

---

## Version History

### Version Numbering
- **Major (X.0.0):** Breaking changes, major feature releases
- **Minor (0.X.0):** New features, backward compatible
- **Patch (0.0.X):** Bug fixes, minor improvements

### Planned Releases

#### [0.2.0] - Phase 1: Authentication & User Management (Week 4)
- User registration with Google, Phone OTP, Email OTP
- JWT-based authentication
- User profile management
- Secure token storage

#### [0.3.0] - Phase 2: Core Timeline & Posts (Week 7)
- Nearby and Following feeds
- Post creation with text and media
- Like and comment functionality
- PostGIS geo queries with GiST indexes

#### [0.4.0] - Phase 3: Messaging & Notifications (Week 9)
- Two-way messaging with post context
- FCM push notifications
- Notification preferences

#### [0.5.0] - Phase 4: Subscription & Monetization (Week 11)
- Free and Premium subscription tiers
- Quota enforcement
- Ads integration for free users
- Subscription management UI

#### [0.6.0] - Phase 5: Search, Safety & Moderation (Week 13)
- Search functionality (subscriber-only)
- Report and block system
- Admin panel for moderation
- Analytics dashboard

#### [0.7.0] - Phase 6: Offline-First & Sync (Week 15)
- Local database with SQLDelight
- Sync service with conflict resolution
- Offline indicators and manual sync

#### [1.0.0] - Phase 7: Production Ready (Week 18)
- Performance optimization
- Security audit
- Load testing
- Production deployment
- Monitoring and alerting
- API documentation
- **First stable release**

---

## Migration Guide

### From 0.1.0 to 0.2.0 (Upcoming)
- Database migrations will be required for user authentication tables
- Environment variables for JWT secrets and OTP providers must be configured
- Google Sign-In credentials (google-services.json, GoogleService-Info.plist) required

---

## Deprecations

None yet.

---

## Security Advisories

None yet.

---

## Contributors

- Development Team - Initial work and ongoing development

---

## License

Proprietary - All rights reserved

---

## Links

- [Product Specification](docs/SPEC.md)
- [Architecture Documentation](docs/ARCHITECTURE.md)
- [Architectural Decisions](docs/DECISIONS.md)
- [Infrastructure Guide](docs/INFRA.md)
- [Testing Strategy](docs/TESTING.md)
- [Project Plan](NearYou_ID_MVP_Plan.md)

