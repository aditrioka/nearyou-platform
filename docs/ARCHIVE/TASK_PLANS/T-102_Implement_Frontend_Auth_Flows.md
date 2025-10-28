# T-102: Implement Frontend Auth Flows

**Task ID:** T-102
**Phase:** Phase 1 - Authentication & User Management
**Status:** Completed
**Created:** 2025-10-18
**Completed:** 2025-10-21
**Dependencies:** T-101 (Backend Auth Service)

---

## Purpose

Create login/signup UI with OTP verification and token storage for the NearYou ID mobile application using Compose Multiplatform.

---

## Scope

### In Scope
1. Login screen with email/phone input
2. Signup screen with user registration form
3. OTP verification screen
4. Google Sign-In integration (platform-specific)
5. AuthRepository for API communication
6. Secure token storage (Keystore/Keychain)
7. Navigation logic (authenticated vs unauthenticated routes)
8. Error handling and user-friendly messages

### Out of Scope
- Backend authentication endpoints (already implemented in T-101)
- Profile management (T-103)
- Password reset functionality (future enhancement)

---

## Dependencies

### Required Tasks
- **T-101:** Backend Auth Service (COMPLETED)
  - Provides `/auth/register`, `/auth/login`, `/auth/verify-otp`, `/auth/resend-otp`, `/auth/refresh` endpoints
  - JWT token generation and validation
  - OTP generation and storage in Redis

### External Dependencies
- Compose Multiplatform 1.9.0
- Ktor Client for HTTP requests
- Kotlinx Serialization for JSON
- Platform-specific: Google Sign-In SDK (Android/iOS)
- Platform-specific: Keystore (Android), Keychain (iOS)

---

## Affected Modules

### `/composeApp` Module
- `src/commonMain/kotlin/ui/auth/` - Auth screens
- `src/commonMain/kotlin/navigation/` - Navigation setup
- `src/androidMain/kotlin/` - Android-specific implementations
- `src/iosMain/kotlin/` - iOS-specific implementations

### `/shared` Module
- `src/commonMain/kotlin/data/` - AuthRepository, TokenStorage interface
- `src/commonMain/kotlin/domain/model/auth/` - Auth DTOs (single source of truth)
- `src/androidMain/kotlin/data/` - Android Keystore implementation
- `src/iosMain/kotlin/data/` - iOS Keychain implementation
- `src/jvmMain/kotlin/data/` - JVM in-memory implementation (for testing)

### `/server` Module
- Imports all auth DTOs from `/shared` module to ensure type consistency

---

## Implementation Steps

### Step 1: Create Shared Auth Models (if not exists)
- Define request/response models matching backend API
- Location: `shared/src/commonMain/kotlin/domain/model/auth/`

### Step 2: Create AuthRepository
- HTTP client setup with Ktor
- API methods: register, verifyOtp, refreshToken, loginWithGoogle
- Location: `shared/src/commonMain/kotlin/data/AuthRepository.kt`

### Step 3: Create Token Storage Interface
- Define common interface for secure storage
- Platform-specific implementations
- Location: `shared/src/commonMain/kotlin/data/TokenStorage.kt`

### Step 4: Implement Android Token Storage
- Use Android Keystore
- Location: `shared/src/androidMain/kotlin/data/TokenStorageAndroid.kt`

### Step 5: Implement iOS Token Storage
- Use iOS Keychain
- Location: `shared/src/iosMain/kotlin/data/TokenStorageIOS.kt`

### Step 6: Create Auth Screens
- LoginScreen.kt - Email/phone input, navigation to signup
- SignupScreen.kt - Registration form
- OtpVerificationScreen.kt - OTP input and verification
- Location: `composeApp/src/commonMain/kotlin/ui/auth/`

### Step 7: Implement Navigation
- Define auth navigation graph
- Authenticated vs unauthenticated routes
- Location: `composeApp/src/commonMain/kotlin/navigation/`

### Step 8: Implement Google Sign-In (Android)
- Add Google Sign-In SDK dependency
- Platform-specific implementation
- Location: `composeApp/src/androidMain/kotlin/auth/`

### Step 9: Implement Google Sign-In (iOS)
- Add Google Sign-In SDK dependency
- Platform-specific implementation
- Location: `composeApp/src/iosMain/kotlin/auth/`

### Step 10: Error Handling
- Create error message mapping
- Display user-friendly error messages
- Handle network errors, validation errors

---

## Expected Artifacts

### Code Files
- `shared/src/commonMain/kotlin/data/AuthRepository.kt`
- `shared/src/commonMain/kotlin/data/TokenStorage.kt`
- `shared/src/androidMain/kotlin/data/TokenStorageAndroid.kt`
- `shared/src/iosMain/kotlin/data/TokenStorageIOS.kt`
- `composeApp/src/commonMain/kotlin/ui/auth/LoginScreen.kt`
- `composeApp/src/commonMain/kotlin/ui/auth/SignupScreen.kt`
- `composeApp/src/commonMain/kotlin/ui/auth/OtpVerificationScreen.kt`
- `composeApp/src/commonMain/kotlin/navigation/AuthNavigation.kt`
- `composeApp/src/androidMain/kotlin/auth/GoogleSignInAndroid.kt`
- `composeApp/src/iosMain/kotlin/auth/GoogleSignInIOS.kt`

### Documentation Updates
- None required (implementation follows existing architecture)

---

## Validation Plan

### Validation Owner
**HYBRID** - AI handles compilation and structure validation, Human handles UI/UX and device testing

### AI Capability
**What AI Can Validate:**
- Code compiles successfully for all targets (Android, iOS, JVM)
- File structure matches architecture guidelines
- Kotlin syntax and type safety
- API integration code structure
- Navigation logic structure

**What AI Cannot Validate:**
- Actual UI appearance and user experience
- Google Sign-In flow on real devices
- Token storage security on actual devices
- Network requests to running backend
- Platform-specific behavior (Keystore/Keychain)

### Human Prerequisites
**Required Setup:**
1. Backend server running (from T-101)
2. Android device/emulator with Google Play Services
3. iOS device/simulator with Google Sign-In configured
4. Google OAuth credentials configured
5. Network connectivity to backend API

**Required Actions:**
1. Test signup flow end-to-end
2. Test login flow with OTP
3. Test Google Sign-In on both platforms
4. Verify token persistence across app restarts
5. Test error scenarios (invalid OTP, network errors)

### Evidence Required
**AI Evidence:**
- Compilation output showing successful build
- File structure verification
- Code review of key components

**Human Evidence:**
- Screenshots of each auth screen
- Video of complete auth flow
- Confirmation of token persistence
- Error handling screenshots

### Pass Criteria
**AI Pass Criteria:**
- ✅ All Kotlin files compile without errors
- ✅ No type safety violations
- ✅ File structure matches plan
- ✅ Navigation logic implemented

**Human Pass Criteria:**
- ✅ User can sign up with email/phone
- ✅ OTP verification works correctly
- ✅ Google Sign-In works on Android
- ✅ Google Sign-In works on iOS
- ✅ Tokens persist across app restarts
- ✅ Error messages are user-friendly
- ✅ Navigation flows correctly

---

## Testing Strategy

### Unit Tests
- AuthRepository methods
- Token storage interface
- Input validation logic

### Integration Tests
- API communication with backend
- Token refresh flow
- Error handling

### UI Tests
- Screen rendering
- Form validation
- Navigation flows

### Manual Testing
- Complete signup flow
- Complete login flow
- Google Sign-In flow
- Token persistence
- Error scenarios

---

## Rollback Strategy

If issues arise:
1. Revert all commits on this branch
2. Delete branch: `git branch -D task/T-102-frontend-auth-flows`
3. Return to main branch
4. No database changes required (frontend only)

---

## Definition of Done

- [x] All code files created and implemented
- [x] Code compiles for Android, iOS targets
- [x] AuthRepository communicates with backend API
- [x] Token storage works on both platforms
- [x] All auth screens functional
- [x] Navigation logic implemented
- [ ] Google Sign-In integrated (both platforms) - Deferred to future task
- [x] Error handling implemented
- [x] AI validation passed (compilation)
- [x] Human validation passed (UI/device testing)
- [x] Validation report created
- [x] Progress Ledger updated
- [x] Changes committed and pushed
- [x] **BONUS:** Refactored to KMP best practices (shared models)

---

## Notes

- Google Sign-In implementation is platform-specific and requires OAuth credentials (deferred to future task)
- Token storage must use platform-specific secure storage (Keystore/Keychain)
- Backend endpoints from T-101 are already tested and functional
- Follow Compose Multiplatform best practices for shared UI code
- Use expect/actual pattern for platform-specific implementations
- **IMPORTANT:** All DTOs are defined in `/shared` module and imported by both client and server to ensure type consistency (KMP best practice)
- Database layer uses mapping to convert internal enums to shared model enums

### Issues Encountered and Resolved

1. **Network Security Error:** Android blocked cleartext HTTP to localhost
   - **Solution:** Added `android:usesCleartextTraffic="true"` to AndroidManifest.xml for development

2. **Signup/Login Screens Not Calling API:** Buttons navigated directly without backend calls
   - **Solution:** Injected `AuthViewModel` using Koin DI and called API methods before navigation

3. **Serialization Error:** Server sending `subscriptionTier: "free"` but client expecting `SubscriptionTier.FREE`
   - **Root Cause:** Duplicate model definitions between server and client
   - **Solution:** Major refactoring to use shared models as single source of truth (see ADR-014)

4. **Smart Cast Issues:** Kotlin couldn't smart cast nullable properties from different modules
   - **Solution:** Assigned nullable properties to local variables before null checks

---

## References

- **Backend API:** `server/src/main/kotlin/id/nearyou/app/auth/AuthRoutes.kt`
- **Shared Auth Models:** `shared/src/commonMain/kotlin/domain/model/auth/AuthModels.kt` (single source of truth)
- **Architecture:** `docs/CORE/ARCHITECTURE.md`
- **ADR-014:** Shared Models as Single Source of Truth (`docs/CORE/DECISIONS.md`)
- **MVP Plan:** `docs/PLANS/NearYou_ID_MVP_Plan.md` (Lines 327-360)

