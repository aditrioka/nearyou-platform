# Testing User Profile Implementation (T-103)

**Quick guide to test the new user profile management endpoints**

---

## Prerequisites

Before testing, ensure you have:

1. **PostgreSQL running** (with PostGIS extension)
2. **Redis running** (for OTP storage)
3. **Server built successfully**

### Start Required Services

```bash
# Start PostgreSQL and Redis (if using Docker)
docker-compose up -d postgres redis

# Or start them individually if installed locally
# PostgreSQL: brew services start postgresql
# Redis: brew services start redis
```

---

## Option 1: Manual Testing with cURL

### Step 1: Build and Start the Server

```bash
# Build the server
./gradlew :server:build -x test

# Start the server
./gradlew :server:run
```

The server should start on `http://localhost:8080`

### Step 2: Register a New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "displayName": "Test User",
    "email": "test@example.com",
    "password": "securePassword123"
  }'
```

**Expected Response:**
```json
{
  "message": "OTP sent successfully",
  "identifier": "test@example.com",
  "expiresIn": 300
}
```

### Step 3: Verify OTP

Check your server logs for the OTP code (in development, it's logged to console).

```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "code": "123456",
    "type": "email"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "testuser",
    "displayName": "Test User",
    "email": "test@example.com",
    ...
  }
}
```

**Save the `accessToken` for the next steps!**

### Step 4: Get Current User Profile

```bash
# Replace YOUR_ACCESS_TOKEN with the token from Step 3
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Expected Response:**
```json
{
  "id": "uuid",
  "username": "testuser",
  "displayName": "Test User",
  "email": "test@example.com",
  "phone": null,
  "bio": null,
  "profilePhotoUrl": null,
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-28T12:00:00Z",
  "updatedAt": "2025-10-28T12:00:00Z"
}
```

### Step 5: Update User Profile

```bash
# Update display name and bio
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "John Doe",
    "bio": "Software developer from Jakarta"
  }'
```

**Expected Response:**
```json
{
  "id": "uuid",
  "username": "testuser",
  "displayName": "John Doe",
  "email": "test@example.com",
  "phone": null,
  "bio": "Software developer from Jakarta",
  "profilePhotoUrl": null,
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-28T12:00:00Z",
  "updatedAt": "2025-10-28T12:05:00Z"
}
```

### Step 6: Test Validation

**Test 1: Display name too long (should fail)**
```bash
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "This is a very long display name that exceeds fifty characters limit"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "error": "INVALID_DISPLAY_NAME",
  "message": "Display name must be between 1 and 50 characters"
}
```

**Test 2: Bio too long (should fail)**
```bash
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "error": "INVALID_BIO",
  "message": "Bio must not exceed 200 characters"
}
```

**Test 3: No authentication (should fail)**
```bash
curl -X GET http://localhost:8080/users/me
```

**Expected Response:** `401 Unauthorized`

---

## Option 2: Automated Testing

### Run Integration Tests

```bash
# Run all integration tests
./gradlew :server:test --tests "*IntegrationTest"

# Run only user profile tests
./gradlew :server:test --tests "UserProfileIntegrationTest"

# Run only auth tests
./gradlew :server:test --tests "AuthIntegrationTest"
```

**Expected Output:**
```
BUILD SUCCESSFUL in 8s
15 tests completed (all passing)
```

### Run All Server Tests

```bash
# Run all tests (excluding database tests if DB not running)
./gradlew :server:test -x :server:test --tests "*DatabaseConnectionTest*"
```

---

## Option 3: Using Postman/Insomnia

1. **Import the API collection** (create one from the API documentation)
2. **Set up environment variables:**
   - `BASE_URL`: `http://localhost:8080`
   - `ACCESS_TOKEN`: (will be set after login)

3. **Test flow:**
   - Register → Verify OTP → Get Profile → Update Profile

---

## Verification Checklist

- [ ] Server starts without errors
- [ ] Can register a new user
- [ ] Can verify OTP and receive tokens
- [ ] `GET /users/me` returns user profile with valid token
- [ ] `GET /users/me` returns 401 without token
- [ ] `PUT /users/me` updates profile successfully
- [ ] `PUT /users/me` validates display name (1-50 chars)
- [ ] `PUT /users/me` validates bio (max 200 chars)
- [ ] `PUT /users/me` accepts partial updates (only some fields)
- [ ] `PUT /users/me` returns 401 without token
- [ ] All integration tests pass
- [ ] Updated `updatedAt` timestamp after profile update

---

## Troubleshooting

### Server won't start
```bash
# Check if PostgreSQL is running
psql -U postgres -c "SELECT version();"

# Check if Redis is running
redis-cli ping
# Should return: PONG

# Check server logs
./gradlew :server:run --info
```

### Tests failing
```bash
# Clean and rebuild
./gradlew clean :server:build -x test

# Run tests with more output
./gradlew :server:test --info
```

### Database connection errors
```bash
# Verify database exists
psql -U postgres -c "\l"

# Run migrations
./gradlew :server:flywayMigrate
```

---

## Next Steps

After verifying the backend works:

1. **Test with frontend** (once ProfileScreen.kt is implemented)
2. **Test profile photo upload** (when S3/GCS integration is added)
3. **Test with real email/SMS** (when Twilio/SendGrid is integrated)
4. **Load testing** (test with multiple concurrent users)

---

## Related Documents

- **[API_DOCUMENTATION.md](../CORE/API_DOCUMENTATION.md)** → Full API reference
- **[PRE_PUSH_CHECKLIST.md](./PRE_PUSH_CHECKLIST.md)** → Pre-push verification
- **[VALIDATION_GUIDE.md](../CORE/VALIDATION_GUIDE.md)** → Validation procedures

