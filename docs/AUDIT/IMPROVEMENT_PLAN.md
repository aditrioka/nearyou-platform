# NearYou ID — Phased Improvement Plan

**Date:** 2026-02-07
**Team:** nearyou-audit (6 agents)
**Project State:** MVP, early Phase 2. Runnable but not production-ready.

---

## Overall Assessment

| Area | Grade | Key Issue |
|------|-------|-----------|
| Backend Architecture | C+ | SQL injection, weak auth security |
| Frontend Architecture | B+ | iOS compat bugs, no navigation library |
| Documentation | C | 40% redundancy, misleading "production ready" claims |
| Spec-Driven Development | N/A | Not yet adopted; strong foundation via shared KMP module |
| Testing & CI/CD | 5/10 | Phantom CI jobs, weak assertions, zero UI tests |

---

## Phase 0 — Critical Security Fixes (Do First)

**Goal:** Eliminate vulnerabilities that block any deployment.

| # | Task | Owner | Risk |
|---|------|-------|------|
| 0.1 | Fix SQL injection in `PostRepository` — replace string interpolation with parameterized queries | Backend | P0 Critical |
| 0.2 | Use `SecureRandom` for OTP generation | Backend | P0 Critical |
| 0.3 | Reduce JWT access token expiry from 7 days → 30 min | Backend | P0 Critical |
| 0.4 | Fail server startup if JWT secret is default in non-dev mode | Backend | P0 Critical |
| 0.5 | Fix `System.currentTimeMillis()` → `Clock.System.now()` in EditProfileScreen | Frontend | P0 (iOS crash) |
| 0.6 | Guard iOS Koin init against duplicate `startKoin` | Frontend | P0 (iOS crash) |
| 0.7 | Fix `koinInject()` → `koinViewModel()` for all ViewModels | Frontend | P0 (stale state) |

**Milestone:** App still runs. No functionality changes. Security holes closed.

---

## Phase 1 — CI/CD & Testing Foundation (Week 1-2)

**Goal:** Make CI trustworthy and tests meaningful.

| # | Task | Priority |
|---|------|----------|
| 1.1 | Add ktlint plugin to all `build.gradle.kts` | P0 |
| 1.2 | Add Kover plugin to all `build.gradle.kts` | P0 |
| 1.3 | Remove `continue-on-error: true` from lint, Docker, and coverage CI jobs | P0 |
| 1.4 | Fix integration test assertions (replace `200..599` with real checks) | P0 |
| 1.5 | Add `.dockerignore` (exclude .git, build, composeApp, iosApp, docs) | P1 |
| 1.6 | Convert PostServiceTest to use Testcontainers | P1 |
| 1.7 | Add at least ViewModel unit tests for composeApp | P1 |
| 1.8 | Add Dependabot/Renovate for dependency updates | P2 |

**Milestone:** CI pipeline actually catches regressions. Test suite is honest.

---

## Phase 2 — Backend Hardening (Week 2-3)

**Goal:** Production-grade API patterns.

| # | Task | Priority |
|---|------|----------|
| 2.1 | Add CORS configuration | P1 |
| 2.2 | Add request body size limits (ContentNegotiation/DoubleReceive) | P1 |
| 2.3 | Add CallLogging plugin for request logging | P1 |
| 2.4 | Add input validation on registration (email, phone, password) | P1 |
| 2.5 | Validate file upload content via magic bytes, not just Content-Type | P1 |
| 2.6 | Extract JWT principal extraction to shared utility (DRY 8+ copies) | P1 |
| 2.7 | Make repositories injectable (interfaces) for testability | P2 |
| 2.8 | Centralize Exposed table definitions | P2 |
| 2.9 | Add API versioning (`/api/v1/`) | P2 |
| 2.10 | Add Flyway for database migrations | P2 |
| 2.11 | Set `prettyPrint=false`, `isLenient=false` for production | P3 |

**Milestone:** Backend passes OWASP baseline. APIs are logged, validated, versioned.

---

## Phase 3 — Frontend Modernization (Week 3-4)

**Goal:** Type-safe navigation, robust state management.

| # | Task | Priority |
|---|------|----------|
| 3.1 | Replace string-based MainScreen routing with sealed classes | P1 |
| 3.2 | Adopt Decompose or Compose Navigation for back stack + deep linking | P1 |
| 3.3 | Switch one-time events from `StateFlow<Event?>` to `Channel<Event>` | P1 |
| 3.4 | Move EditProfile local state into ProfileViewModel (single source of truth) | P2 |
| 3.5 | Add `@Immutable` to all UI state classes | P2 |
| 3.6 | Add `rememberSaveable` for critical form state | P2 |
| 3.7 | Replace deprecated `Icons.Default.ArrowBack` | P3 |
| 3.8 | Add `@Preview` to reusable components | P3 |

**Milestone:** Navigation is type-safe with history. State management follows documented MVI pattern consistently.

---

## Phase 4 — Documentation Cleanup (Week 2, parallel)

**Goal:** 22 files → 12 active + 3 archived. Remove redundancy and misleading claims.

| # | Task | Priority |
|---|------|----------|
| 4.1 | Delete: PRE_PUSH_CHECKLIST.md, TESTING_USER_PROFILE.md, TESTING_LOGGING.md, TESTING_WITH_CURL.md | P1 |
| 4.2 | Remove PROJECT_MAP.md (redundant with README) | P1 |
| 4.3 | Merge VALIDATION_GUIDE.md content into TESTING.md, delete original | P1 |
| 4.4 | Merge TESTING_LOGGING.md content into LOGGING.md | P1 |
| 4.5 | Archive PERFORMANCE.md → `docs/ARCHIVE/PERFORMANCE_2025-10-24.md` | P2 |
| 4.6 | Trim MVP_Plan.md: remove duplicated architecture/infra/testing sections (~800 lines) | P2 |
| 4.7 | Update README.md: remove "Production Ready" claim, update test counts | P1 |
| 4.8 | Trim ARCHITECTURE.md: remove API endpoint list (defer to API_DOCUMENTATION.md) | P2 |
| 4.9 | Trim INFRA.md: remove aspirational K8s/deployment sections | P2 |

**Milestone:** Documentation is accurate, non-redundant, and honest about project maturity.

---

## Phase 5 — OpenSpec Integration (Week 4-5)

**Goal:** Structured spec-driven development for new features.

| # | Task | Priority |
|---|------|----------|
| 5.1 | Install OpenSpec (`npm install -g openspec`) | P2 |
| 5.2 | Retrofit `auth-api` spec (proof-of-concept, no code changes) | P2 |
| 5.3 | Retrofit `post-api` and `user-profile-api` specs | P2 |
| 5.4 | Create `shared-models` spec documenting KMP DTOs | P2 |
| 5.5 | Use spec-first for next new feature (messaging-api) | P2 |

**Milestone:** All existing features have structured specs. New features start spec-first.

**Reversibility:** Delete `openspec/` directory to fully roll back. Zero impact on builds or code.

---

## Decisions Log

| Decision | Rationale |
|----------|-----------|
| Fix security before anything else | SQL injection and weak crypto are non-negotiable blockers |
| Fix CI before adding features | Current CI silently ignores failures — no confidence in green builds |
| Don't adopt a new navigation library in Phase 0 | Too risky alongside security fixes; do in Phase 3 |
| OpenSpec complements shared KMP module, doesn't replace it | Kotlin `@Serializable` classes remain machine-enforceable contract; OpenSpec adds human-readable layer |
| Trim docs before adding OpenSpec | Reduce redundancy first so specs don't add to the noise |
| Decompose over Compose Navigation | More mature for KMP, supports state restoration and proper lifecycle |
| Flyway over manual SQL scripts | Industry standard, integrates with CI, supports rollback |
| Keep all changes incremental | App must remain runnable after each phase |

---

## Individual Assessment Reports

- [Backend Assessment](backend_assessment.md)
- [Frontend Assessment](frontend_assessment.md)
- [Documentation Assessment](docs_assessment.md)
- [Spec-Driven Development Proposal](spec_driven_proposal.md)
- [CI/CD Assessment](cicd_assessment.md)
