# Frontend Architecture Assessment

**Date:** 2026-02-07
**Scope:** composeApp/, shared/, iosApp/ modules
**Overall Rating:** B+ (Solid foundation, some gaps to address)

---

## 1. Navigation Patterns

**Rating: C+**

**Current state:** Custom navigation using `mutableStateOf<Route>` with manual `when` blocks. No navigation library (e.g., Voyager, Decompose, or Compose Navigation).

**Issues:**
- `MainScreen.kt` uses string-based routing (`"home"`, `"profile"`, `"edit_profile"`) which is fragile and not type-safe
- `AuthNavigation.kt` uses a sealed class `AuthRoute` which is better, but the two approaches are inconsistent
- No back stack management -- pressing back on edit_profile or profile has no history-aware behavior
- No deep linking support
- No transition animations between screens
- Navigation state is lost on configuration change (Android rotation)

**Recommendation:** Adopt a proper navigation library. Decompose is the most mature choice for KMP, or use the official Compose Navigation with type-safe routes. At minimum, unify the approach -- all navigation should use sealed classes, not strings.

---

## 2. State Management

**Rating: A-**

**Current state:** MVI-style with `StateFlow<UiState>` in ViewModels, `@Immutable` annotations on state classes, and a separate `StateFlow<Event?>` for one-time events.

**Strengths:**
- Single source of truth per feature via immutable data classes
- `@Immutable` annotation on `AuthUiState` helps Compose skip recomposition
- Proper use of `StateFlow.update { }` for atomic state transitions
- Events separated from persistent state (navigation, snackbars)

**Issues:**
- `ProfileUiState` is missing `@Immutable` annotation (inconsistent with `AuthUiState`)
- Events use `MutableStateFlow<Event?>` which can lose events if two fire rapidly before consumption. A `Channel` or `SharedFlow(replay=0)` would be safer
- `EditProfileScreen` manages local state (`displayName`, `bio`, errors) alongside ViewModel state, creating a dual source of truth. This contradicts the documented MVI pattern
- Redundant try/catch in ViewModels -- `Result.fold` already handles failures, so the outer catch is only needed for unexpected exceptions from the repository call itself. Consider using a helper function

**Recommendation:** Use `Channel<Event>` for one-time events. Add `@Immutable` to all UI state classes. Move EditProfile local state into ProfileViewModel.

---

## 3. UI Component Structure and Reusability

**Rating: B+**

**Current state:** Reusable components in `ui/components/`: PrimaryButton, SecondaryButton, TextInput, PasswordInput, OtpInput, ErrorScreen, EmptyScreen.

**Strengths:**
- Good component API design -- `PrimaryButton` accepts `onClick`, `text`, `isLoading`, `enabled`, `modifier`
- Accessibility built into PrimaryButton via `semantics { contentDescription }`
- ErrorScreen and EmptyScreen provide consistent empty/error states
- Components use design system tokens (Dimensions, Spacing)

**Issues:**
- ProfileScreen uses `koinInject()` for ViewModel while LoginScreen uses `koinViewModel()` -- these have different lifecycle behavior. `koinViewModel()` ties to the Compose lifecycle; `koinInject()` returns a singleton. This means ProfileViewModel is a singleton across the app, which can cause stale state
- EditProfileScreen also uses `koinInject()` instead of `koinViewModel()`
- No preview annotations on individual components (only on `App()`)
- `ProfileContent` takes `domain.model.User` directly as a fully qualified import -- inconsistent with other screens
- Some magic numbers remain (e.g., `120.dp` for avatar, `60.dp` for icon, `48.dp` for camera icon in EditProfileScreen)

**Recommendation:** Standardize on `koinViewModel()` for all ViewModels. Add `@Preview` to components. Extract remaining magic numbers into Dimensions.

---

## 4. Design System Implementation

**Rating: A-**

**Current state:** Well-documented design system with Color, Typography, Spacing, Dimensions, and Strings centralized in `ui/theme/`.

**Strengths:**
- Full Material 3 color scheme with light/dark theme support
- Complete type scale following M3 guidelines
- Semantic spacing system (`Spacing.xs` through `Spacing.xxxl`)
- Centralized dimension constants (`Dimensions.BUTTON_HEIGHT`, etc.)
- Centralized strings (`Strings.kt`) ready for i18n
- Comprehensive DESIGN_SYSTEM.md documentation

**Issues:**
- No custom shapes defined (relies on M3 defaults) -- fine for now but limits brand differentiation
- Theme does not pass custom shapes to `MaterialTheme`
- Some screens still use inline dp values (e.g., `120.dp`, `60.dp` in ProfileScreen)
- No dynamic color support (Material You / Android 12+)

**Recommendation:** Minor -- extract remaining hardcoded sizes. Consider dynamic color as a future enhancement.

---

## 5. Platform-Specific Code (expect/actual)

**Rating: B+**

**Current state:** Uses `expect val platformModule: Module` in shared/ for DI, with actual implementations providing platform-specific `TokenStorage`.

**Strengths:**
- Clean separation via Koin platform modules
- `TokenStorage` interface with Android (EncryptedSharedPreferences) and iOS (Keychain) implementations
- Platform-specific Ktor engines (OkHttp for Android, Darwin for iOS)
- `Platform.kt` expect/actual for basic platform info

**Issues:**
- iOS `MainViewController.kt` calls `startKoin` every time `MainViewController()` is called. If SwiftUI recreates the view, this will crash with "Koin already started". Should use `startKoin` with a guard or `KoinApplication`
- No `androidMain` platform-specific code in composeApp (e.g., no `BackHandler` integration, no Activity result handling for Google Sign-In)
- `System.currentTimeMillis()` used in `EditProfileScreen.kt` line 63 -- this is JVM-specific and will not compile on iOS. Should use `kotlinx.datetime.Clock.System.now()`

**Recommendation:** Fix the iOS Koin initialization to be idempotent. Replace `System.currentTimeMillis()` with KMP-compatible alternative. Add proper Android back navigation handling.

---

## 6. Dependency Injection

**Rating: A-**

**Current state:** Koin 4.0.1 with three-layer module structure: `platformModule` (expect/actual), `sharedModule` (repositories, HttpClient), `appModule` (ViewModels).

**Strengths:**
- Clean module separation matching architecture layers
- ViewModels registered with `viewModel { }` DSL for lifecycle awareness
- HttpClient configured as singleton with auth, logging, timeout, and content negotiation
- Automatic token refresh via Ktor Auth bearer plugin

**Issues:**
- `appModule` registers ViewModels but screens inconsistently use `koinViewModel()` vs `koinInject()` (as noted in section 3)
- Repositories (`AuthRepository`, `UserRepository`) are registered as `single` but are not interfaces -- makes testing harder (no easy faking without mocking library)
- No scope management for feature-level DI (all ViewModels are at app scope)

**Recommendation:** Use interfaces for repositories to improve testability. Ensure all ViewModel injections use `koinViewModel()`.

---

## 7. Modern Compose Best Practices

**Rating: B+**

**Strengths:**
- Proper use of `collectAsState()` for StateFlow observation
- `@Immutable` on state classes for recomposition optimization
- `LaunchedEffect` for one-time event handling
- `windowInsetsPadding(WindowInsets.systemBars)` and `imePadding()` for proper insets
- Modifier parameter follows convention (last param, default `Modifier`)
- Coil 3 for image loading (KMP-compatible)
- Peekaboo for cross-platform image picking

**Issues:**
- No use of `rememberSaveable` -- form state is lost on process death (Android)
- No `derivedStateOf` usage where it could optimize (e.g., form validation)
- `Icons.Default.ArrowBack` is deprecated in favor of `Icons.AutoMirrored.Filled.ArrowBack`
- No Compose stability configuration file (`compose_compiler_config.conf`) to mark external classes as stable
- Loading overlay in EditProfileScreen overlaps content rather than using a proper modal/scrim pattern

**Recommendation:** Add `rememberSaveable` for critical form state. Use `Icons.AutoMirrored` variants. Add stability config for external models.

---

## 8. Frontend-Backend Contract Alignment

**Rating: A**

**Strengths:**
- Shared module (`shared/`) contains all domain models and DTOs used by both client and server
- `AuthModels.kt` defines `RegisterRequest`, `LoginRequest`, `VerifyOtpRequest`, `AuthResponse`, etc. -- single source of truth
- kotlinx.serialization used consistently across client and server
- `UserRepository` and `AuthRepository` in shared/ use the same DTOs the server expects
- Ktor client configured with proper content negotiation matching server expectations

**Issues:**
- No API versioning in client URLs (requests go to `/auth/login` not `/v1/auth/login`)
- No response envelope pattern -- errors are parsed ad-hoc via `parseErrorMessage`
- `ApiErrorResponse` exists but only used in AuthRepository, not in UserRepository

**Recommendation:** Standardize error handling across all repositories. Consider API versioning.

---

## Summary of Critical Issues

| Priority | Issue | Location |
|----------|-------|----------|
| **HIGH** | `System.currentTimeMillis()` won't compile on iOS | EditProfileScreen.kt:63 |
| **HIGH** | iOS Koin init will crash on view recreation | MainViewController.kt:11 |
| **HIGH** | `koinInject()` vs `koinViewModel()` mismatch causes singleton ViewModels | ProfileScreen.kt:31, EditProfileScreen.kt:38 |
| **MEDIUM** | String-based navigation in MainScreen is fragile | MainScreen.kt:30 |
| **MEDIUM** | Event channel can lose events (StateFlow vs Channel) | AuthViewModel.kt:72 |
| **MEDIUM** | No back stack / deep link support | AuthNavigation.kt, MainScreen.kt |
| **LOW** | Missing `@Immutable` on ProfileUiState | ProfileViewModel.kt:17 |
| **LOW** | Deprecated `Icons.Default.ArrowBack` | EditProfileScreen.kt:91 |
| **LOW** | No `rememberSaveable` for form persistence | EditProfileScreen.kt |

---

## Recommended Improvements (Prioritized)

### Phase 1 -- Critical Fixes
1. Replace `System.currentTimeMillis()` with `Clock.System.now().toEpochMilliseconds()`
2. Guard iOS Koin initialization (check `KoinPlatformTools.defaultContext().getOrNull()`)
3. Change `koinInject()` to `koinViewModel()` for all ViewModel injections

### Phase 2 -- Navigation
4. Adopt Decompose or Compose Navigation for type-safe routing with back stack
5. Replace string-based navigation in MainScreen with sealed classes
6. Add screen transition animations

### Phase 3 -- Robustness
7. Switch one-time events from `StateFlow<Event?>` to `Channel<Event>`
8. Move EditProfile local state into ProfileViewModel
9. Add `@Immutable` to all UI state classes
10. Add Compose stability configuration file

### Phase 4 -- Polish
11. Add `@Preview` annotations to all reusable components
12. Standardize error handling across repositories
13. Add `rememberSaveable` for form state persistence
14. Consider Decompose for proper KMP navigation with state restoration
