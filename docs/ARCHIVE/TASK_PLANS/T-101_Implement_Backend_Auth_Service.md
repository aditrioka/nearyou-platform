# Task Plan: T-101 - Implement Backend Auth Service

## 📋 Task Context
- **Task ID:** T-101
- **Title:** Implement Backend Auth Service
- **Phase:** Phase 1 - Authentication & User Management
- **Related Plan:** [NearYou_ID_MVP_Plan.md](../PLANS/NearYou_ID_MVP_Plan.md#t-101-implement-backend-auth-service)
- **Dependencies:** 
  - T-002 (Database Setup) - ✅ DONE
  - T-003 (User Model) - ✅ DONE

---

## 🎯 Scope

### Purpose
Create a complete authentication service with JWT generation, OTP handling, and user registration for the NearYou ID backend.

### In Scope
1. JWT token generation and validation (7-day access token, 30-day refresh token)
2. OTP generation and verification (6-digit code, 5-minute expiry)
3. User registration with email/phone
4. User login with email/phone + OTP
5. Google OAuth login support (endpoint structure)
6. Token refresh mechanism
7. Password hashing with BCrypt
8. Rate limiting (5 OTP requests per hour per user)
9. Integration with PostgreSQL users table
10. Redis integration for OTP storage and rate limiting

### Out of Scope
- Frontend auth screens (T-102)
- Actual Twilio/SendGrid integration (using mock for MVP)
- Google OAuth implementation details (placeholder endpoint)
- User profile management (T-103)

---

## 📦 Expected Artifacts

### Code Files
1. **server/src/main/kotlin/id/nearyou/app/auth/AuthService.kt**
   - OTP generation and verification
   - JWT token generation
   - User registration and login logic
   - Password hashing

2. **server/src/main/kotlin/id/nearyou/app/auth/AuthRoutes.kt**
   - POST /auth/register
   - POST /auth/verify-otp
   - POST /auth/login/google
   - POST /auth/refresh

3. **server/src/main/kotlin/id/nearyou/app/auth/JwtConfig.kt**
   - JWT configuration
   - Token generation utilities
   - Token validation

4. **server/src/main/kotlin/id/nearyou/app/auth/models/AuthModels.kt**
   - Request/Response DTOs
   - RegisterRequest, VerifyOtpRequest, RefreshTokenRequest
   - AuthResponse, TokenResponse

5. **server/src/main/kotlin/id/nearyou/app/config/DatabaseConfig.kt**
   - Database connection configuration
   - Exposed database setup

6. **server/src/main/kotlin/id/nearyou/app/config/EnvironmentConfig.kt**
   - Environment variable loading
   - Configuration object

7. **server/src/main/kotlin/id/nearyou/app/plugins/Authentication.kt**
   - Ktor authentication plugin configuration
   - JWT authentication setup

8. **server/src/main/kotlin/id/nearyou/app/plugins/Serialization.kt**
   - Content negotiation plugin
   - JSON serialization setup

9. **server/src/main/kotlin/id/nearyou/app/repository/UserRepository.kt**
   - Database operations for users
   - User CRUD operations

### Database Migration
10. **database/migrations/002_auth_tables.sql**
    - otp_codes table
    - refresh_tokens table
    - rate_limits table (optional, can use Redis)

### Configuration Updates
11. **server/build.gradle.kts** (updated)
    - Add ktor-server-auth
    - Add ktor-server-auth-jwt
    - Add ktor-server-content-negotiation
    - Add ktor-serialization-kotlinx-json
    - Add bcrypt (jbcrypt)
    - Add lettuce-core (Redis client)

12. **server/src/main/kotlin/id/nearyou/app/Application.kt** (updated)
    - Configure authentication plugin
    - Configure serialization plugin
    - Register auth routes

---

## 🔧 Implementation Steps

### Step 1: Add Dependencies
Update `server/build.gradle.kts` with required dependencies:
- ktor-server-auth
- ktor-server-auth-jwt
- ktor-server-content-negotiation
- ktor-serialization-kotlinx-json
- jbcrypt (BCrypt implementation)
- lettuce-core (Redis client)

### Step 2: Create Database Migration
Create `002_auth_tables.sql` with:
- otp_codes table (id, user_id, code, type, expires_at, created_at)
- refresh_tokens table (id, token, user_id, expires_at, created_at, is_revoked)

### Step 3: Create Configuration Files
- EnvironmentConfig.kt - Load JWT_SECRET, JWT_ISSUER, etc.
- DatabaseConfig.kt - Database connection setup
- JwtConfig.kt - JWT token generation and validation

### Step 4: Create Auth Models
- Request DTOs (RegisterRequest, VerifyOtpRequest, etc.)
- Response DTOs (AuthResponse, TokenResponse, etc.)

### Step 5: Create Repository Layer
- UserRepository.kt - Database operations for users

### Step 6: Create Auth Service
- AuthService.kt with methods:
  - generateOtp()
  - verifyOtp()
  - registerUser()
  - loginUser()
  - generateTokens()
  - refreshToken()
  - hashPassword()
  - verifyPassword()

### Step 7: Create Ktor Plugins
- Authentication.kt - Configure JWT authentication
- Serialization.kt - Configure JSON serialization

### Step 8: Create Auth Routes
- AuthRoutes.kt with endpoints:
  - POST /auth/register
  - POST /auth/verify-otp
  - POST /auth/login/google (placeholder)
  - POST /auth/refresh

### Step 9: Update Application.kt
- Install authentication plugin
- Install serialization plugin
- Register auth routes

### Step 10: Add Rate Limiting
- Implement rate limiting in AuthService using Redis
- 5 OTP requests per hour per user

---

## ✅ Definition of Done

- [ ] All dependencies added to build.gradle.kts
- [ ] Database migration 002_auth_tables.sql created and documented
- [ ] All auth endpoints functional (register, verify-otp, refresh)
- [ ] JWT tokens generated with correct expiry (7 days access, 30 days refresh)
- [ ] OTP generated as 6-digit code with 5-minute expiry
- [ ] Password hashing with BCrypt implemented
- [ ] Rate limiting enforced (5 OTP requests per hour)
- [ ] Code follows Kotlin conventions and project structure
- [ ] All files compile without errors
- [ ] Integration with PostgreSQL users table working
- [ ] Redis integration for OTP storage and rate limiting
- [ ] Mock OTP provider for development (logs OTP to console)

---

## 🧪 Validation Plan

### Validation Owner
**HYBRID** - AI validates code structure and compilation, Human validates runtime behavior

### AI Capability
- ✅ Verify all files created in correct locations
- ✅ Verify dependencies added to build.gradle.kts
- ✅ Verify code syntax and structure
- ✅ Verify Kotlin conventions followed
- ✅ Verify database migration SQL syntax
- ✅ Verify all imports and references are correct

### Human Prerequisites
- ⚠️ Running PostgreSQL database with migrations applied
- ⚠️ Running Redis instance
- ⚠️ Server running on port 8080
- ⚠️ Testing endpoints with Postman/curl
- ⚠️ Verifying OTP generation in logs
- ⚠️ Verifying JWT token structure and expiry

### Evidence Required
1. All code files created (file paths)
2. Build.gradle.kts updated with dependencies
3. Code compiles successfully (build output)
4. Database migration file created
5. Application.kt updated with plugins and routes

### Pass Criteria
- ✅ All expected artifact files exist
- ✅ Code compiles without errors
- ✅ All imports resolve correctly
- ✅ Database migration SQL is valid
- ✅ Follows project package structure (id.nearyou.app.*)
- ✅ Follows Kotlin coding conventions
- ✅ All endpoints defined in AuthRoutes.kt
- ✅ JWT configuration uses environment variables

---

## 📚 Affected Documentation

### To Update (if needed)
- None initially - implementation follows existing architecture

### To Reference
- [ARCHITECTURE.md](../CORE/ARCHITECTURE.md) - Server structure
- [INFRA.md](../CORE/INFRA.md) - Environment variables
- [SPEC.md](../CORE/SPEC.md) - Authentication requirements
- [DECISIONS.md](../CORE/DECISIONS.md) - JWT decision (ADR-004)

---

## 🔄 Rollback Strategy

If implementation fails or causes issues:
1. Revert all code changes in server/src/main/kotlin/id/nearyou/app/
2. Revert build.gradle.kts changes
3. Drop auth tables from database (if migration was applied)
4. Restore Application.kt to previous state

---

## 📝 Notes

- Using mock OTP provider for MVP (logs to console instead of sending SMS/email)
- Google OAuth endpoint is a placeholder - actual implementation in future task
- Rate limiting uses Redis for distributed rate limiting
- OTP codes stored in Redis with TTL instead of database for better performance
- Refresh tokens stored in database for persistence and revocation capability
- BCrypt work factor set to 12 for password hashing
- JWT secret must be changed in production (use strong random value)

---

**Created:** 2025-10-17  
**Status:** Ready for Implementation

