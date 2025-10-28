# Testing Authenticated Endpoints with cURL

Quick guide untuk test authenticated endpoints pakai cURL setelah dapet token.

---

## Prerequisites

1. ✅ Server running: `./gradlew :server:run`
2. ✅ Docker services running: `docker-compose up -d`
3. ✅ Sudah punya **access token** dari verify OTP

---

## Step 1: Get Your Access Token

Kalau belum punya token, register & verify OTP dulu:

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!@#"
  }'

# Response:
# {
#   "message": "OTP sent successfully",
#   "identifier": "test@example.com"
# }

# 2. Check OTP di server logs atau Redis
docker exec -it nearyou-redis redis-cli
> KEYS otp:*
> GET otp:test@example.com

# 3. Verify OTP
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "otp": "123456"
  }'

# Response:
# {
#   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "refreshToken": "...",
#   "user": { ... }
# }
```

**COPY** `accessToken` dari response!

---

## Step 2: Test GET /users/me

Get current user profile:

```bash
# Replace YOUR_TOKEN with your actual access token
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### Expected Response (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "testuser",
  "displayName": "testuser",
  "email": "test@example.com",
  "phone": null,
  "bio": null,
  "profilePhotoUrl": null,
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-28T10:30:00Z",
  "updatedAt": "2025-10-28T10:30:00Z"
}
```

### Error Cases:

**No token:**
```bash
curl -X GET http://localhost:8080/users/me
# Response: 401 Unauthorized
# {"error":"invalid_token","message":"Token is not valid or has expired"}
```

**Invalid token:**
```bash
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer invalid_token"
# Response: 401 Unauthorized
```

---

## Step 3: Test PUT /users/me

Update user profile:

```bash
export TOKEN="your_access_token_here"

curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "John Doe",
    "bio": "Software Engineer | Coffee Lover ☕",
    "profilePhotoUrl": "https://example.com/photos/john.jpg"
  }'
```

### Expected Response (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "testuser",
  "displayName": "John Doe",
  "email": "test@example.com",
  "phone": null,
  "bio": "Software Engineer | Coffee Lover ☕",
  "profilePhotoUrl": "https://example.com/photos/john.jpg",
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-28T10:30:00Z",
  "updatedAt": "2025-10-28T10:35:00Z"
}
```

### Partial Update (Only Update Some Fields):

```bash
# Update only displayName
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Jane Doe"
  }'

# Update only bio
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "New bio text"
  }'
```

---

## Step 4: Verify Changes

Get profile lagi untuk verify update berhasil:

```bash
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## Quick Test Script

Save this as `test-user-endpoints.sh`:

```bash
#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if token is provided
if [ -z "$1" ]; then
    echo -e "${RED}Usage: ./test-user-endpoints.sh YOUR_ACCESS_TOKEN${NC}"
    exit 1
fi

TOKEN=$1
BASE_URL="http://localhost:8080"

echo -e "${BLUE}=== Testing User Endpoints ===${NC}\n"

# Test 1: Get Profile
echo -e "${GREEN}1. GET /users/me${NC}"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .
echo -e "\n"

# Test 2: Update Profile
echo -e "${GREEN}2. PUT /users/me (Update displayName)${NC}"
curl -s -X PUT "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Updated Name",
    "bio": "Testing bio update"
  }' | jq .
echo -e "\n"

# Test 3: Verify Update
echo -e "${GREEN}3. GET /users/me (Verify update)${NC}"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .
echo -e "\n"

echo -e "${BLUE}=== Tests Complete ===${NC}"
```

Run it:

```bash
chmod +x test-user-endpoints.sh
./test-user-endpoints.sh "your_access_token_here"
```

---

## Troubleshooting

### 401 Unauthorized

**Problem:** Token invalid atau expired

**Solution:**
1. Check token format: harus `Bearer <token>`
2. Verify token belum expired (7 hari)
3. Get new token dengan verify OTP lagi

### 404 Not Found

**Problem:** Server belum running atau endpoint salah

**Solution:**
```bash
# Check server running
curl http://localhost:8080/health

# Should return:
# {"status":"healthy","service":"nearyou-id-api","version":"1.0.0"}
```

### Connection Refused

**Problem:** Server not running

**Solution:**
```bash
# Start server
./gradlew :server:run

# Or check if already running
lsof -i :8080
```

---

## Tips

1. **Save token to variable:**
   ```bash
   export TOKEN="your_long_token_here"
   # Then use: -H "Authorization: Bearer $TOKEN"
   ```

2. **Pretty print JSON with jq:**
   ```bash
   curl ... | jq .
   ```

3. **See full response with headers:**
   ```bash
   curl -v -X GET http://localhost:8080/users/me \
     -H "Authorization: Bearer $TOKEN"
   ```

4. **Check server logs:**
   ```bash
   # Terminal running ./gradlew :server:run
   # You'll see:
   # [AuthRepository] Fetching current user profile
   # [UserService] Getting user by ID: ...
   ```

---

## Next Steps

After testing manually with cURL:
1. ✅ Implement UI screens for profile view/edit
2. ✅ Add profile photo upload
3. ✅ Add form validation
4. ✅ Handle error states in UI

---

Happy Testing! 🚀

