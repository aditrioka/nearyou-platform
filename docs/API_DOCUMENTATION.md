# Near You ID - API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Last Updated:** 2025-10-24

## Overview

Near You ID provides a secure authentication API with OTP-based verification, JWT token management, and OAuth integration.

### Key Features

- ✅ OTP-based authentication (Email/Phone)
- ✅ JWT access and refresh tokens
- ✅ Google OAuth integration
- ✅ Secure password hashing (BCrypt)
- ✅ Rate limiting
- ✅ Centralized error handling

---

## Authentication

All protected endpoints require a valid JWT access token in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

---

## Endpoints

### 1. Register User

Register a new user account and send OTP for verification.

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "username": "string (3-20 chars, alphanumeric + underscore)",
  "displayName": "string (1-50 chars)",
  "email": "string (optional, required if phone is null)",
  "phone": "string (optional, required if email is null)",
  "password": "string (optional, for password-based auth)"
}
```

**Success Response (200 OK):**
```json
{
  "message": "OTP sent successfully",
  "identifier": "user@example.com",
  "type": "email",
  "expiresInSeconds": 300
}
```

**Error Responses:**

- **409 Conflict** - Email/Phone/Username already exists
  ```json
  {
    "error": {
      "code": "EMAIL_EXISTS",
      "message": "Email already registered",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

- **429 Too Many Requests** - Rate limit exceeded
  ```json
  {
    "error": {
      "code": "RATE_LIMIT_EXCEEDED",
      "message": "Too many OTP requests. Please try again later.",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

---

### 2. Login User

Send OTP to existing user for authentication.

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "email": "string (optional)",
  "phone": "string (optional)"
}
```

**Success Response (200 OK):**
```json
{
  "message": "OTP sent successfully",
  "identifier": "user@example.com",
  "type": "email",
  "expiresInSeconds": 300
}
```

**Error Responses:**

- **404 Not Found** - User not found
  ```json
  {
    "error": {
      "code": "USER_NOT_FOUND",
      "message": "User not found",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

---

### 3. Verify OTP

Verify OTP code and receive authentication tokens.

**Endpoint:** `POST /auth/verify-otp`

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "code": "123456",
  "type": "email"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "testuser",
    "displayName": "Test User",
    "email": "user@example.com",
    "phone": null,
    "bio": null,
    "profilePhotoUrl": null,
    "isVerified": true,
    "subscriptionTier": "FREE",
    "createdAt": "2025-10-24T12:00:00Z",
    "updatedAt": "2025-10-24T12:00:00Z"
  }
}
```

**Error Responses:**

- **401 Unauthorized** - Invalid or expired OTP
  ```json
  {
    "error": {
      "code": "INVALID_OTP",
      "message": "Invalid or expired OTP",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

---

### 4. Refresh Token

Refresh access token using refresh token.

**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**

- **401 Unauthorized** - Invalid or expired refresh token
  ```json
  {
    "error": {
      "code": "INVALID_TOKEN",
      "message": "Invalid or expired refresh token",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

---

### 5. Google OAuth Login

Authenticate using Google ID token.

**Endpoint:** `POST /auth/google`

**Request Body:**
```json
{
  "idToken": "google_id_token_here"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

---

### 6. Get Current User

Get authenticated user's profile.

**Endpoint:** `GET /auth/me`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Success Response (200 OK):**
```json
{
  "id": "uuid",
  "username": "testuser",
  "displayName": "Test User",
  "email": "user@example.com",
  "phone": null,
  "bio": "User bio",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-24T12:00:00Z",
  "updatedAt": "2025-10-24T12:00:00Z"
}
```

**Error Responses:**

- **401 Unauthorized** - Missing or invalid token

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `EMAIL_EXISTS` | 409 | Email already registered |
| `PHONE_EXISTS` | 409 | Phone already registered |
| `USERNAME_EXISTS` | 409 | Username already taken |
| `USER_NOT_FOUND` | 404 | User not found |
| `INVALID_OTP` | 401 | Invalid or expired OTP |
| `INVALID_TOKEN` | 401 | Invalid or expired token |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `VALIDATION_ERROR` | 400 | Invalid request data |
| `INTERNAL_ERROR` | 500 | Internal server error |

---

## Rate Limiting

- **OTP Requests:** 1 request per 60 seconds per identifier
- **API Requests:** 100 requests per minute per IP (planned)

---

## Token Expiration

- **Access Token:** 7 days
- **Refresh Token:** 30 days
- **OTP Code:** 5 minutes

---

## Security

### Password Hashing
- Algorithm: BCrypt
- Rounds: 12
- Passwords are hashed before storage

### JWT Tokens
- Algorithm: HMAC256
- Tokens include user ID and expiration
- Refresh tokens are stored in database and can be revoked

### HTTPS
- All production endpoints must use HTTPS
- Tokens should never be transmitted over HTTP

---

## Examples

### cURL Examples

**Register:**
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

**Verify OTP:**
```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "code": "123456",
    "type": "email"
  }'
```

**Get Current User:**
```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Changelog

### Version 1.0.0 (2025-10-24)
- Initial API release
- OTP-based authentication
- JWT token management
- Google OAuth integration
- Centralized error handling
- Rate limiting

---

## Support

For issues or questions, please contact the development team or create an issue in the repository.

