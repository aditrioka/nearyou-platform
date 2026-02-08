# Phase-by-Phase Implementation Guide

> Synthesized from 5 domain audits (Backend, Frontend, Docs, Spec-Driven, CI/CD)
> Generated: 2026-02-09 | Project: NearYou ID (KMP)

---

## How to Use This Guide

Each phase is **self-contained, incremental, and reversible**. Complete phases in order — later phases depend on earlier ones. Each phase has:

- **Goal**: What this phase achieves
- **Tasks**: Specific work items with file paths
- **Acceptance Criteria**: How to verify the phase is done
- **Risk Level**: Low (safe to execute) / Medium (test carefully) / High (review before merging)

**Non-Regression Rule**: Every task in every phase MUST preserve existing working functionality. After each task:
1. Server compiles and starts successfully
2. Client (Android) compiles and runs successfully
3. All existing tests pass (`./gradlew test`)
4. Core user flows still work: registration, login, OTP verification, profile view (MVP)
5. If a task breaks existing functionality, fix the regression before moving to the next task

---

## Phase 0: Critical Security Fixes

**Goal**: Eliminate active security vulnerabilities. These must be fixed before any other work.
**Risk Level**: Medium (changes runtime behavior, but all are bug fixes)
**Estimated scope**: ~8 files changed

### Task 0.1: Fix SQL Injection in PostRepository

**Files**: `server/src/main/kotlin/id/nearyou/app/post/PostRepository.kt`

The `findNearbyPosts()` method (lines 129-192) uses string interpolation for SQL with `longitude`, `latitude`, `radiusMeters`, `limit`, and `currentUserId`. The `createPost()` method (lines 222-248) interpolates `mediaUrls` with single-quote wrapping — a clear injection vector. The `content.replace("'", "''")` escaping is insufficient.

**Action**:
1. Use the existing parameterized query template (lines 93-126 — currently unused) with `?` placeholders
2. Replace all `exec(finalQuery)` calls with parameterized `exec()` using `PreparedStatement`
3. For `createPost()`, use Exposed DSL `insertAndGetId {}` instead of raw SQL
4. Remove manual quote escaping

### Task 0.2: Switch OTP to SecureRandom

**Files**: `server/src/main/kotlin/id/nearyou/app/auth/AuthService.kt`

Line 285 uses `Random.nextInt(100000, 999999)` (non-cryptographic PRNG). MEMORY.md claims Phase 0 fixed this, but the code still uses `kotlin.random.Random`.

**Action**: Replace with `java.security.SecureRandom().nextInt(900000) + 100000`

### Task 0.3: Remove Production Utility Files

**Files**:
- `server/src/main/kotlin/id/nearyou/app/TestDatabaseConnection.kt` — hardcoded credentials
- `server/src/main/kotlin/id/nearyou/app/ShowDatabaseData.kt` — dumps OTP codes, security risk
- `server/build.gradle.kts` lines 68-81 — `testDb` and `showData` Gradle tasks

**Action**: Delete both files and their Gradle task registrations. If needed for development, move to a separate `tools/` directory outside the server module.

### Task 0.4: Reduce Access Token Expiry

**Files**: `server/src/main/kotlin/id/nearyou/app/config/EnvironmentConfig.kt`

Line 53: `accessTokenExpiry = 7.days`. 7-day access tokens create a large attack window for stolen tokens.

**Action**:
1. Change `accessTokenExpiry` to 15 minutes (or make configurable via env var)
2. Make `accessTokenExpiry` and `refreshTokenExpiry` environment-configurable
3. Add `type` claim to JWT tokens (access vs refresh) in `JwtConfig.kt`
4. Validate token `type` in `Authentication.kt` to prevent refresh tokens being used as access tokens

### Task 0.5: Fix Android Silent Fallback to Unencrypted Token Storage

**Files**: `shared/src/androidMain/kotlin/data/TokenStorageAndroid.kt`

Lines 48-49: Falls back to plain `SharedPreferences` silently if `EncryptedSharedPreferences` fails.

**Action**: Log a severe warning when falling back. Consider throwing and forcing re-authentication instead of storing tokens in plaintext.

### Acceptance Criteria
- [ ] All PostRepository queries use parameterized statements or Exposed DSL
- [ ] OTP generation uses `SecureRandom`
- [ ] `TestDatabaseConnection.kt` and `ShowDatabaseData.kt` deleted from main sources
- [ ] Access token expiry ≤ 60 minutes
- [ ] JWT tokens include `type` claim; auth middleware validates it
- [ ] All existing tests pass
- [ ] Server starts and responds to health check
- [ ] Client compiles and core flows work: login, OTP, profile view (MVP)

---

## Phase 1: Build System & CI Restoration

**Goal**: Restore broken static analysis and CI reliability. Currently ktlint and Kover plugins are missing from build files despite CI referencing them.
**Risk Level**: Low (build config only, no runtime changes)
**Estimated scope**: ~5 files changed

### Task 1.1: Restore ktlint Plugin

**Files**: `build.gradle.kts` (root), `.editorconfig`

The CI runs `./gradlew ktlintCheck` but no build file applies the ktlint plugin. The `.editorconfig` with disabled rules is also missing.

**Action**:
1. Add `id("org.jlleitschuh.gradle.ktlint") version "12.1.2"` to root `build.gradle.kts`
2. Apply to all subprojects via `subprojects { apply(plugin = ...) }` with `exclude { it.file.path.contains("/build/") }`
3. Create `.editorconfig` with disabled rules: `function-naming`, `no-wildcard-imports`, `enum-entry-name-case`, `property-naming`, `class-naming`, `filename`
4. Remove `continue-on-error: true` from CI lint step

### Task 1.2: Restore Kover Plugin

**Files**: `build.gradle.kts` (root)

The CI runs `./gradlew koverXmlReport` but no build file applies the Kover plugin.

**Action**:
1. Add `id("org.jetbrains.kotlinx.kover") version "0.9.0"` to root `build.gradle.kts`
2. Apply via `subprojects {}` block
3. Add coverage threshold (e.g., 40% minimum to start, increase over time)
4. Add PostgreSQL/Redis service containers to coverage CI job, OR separate into unit-only coverage

### Task 1.3: Fix Kotlin Serialization Plugin Version Mismatch

**Files**: `server/build.gradle.kts`

Line 4: `kotlin("plugin.serialization") version "2.1.0"` while project uses Kotlin 2.2.20.

**Action**: Add serialization plugin to version catalog and use `alias(libs.plugins.kotlinSerialization)`. Ensure all modules use the same version.

### Task 1.4: Migrate Hardcoded Dependencies to Version Catalog

**Files**: `server/build.gradle.kts`, `shared/build.gradle.kts`, `composeApp/build.gradle.kts`, `gradle/libs.versions.toml`

13+ Ktor server plugins, 4 Exposed modules, Redis, HikariCP, BCrypt, test deps — all hardcoded outside version catalog.

**Action**: Move all dependency versions to `libs.versions.toml`. Fix the `kotlinx-datetime` version inconsistency (0.6.0 in composeApp vs 0.6.1 in shared).

### Task 1.5: Clean Up CI Pipeline

**Files**: `.github/workflows/ci.yml`

**Action**:
1. Remove `continue-on-error: true` from lint step (make lint gate PRs)
2. Remove or convert `build-ios` job to manual trigger (`workflow_dispatch`)
3. Add service containers (PostgreSQL, Redis) to coverage job
4. Make summary job gate on lint results too
5. Add `@Ignore` to `PostRepositoryTest` and `PostServiceTest` (or convert to Testcontainers)

### Acceptance Criteria
- [ ] `./gradlew ktlintCheck` runs successfully
- [ ] `./gradlew koverXmlReport` generates reports
- [ ] All dependency versions in `libs.versions.toml`
- [ ] CI lint failures block PRs
- [ ] CI passes end-to-end without `continue-on-error` masking failures
- [ ] Server and client still compile and run
- [ ] All existing tests pass
- [ ] Core user flows unaffected: login, OTP, profile view (MVP): login, OTP, profile, posts

---

## Phase 2: Frontend Architecture Fixes

**Goal**: Fix critical frontend bugs and establish correct patterns.
**Risk Level**: Medium (changes runtime behavior, affects both platforms)
**Estimated scope**: ~12 files changed

### Task 2.1: Fix iOS Koin Initialization (CRITICAL)

**Files**: `composeApp/src/iosMain/kotlin/id/nearyou/app/MainViewController.kt`

`startKoin` is called inside the `ComposeUIViewController {}` lambda. If SwiftUI recreates the view controller, Koin crashes with "A Koin Application has already been started."

**Action**:
1. Move `startKoin` to a top-level `fun initKoin()` function
2. Call `initKoin()` from Swift's `AppDelegate.application(_:didFinishLaunchingWithOptions:)` or use a `KoinApplication` composable pattern
3. Also call `AppConfig.initialize()` from the iOS entry point (currently only Android does this)

### Task 2.2: Replace StateFlow Events with Channel

**Files**: `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/auth/AuthViewModel.kt`, `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/profile/ProfileViewModel.kt`

`MutableStateFlow<AuthEvent?>` at line 72 causes events to be lost (if two fire before collection) or replayed (on config change). Multiple composables collect the same event simultaneously, creating race conditions.

**Action**:
1. Replace `MutableStateFlow<AuthEvent?>` with `Channel<AuthEvent>(Channel.BUFFERED)`
2. Expose as `val events = _events.receiveAsFlow()`
3. Remove `onEventConsumed()` method
4. Collect events in ONE place (the navigation host), not in individual screens
5. Apply same pattern to `ProfileViewModel.updateSuccess` boolean flag

### Task 2.3: Implement Proper Navigation with NavHost

**Files**: `composeApp/src/commonMain/kotlin/id/nearyou/app/App.kt`, `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/navigation/AuthNavigation.kt`, `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/main/MainScreen.kt`

Current navigation is manual `when`-switching on `mutableStateOf` with string literals. No back stack, no deep linking.

**Action**:
1. Add `navigation-compose` dependency (2.9.x for KMP)
2. Define `@Serializable` route objects for all destinations
3. Create `NavHost` in `App.kt` with nested auth and main navigation graphs
4. Replace string-based routing in `MainScreen` with type-safe navigation
5. Add proper back stack handling

### Task 2.4: Fix ViewModel Injection Consistency

**Files**: `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/profile/ProfileScreen.kt`, `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/profile/EditProfileScreen.kt`

These use `koinInject()` instead of `koinViewModel()`, causing the ViewModel to not be scoped to the composable lifecycle.

**Action**: Replace `koinInject<ProfileViewModel>()` with `koinViewModel<ProfileViewModel>()` in both files.

### Task 2.5: Remove Manual Bearer Token Attachment

**Files**: `shared/src/commonMain/kotlin/data/UserRepository.kt`, `shared/src/commonMain/kotlin/data/AuthRepository.kt`

Repositories manually call `bearerAuth(accessToken)` despite the Ktor Auth plugin already handling this. This causes double token headers and bypasses the automatic refresh mechanism.

**Action**: Remove all manual `bearerAuth()` calls. Let the Auth plugin handle token attachment and refresh automatically.

### Task 2.6: Fix iOS-Crashing JVM API Usage

**Files**: `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/profile/EditProfileScreen.kt`

Line 61: `System.currentTimeMillis()` is JVM-only, will crash on iOS.

**Action**: Replace with `kotlinx.datetime.Clock.System.now().toEpochMilliseconds()`

### Acceptance Criteria
- [ ] iOS app doesn't crash on Koin initialization
- [ ] Navigation events are never lost or replayed
- [ ] Back button works correctly on Android
- [ ] Profile screens use `koinViewModel()`
- [ ] No manual `bearerAuth()` calls in repositories
- [ ] `EditProfileScreen` works on iOS (no `System.currentTimeMillis()`)
- [ ] All existing tests pass
- [ ] Server starts and responds to health check
- [ ] Client compiles and core flows work: login, OTP, profile view (MVP)

---

## Phase 3: Backend Hardening

**Goal**: Add missing security middleware and fix API inconsistencies. MEMORY.md claims these were done in Phase 2 but they are NOT present in the code.
**Risk Level**: Medium (adds runtime middleware, but all are standard Ktor plugins)
**Estimated scope**: ~8 files changed

### Task 3.1: Add Missing Ktor Plugins

**Files**: `server/src/main/kotlin/id/nearyou/app/Application.kt`

**Action**: Install these plugins in `Application.module()`:
1. `install(CORS)` — configure allowed origins, methods, headers
2. `install(DefaultHeaders)` — X-Content-Type-Options, X-Frame-Options, Strict-Transport-Security, X-XSS-Protection
3. `install(CallLogging)` — request/response logging with MDC context
4. `install(BodyLimit) { maximumSize = 1_048_576L }` — 1MB default (upload route can override)
5. Add corresponding dependencies to `server/build.gradle.kts` if missing

### Task 3.2: Add Server-Side Input Validation on Auth Routes

**Files**: `server/src/main/kotlin/id/nearyou/app/auth/AuthRoutes.kt`, `server/src/main/kotlin/id/nearyou/app/auth/AuthService.kt`

Registration accepts any username/email/phone without validation. Shared module has `UserValidation` with proper rules, but the server doesn't use it.

**Action**:
1. Call `UserValidation.validateUsername()`, `UserValidation.validateEmail()`, `UserValidation.validatePhone()` in `AuthService.register()` before storing
2. Use `PostValidation.validateContent()` in `PostService` instead of inline hardcoded limits
3. Throw `ValidationException` with clear error messages on failure

### Task 3.3: Fix API Response Inconsistencies

**Files**: `server/src/main/kotlin/id/nearyou/app/auth/AuthRoutes.kt`, `server/src/main/kotlin/id/nearyou/app/post/PostRoutes.kt`, `server/src/main/kotlin/id/nearyou/app/upload/UploadRoutes.kt`

**Action**:
1. Registration: Return `HttpStatusCode.Created` (201) instead of `OK` (200)
2. Delete post: Return `HttpStatusCode.NoContent` (204) instead of `OK` (200) with body
3. Upload: Return `HttpStatusCode.Created` (201)
4. Replace ad-hoc `mapOf()` responses with typed response models (use shared `PostListResponse` for nearby posts)
5. Remove duplicate `UploadResponse` from `UploadRoutes.kt` — use the shared model
6. Standardize DI pattern: use `application.get<T>()` consistently (or `inject<T>()` consistently — pick one)

### Task 3.4: Fix Rate Limiting Race Condition

**Files**: `server/src/main/kotlin/id/nearyou/app/auth/AuthService.kt`

Lines 358-373: Non-atomic GET + INCR pattern allows concurrent requests to bypass limits.

**Action**: Use Redis `INCR` + `EXPIRE` in a single atomic operation, or use a Lua script.

### Task 3.5: Fix JWT & Auth Issues

**Files**: `server/src/main/kotlin/id/nearyou/app/auth/JwtConfig.kt`, `server/src/main/kotlin/id/nearyou/app/plugins/Authentication.kt`, `server/src/main/kotlin/id/nearyou/app/config/EnvironmentConfig.kt`

**Action**:
1. Remove duplicate JWT verifier from `Authentication.kt` — use `JwtConfig.verifier` only
2. Make JWT secret validation mandatory (not conditional on `otpProvider`)
3. Validate refresh token exists in DB (not just "not revoked") in `AuthService.refreshToken()`

### Task 3.6: Use Suspended Transactions

**Files**: All files using `transaction {}` — `AuthService.kt`, `PostService.kt`, `UserService.kt`, `PostRepository.kt`, `UserRepository.kt`

Blocking JDBC calls on Ktor's coroutine threads limit concurrency.

**Action**: Replace `transaction {}` with `newSuspendedTransaction(Dispatchers.IO) {}` throughout.

### Task 3.7: Miscellaneous Backend Cleanup

**Files**: Various server files

**Action**:
1. Move pipe-delimited Redis values to JSON serialization (`AuthService.kt` line 94)
2. Remove or gate Google OAuth placeholder endpoint (returns 503 always)
3. Make `prettyPrint` conditional on environment in `Serialization.kt`
4. Disable `isLenient` in production
5. Consolidate table definitions into a `tables/` package
6. Replace `RuntimeException` with `InternalServerException` in PostService

### Acceptance Criteria
- [ ] CORS, security headers, call logging, body limit all installed and configured
- [ ] Auth registration validates username/email/phone format
- [ ] All responses use correct HTTP status codes
- [ ] No ad-hoc `mapOf()` responses — all typed
- [ ] Rate limiting is atomic
- [ ] All DB transactions use suspended transactions
- [ ] All existing tests pass; new plugin integration tests added
- [ ] Server starts and responds to health check
- [ ] Client compiles and core flows work: login, OTP, profile view (MVP)
- [ ] Auth flow (register → OTP → login → token refresh) works end-to-end

---

## Phase 4: Test Quality Improvement

**Goal**: Make tests meaningful and CI trustworthy.
**Risk Level**: Low (test-only changes, no runtime impact)
**Estimated scope**: ~15 files changed

### Task 4.1: Fix Integration Test Assertions

**Files**: All `server/src/test/kotlin/.../integration/*.kt`

Replace `assertTrue(response.status.value in 200..599)` with specific assertions. Either:
- Load the full application module with `testApplication { application { module() } }` using Testcontainers for PostgreSQL/Redis, and assert correct responses
- Or assert specific expected codes (`assertEquals(HttpStatusCode.Unauthorized, response.status)` for unauthenticated requests)

### Task 4.2: Fix Infra-Dependent Tests

**Files**: `server/src/test/kotlin/.../post/PostRepositoryTest.kt`, `server/src/test/kotlin/.../post/PostServiceTest.kt`

**Action**: Either:
1. Convert to Testcontainers-based tests (extend the existing `TestcontainersExampleTest` pattern)
2. Or add `@Ignore("Requires PostgreSQL with PostGIS")` with rationale

### Task 4.3: Extract Shared Test Fixtures

**Files**: Create `shared/src/commonTest/kotlin/fixtures/` or `testFixtures` source set

`MockTokenStorage` is duplicated 3 times (AuthRepositoryTest, AuthViewModelTest, ProfileViewModelTest). `createMockUser()` is duplicated in 2 files.

**Action**:
1. Create a `MockTokenStorage` in shared commonTest
2. Create a `TestDataFactory` with `createMockUser()`, `createMockPost()`, etc.
3. Remove duplicates from individual test files

### Task 4.4: Remove Placeholder Tests

**Files**: `shared/src/commonTest/kotlin/id/nearyou/app/SharedCommonTest.kt`, `composeApp/src/commonTest/kotlin/id/nearyou/app/ComposeAppCommonTest.kt`

Both contain only `assertEquals(3, 1 + 2)`. Replace with meaningful tests or delete.

### Task 4.5: Standardize on JUnit 5

**Files**: Server test files mixing JUnit 4 and 5

**Action**: Migrate all `org.junit.Test` imports to `org.junit.jupiter.api.Test`. Ensure consistent annotations.

### Task 4.6: Add Koin Module Verification Tests

**Files**: New test files for each module

**Action**: Add `checkModules {}` tests for `ServerModule`, `SharedModule`, and `AppModule` to verify DI wiring at compile time.

### Acceptance Criteria
- [ ] All integration tests assert specific expected behavior
- [ ] Infra-dependent tests use Testcontainers or are clearly marked @Ignore
- [ ] No duplicated test fixtures
- [ ] No placeholder tests
- [ ] All server tests use JUnit 5
- [ ] DI modules have verification tests
- [ ] All existing tests still pass (no regressions from test refactoring)
- [ ] Server and client compile and run
- [ ] Core user flows unaffected: login, OTP, profile view (MVP)

---

## Phase 5: Documentation Rationalization

**Goal**: Reduce 19 doc files to 8 high-quality files. Fix all broken cross-references and stale claims.
**Risk Level**: Low (documentation only)
**Estimated scope**: ~19 files affected (delete, merge, update)

### Task 5.1: Fix Stale Status Claims

**Files**: `README.md`, `docs/CORE/CHANGELOG.md`, `docs/CORE/PROJECT_MAP.md`

**Action**: Replace "Production Ready" / "Version 1.0.0" / "Compliance Score: 9.8/10" with accurate "MVP v0.2.0" status.

### Task 5.2: Delete Low-Value Documentation

**Files to delete**:
- `docs/CORE/PROJECT_MAP.md` — README serves as entry point
- `docs/CORE/VALIDATION_GUIDE.md` — absorbed into VIBECODE prompt
- `docs/GUIDES/TESTING_LOGGING.md` — one-time setup, no ongoing value
- `docs/GUIDES/TESTING_USER_PROFILE.md` — redundant with API_DOCUMENTATION
- `docs/GUIDES/TESTING_WITH_CURL.md` — redundant with API_DOCUMENTATION, mixed-language

### Task 5.3: Create Merged Development Guide

**New file**: `docs/DEVELOPMENT_GUIDE.md`

Merge content from:
- `docs/GUIDES/QUICK_START.md` (setup, running)
- `docs/CORE/INFRA.md` (database, Docker, env vars — trim aspirational K8s/Prometheus)
- `docs/CORE/TESTING.md` (testing strategy, commands)
- `docs/GUIDES/LOGGING.md` (logging setup)
- `docs/GUIDES/PRE_PUSH_CHECKLIST.md` (checklist)

Then delete the source files.

### Task 5.4: Merge SPEC and CHANGELOG into MVP Plan

**Action**:
1. Fold essential product vision from `docs/CORE/SPEC.md` into `docs/PLANS/NearYou_ID_MVP_Plan.md`
2. Merge `docs/CORE/CHANGELOG.md` entries into MVP Plan's progress ledger
3. Delete source files
4. Consider trimming the MVP Plan (currently 2100+ lines)

### Task 5.5: Archive PERFORMANCE.md

**Action**: Move `docs/CORE/PERFORMANCE.md` to `docs/ARCHIVE/` or delete. Keep performance test scripts in `performance-tests/`.

### Task 5.6: Update ARCHITECTURE.md for Phase 2-3 Changes

**Files**: `docs/CORE/ARCHITECTURE.md`

**Action**:
1. Update navigation section to reflect actual implementation (manual route switching, or NavHost if Phase 2 of this guide is done first)
2. Update events section (Channel-based or StateFlow depending on current state)
3. Add Phase 2 security hardening (CORS, headers, etc.)
4. Remove broken links to `BEST_PRACTICES_EVALUATION.md`

### Task 5.7: Fix All Broken Cross-References

Fix the 9 broken links identified in the docs audit (see docs audit report for full list).

### Target Documentation Structure

```
README.md                           # Overview, status, quick links
docs/
  ARCHITECTURE.md                   # System design, patterns, data flow
  API_DOCUMENTATION.md              # REST API reference
  DECISIONS.md                      # ADRs
  DESIGN_SYSTEM.md                  # UI patterns
  DEVELOPMENT_GUIDE.md              # Setup, testing, logging, checklist (NEW)
  PLANS/NearYou_ID_MVP_Plan.md      # Roadmap + changelog
  PROMPTS/VIBECODE_SHORT_META_PROMPT.md  # AI workflow
```

### Acceptance Criteria
- [ ] 19 files reduced to 8
- [ ] Zero broken cross-references
- [ ] No stale "Production Ready" or "Version 1.0.0" claims
- [ ] All remaining docs accurately reflect current codebase
- [ ] No code changes — server and client still compile and run as before

---

## Phase 6: API-Model Alignment & Spec Foundation

**Goal**: Align frontend/backend contracts and establish spec-driven development.
**Risk Level**: Medium (changes API response shapes)
**Estimated scope**: ~10 files changed + new spec files

### Task 6.1: Fix Error Response Format Mismatch

**Files**: `shared/src/commonMain/kotlin/data/ApiError.kt`, `server/src/main/kotlin/id/nearyou/app/exceptions/ApiExceptions.kt`

Server returns `ErrorResponse { ErrorDetail(code, message, timestamp, details) }`. Client expects `ApiErrorResponse(error, message)`. The field structures differ.

**Action**: Align both sides. Either update client `ApiErrorResponse` to match server format, or simplify server format. The server format is richer — update the client.

### Task 6.2: Replace Ad-hoc Responses with Shared Models

**Files**: `server/src/main/kotlin/id/nearyou/app/post/PostRoutes.kt`

`GET /posts/nearby` returns `mapOf("posts" to ..., "count" to ...)` instead of using the shared `PostListResponse` model.

**Action**: Use `PostListResponse` from shared module. Remove unused `FeedType` enum or implement it.

### Task 6.3: Remove Duplicate UploadResponse

**Files**: `server/src/main/kotlin/id/nearyou/app/upload/UploadRoutes.kt`

Server defines its own `UploadResponse` at line 20-26, identical to the shared model.

**Action**: Import and use the shared `UploadResponse` instead.

### Task 6.4: Create OpenSpec Foundation

**Directory**: `openspec/specs/`

**Action**: Create spec files documenting actual API behavior (not aspirational):
```
openspec/specs/
├── auth-register/spec.md
├── auth-login/spec.md
├── auth-verify-otp/spec.md
├── auth-refresh/spec.md
├── auth-logout/spec.md
├── user-profile/spec.md
├── post-crud/spec.md
├── post-nearby/spec.md
└── upload-profile-photo/spec.md
```

Each spec documents: purpose, request/response shapes (referencing shared models), validation rules, error codes, auth requirements, and any known gaps.

### Task 6.5: Clean Up Unused Shared Models

**Files**: `shared/src/commonMain/kotlin/domain/model/`

**Action**: Remove or mark as `@Deprecated` models with no implementation:
- `Message.kt`, `Conversation` — no messaging routes
- `Subscription.kt` management models — no subscription management routes
- `PostListResponse.nextCursor` — pagination not implemented

### Acceptance Criteria
- [ ] Client and server agree on error response format
- [ ] All API responses use typed shared models
- [ ] No duplicate model definitions
- [ ] OpenSpec specs created for all 9 API groups
- [ ] Unused models documented or removed
- [ ] All existing tests pass
- [ ] Server starts and API responses match updated shared models
- [ ] Client compiles and correctly parses all API responses
- [ ] Core user flows work end-to-end: login, OTP, profile view (MVP)

---

## Phase 7: Frontend Polish (Medium Priority)

**Goal**: Improve code quality and consistency in the UI layer.
**Risk Level**: Low
**Estimated scope**: ~10 files changed

### Task 7.1: Align Validation Between ViewModel and Shared Module

**Files**: `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/auth/AuthViewModel.kt`

`AuthViewModel` has its own `isValidEmail`/`validateIdentifier` with different rules than `UserValidation` in shared.

**Action**: Replace ViewModel validation with calls to `UserValidation` from the shared module.

### Task 7.2: Move Hardcoded Strings to Strings Object

**Files**: `ProfileScreen.kt`, `EditProfileScreen.kt`, `MainScreen.kt`

**Action**: Add all UI strings to `Strings.kt` object for consistency and future i18n support.

### Task 7.3: Replace Hardcoded dp Values with Dimensions

**Files**: `SecondaryButton.kt`, `EmptyScreen.kt`, and others

**Action**: Use `Dimensions` constants instead of inline `.dp` values.

### Task 7.4: Add Global Auth State Observer

**Files**: `App.kt` or new `AuthStateManager.kt`

When token refresh fails (SharedModule clears tokens), the user stays on the main screen with a broken session.

**Action**: Create a global auth state observer that redirects to the login screen when tokens are cleared.

### Task 7.5: Delete Dead Code

**Files**:
- `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/components/PasswordInput.kt` — unused
- `shared/src/commonMain/kotlin/id/nearyou/app/Greeting.kt` — template code

### Task 7.6: Add @Immutable to ProfileUiState

**Files**: `ProfileViewModel.kt`

**Action**: Add `@Immutable` annotation to `ProfileUiState` data class for recomposition optimization.

### Acceptance Criteria
- [ ] All validation uses shared `UserValidation` module
- [ ] No hardcoded strings in screen composables
- [ ] No hardcoded dp values outside `Dimensions`
- [ ] Broken auth sessions redirect to login
- [ ] No dead code
- [ ] All existing tests pass
- [ ] Server and client compile and run
- [ ] Core user flows work: login, OTP, profile view (MVP)

---

## Phase Summary

| Phase | Focus | Risk | Severity Addressed | Prerequisites |
|-------|-------|------|-------------------|---------------|
| **0** | Critical Security | Medium | 3 Critical, 3 High | None |
| **1** | Build System & CI | Low | 5 Critical CI, 3 High | None |
| **2** | Frontend Architecture | Medium | 2 Critical, 6 High | None |
| **3** | Backend Hardening | Medium | 4 High, 8 Medium | Phase 0 |
| **4** | Test Quality | Low | 3 Critical, 2 High | Phases 1, 3 |
| **5** | Documentation | Low | — | Phases 2, 3 |
| **6** | API Alignment & Specs | Medium | 4 High drift issues | Phases 3, 5 |
| **7** | Frontend Polish | Low | 6 Medium, 5 Low | Phase 2 |

**Phases 0, 1, 2 can run in parallel** — they are independent.
**Phases 3-7 have dependencies** as shown above.

---

## Cross-Cutting Observations

### MEMORY.md vs Reality
A recurring theme: MEMORY.md records phases as "completed" when the code shows they were NOT implemented. Affected claims:
- Phase 0: SecureRandom for OTP — **still uses kotlin.random.Random**
- Phase 2: CORS, security headers, CallLogging, body limit, input validation, XSS sanitization — **NONE present in code**
- Phase 3: navigation-compose, @Serializable routes, Channel-based events, NavHost — **NONE present in code**

**Recommendation**: After each phase in this guide, verify the work in code (not just in docs/memory) before marking complete. Update MEMORY.md only after code changes are committed and tests pass.

### Dependency on Shared Module
The project's strongest architectural asset is the shared KMP module with `@Serializable` domain models and validation rules. Several issues stem from the server NOT using these shared resources (duplicate validation, duplicate models, mismatched formats). Phases 3 and 6 specifically address this alignment.

### Vibe-Coding Workflow Support
The spec-driven approach (Phase 6) is designed to support AI-assisted development by providing persistent, readable API contracts that survive across chat sessions. Combined with the documentation rationalization (Phase 5), this creates a lean, accurate knowledge base that AI assistants can efficiently consume.
