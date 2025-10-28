# NearYou ID

> **Location-based social platform** built with Kotlin Multiplatform  
> Connects users within proximity to share posts, chat, and engage with their local community.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.0-orange.svg)](https://ktor.io)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.9.0-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

---

## 🚀 Quick Start

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

**Full setup guide:** [`docs/PLANS/QUICK_START.md`](./docs/PLANS/QUICK_START.md)

---

## 📁 Project Structure

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
- **`/composeApp`** → Shared UI for Android and iOS
- **`/shared`** → Domain models, repositories, validation (shared across all platforms)
- **`/server`** → Ktor backend with PostgreSQL + PostGIS
- **`/iosApp`** → iOS application entry point

---

## 📚 Documentation

**Essential reading:**
- **[PROJECT_MAP.md](./docs/CORE/PROJECT_MAP.md)** → Main documentation index
- **[ARCHITECTURE.md](./docs/CORE/ARCHITECTURE.md)** → System design and architecture
- **[SPEC.md](./docs/CORE/SPEC.md)** → Product specification
- **[QUICK_START.md](./docs/PLANS/QUICK_START.md)** → Development environment setup
- **[API_DOCUMENTATION.md](./docs/API_DOCUMENTATION.md)** → API reference

**For AI assistants:**
- **[VIBECODE_SHORT_META_PROMPT.md](./docs/PROMPTS/VIBECODE_SHORT_META_PROMPT.md)** → AI workflow guide
- **[VALIDATION_GUIDE.md](./docs/CORE/VALIDATION_GUIDE.md)** → Validation procedures

---

## 🛠️ Tech Stack

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

## 🧪 Testing & Validation

This project follows a **validation-first approach**:

- **AI validation** → File consistency, structure, automated tests
- **Human validation** → Manual testing, external service setup, UI/UX verification
- **Hybrid validation** → Collaborative validation for complex features

**Learn more:** [`docs/CORE/VALIDATION_GUIDE.md`](./docs/CORE/VALIDATION_GUIDE.md)

---

## 📖 Learn More

- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor Framework](https://ktor.io/docs/)
- [PostGIS Documentation](https://postgis.net/documentation/)

