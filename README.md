# NearYou ID

> **Location-based social platform** built with Kotlin Multiplatform
> Connects users within proximity to share posts, chat, and engage with their local community.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.0-orange.svg)](https://ktor.io)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.9.0-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Status](https://img.shields.io/badge/Status-MVP-yellow.svg)]()

---

## Quick Start

```bash
# Start infrastructure (PostgreSQL + Redis)
docker-compose up -d

# Run backend server
./gradlew :server:run

# Run Android app
./gradlew :composeApp:installDebug

# Run iOS app (macOS only)
open iosApp/iosApp.xcodeproj
```

**For detailed setup instructions, see [QUICK_START.md](./docs/GUIDES/QUICK_START.md)**

---

## Project Structure

```
nearyou-id/
├── composeApp/     # Android & iOS UI (Compose Multiplatform)
├── shared/         # Shared business logic (KMP)
├── server/         # Backend API (Ktor)
├── iosApp/         # iOS app wrapper
├── database/       # Database migrations & scripts
└── docs/           # Documentation
```

**Key modules:**
- **`/composeApp`** — Shared UI for Android and iOS
- **`/shared`** — Domain models, repositories, validation (shared across all platforms)
- **`/server`** — Ktor backend with PostgreSQL + PostGIS
- **`/iosApp`** — iOS application entry point

---

## Documentation Hub

### Start Here
- **[QUICK_START.md](./docs/GUIDES/QUICK_START.md)** — Development environment setup
- **[NearYou_ID_MVP_Plan.md](./docs/PLANS/NearYou_ID_MVP_Plan.md)** — Complete MVP execution plan

### For AI-Assisted Development
- **[VIBECODE_SHORT_META_PROMPT.md](./docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md)** — AI workflow guide for vibe code

### Core Technical Docs
- **[ARCHITECTURE.md](./docs/CORE/ARCHITECTURE.md)** — System design and architecture patterns
- **[SPEC.md](./docs/CORE/SPEC.md)** — Product specification
- **[API_DOCUMENTATION.md](./docs/CORE/API_DOCUMENTATION.md)** — REST API reference
- **[DESIGN_SYSTEM.md](./docs/CORE/DESIGN_SYSTEM.md)** — UI components and patterns
- **[INFRA.md](./docs/CORE/INFRA.md)** — Infrastructure, database, deployment
- **[TESTING.md](./docs/CORE/TESTING.md)** — Testing strategy and procedures

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | Compose Multiplatform, Kotlin 2.2.20, MVI Pattern |
| **Backend** | Ktor 3.3.0, Kotlin Coroutines |
| **Database** | PostgreSQL 15+ with PostGIS |
| **Caching** | Redis |
| **Auth** | JWT, OAuth 2.0 (Google) |
| **Storage** | S3/GCS (media files) |
| **Notifications** | Firebase Cloud Messaging |

---

## Current Status

**Version:** 0.2.0 (MVP — Auth Complete)
**Test Coverage:** See CI for current counts
**Last Updated:** 2026-02-08

### Completed
- Authentication system (JWT, OTP, Google OAuth)
- Backend infrastructure (PostgreSQL + Redis)
- Frontend authentication flows (Android + iOS)
- Basic geo-posts with PostGIS queries
- API documentation
- CI/CD pipeline with ktlint and Kover

### In Progress
- Phase 2: Core Timeline & Posts
- Phase 3: Messaging & Notifications

**For detailed progress, see [NearYou_ID_MVP_Plan.md](./docs/PLANS/NearYou_ID_MVP_Plan.md#progress-ledger-pl)**

---

## Testing

Run tests with:

```bash
./gradlew :server:test :composeApp:testDebugUnitTest :shared:testDebugUnitTest
```

**Learn more:** [`docs/CORE/TESTING.md`](./docs/CORE/TESTING.md)

---

## Learn More

- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor Framework](https://ktor.io/docs/)
- [PostGIS Documentation](https://postgis.net/documentation/)

---

## Contributing

1. Create a task branch: `git checkout -b task/T-XXX-description`
2. Follow the [VIBECODE_SHORT_META_PROMPT.md](./docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md) workflow
3. Validate changes using [TESTING.md](./docs/CORE/TESTING.md)
4. Update documentation if needed
5. Create pull request

---

## Support

For questions or issues:
- Check [QUICK_START.md](./docs/GUIDES/QUICK_START.md) for setup help
- See [INFRA.md](./docs/CORE/INFRA.md) for infrastructure details
- See [ARCHITECTURE.md](./docs/CORE/ARCHITECTURE.md) for system design

---

**Repository:** [aditrioka/nearyou-platform](https://github.com/aditrioka/nearyou-platform)
**License:** Proprietary - All rights reserved
