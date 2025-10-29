# Near You ID - API Documentation

**Version:** 1.1.0
**Base URL:** `http://localhost:8080`
**Last Updated:** 2025-10-29

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

## API Design Philosophy

This API follows RESTful design principles with clear separation of concerns:

- **`/auth/*`** - Authentication **actions** (login, register, verify, refresh, logout)
- **`/users/*`** - User resource **CRUD operations** (read, update, delete)
- **`/users/me`** - Special resource representing the authenticated user

> **Important:** Use `GET /users/me` to get the current user's profile, not `/auth/me`.
> The `/auth/*` endpoints return user data as part of authentication responses, but for dedicated profile operations, use `/users/me`.

---

## Endpoints

### Authentication Endpoints

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

## User Profile Endpoints

> **Note:** For getting the current user's profile, use `GET /users/me` (not `/auth/me`).
> The `/auth/*` endpoints are for authentication actions only (login, register, verify, refresh).

### 6. Get Current User Profile

Get authenticated user's profile.

**Endpoint:** `GET /users/me`

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
- **404 Not Found** - User not found

**Example:**
```bash
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 7. Update Current User Profile

Update authenticated user's profile information.

**Endpoint:** `PUT /users/me`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "displayName": "New Display Name",
  "bio": "Updated bio text",
  "profilePhotoUrl": "https://example.com/new-photo.jpg"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Success Response (200 OK):**
```json
{
  "id": "uuid",
  "username": "testuser",
  "displayName": "New Display Name",
  "email": "user@example.com",
  "phone": null,
  "bio": "Updated bio text",
  "profilePhotoUrl": "https://example.com/new-photo.jpg",
  "isVerified": true,
  "subscriptionTier": "FREE",
  "createdAt": "2025-10-24T12:00:00Z",
  "updatedAt": "2025-10-24T13:30:00Z"
}
```

**Error Responses:**

- **400 Bad Request** - Invalid input (e.g., display name too long, bio exceeds 200 characters)
- **401 Unauthorized** - Missing or invalid token
- **404 Not Found** - User not found

**Validation Rules:**
- `displayName`: 1-50 characters
- `bio`: 0-200 characters
- `profilePhotoUrl`: Must be a valid URL (if provided)

**Example:**
```bash
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "John Doe",
    "bio": "Software developer from Jakarta"
  }'
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `EMAIL_EXISTS` | 409 | Email already registered |
| `INVALID_DISPLAY_NAME` | 400 | Display name validation failed |
| `INVALID_BIO` | 400 | Bio exceeds 200 characters |
| `INVALID_PHOTO_URL` | 400 | Profile photo URL is invalid |
| `USER_NOT_FOUND` | 404 | User not found |
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

**Get Current User Profile:**
```bash
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Update User Profile:**
```bash
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "John Doe",
    "bio": "Software developer from Jakarta"
  }'
```

---

## Post Endpoints

### 1. Get Nearby Posts

Get posts within a specified radius of a location using PostGIS spatial queries.

**Endpoint:** `GET /posts/nearby`

**Authentication:** Required (JWT Bearer token)

**Query Parameters:**
- `lat` (required): Latitude of the user's location (e.g., -6.2088)
- `lon` (required): Longitude of the user's location (e.g., 106.8456)
- `radius` (required): Search radius in meters. Must be one of: 1000, 5000, 10000, 20000
- `limit` (optional): Maximum number of posts to return (1-100, default: 20)

**Success Response (200 OK):**
```json
{
  "posts": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "content": "Great coffee at this cafe!",
      "location": {
        "latitude": -6.2088,
        "longitude": 106.8456
      },
      "mediaUrls": [
        "https://example.com/image1.jpg"
      ],
      "likeCount": 42,
      "commentCount": 5,
      "isLikedByCurrentUser": false,
      "distance": 150.5,
      "createdAt": "2025-10-29T10:30:00Z",
      "updatedAt": "2025-10-29T10:30:00Z",
      "author": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "username": "johndoe",
        "displayName": "John Doe",
        "profilePhotoUrl": "https://example.com/profile.jpg",
        "subscriptionTier": "PREMIUM"
      }
    }
  ]
}
```

**Error Responses:**

- **400 Bad Request** - Invalid parameters
  ```json
  {
    "error": {
      "code": "INVALID_RADIUS",
      "message": "Radius must be one of: 1000, 5000, 10000, 20000 meters",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

- **401 Unauthorized** - Missing or invalid token

### 2. Create Post

Create a new post at the user's current location.

**Endpoint:** `POST /posts`

**Authentication:** Required (JWT Bearer token)

**Request Body:**
```json
{
  "content": "string (1-500 chars)",
  "location": {
    "latitude": -6.2088,
    "longitude": 106.8456
  },
  "mediaUrls": ["https://example.com/image.jpg"] // Optional, premium users only
}
```

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "content": "Great coffee at this cafe!",
  "location": {
    "latitude": -6.2088,
    "longitude": 106.8456
  },
  "mediaUrls": ["https://example.com/image.jpg"],
  "likeCount": 0,
  "commentCount": 0,
  "isLikedByCurrentUser": false,
  "distance": null,
  "createdAt": "2025-10-29T10:30:00Z",
  "updatedAt": "2025-10-29T10:30:00Z",
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "johndoe",
    "displayName": "John Doe",
    "profilePhotoUrl": "https://example.com/profile.jpg",
    "subscriptionTier": "PREMIUM"
  }
}
```

**Error Responses:**

- **400 Bad Request** - Invalid content
  ```json
  {
    "error": {
      "code": "INVALID_CONTENT",
      "message": "Content must be between 1 and 500 characters",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

- **403 Forbidden** - Media upload requires premium subscription
  ```json
  {
    "error": {
      "code": "PREMIUM_REQUIRED",
      "message": "Media upload is only available for premium users",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

### 3. Get Post by ID

Retrieve a specific post by its ID.

**Endpoint:** `GET /posts/:id`

**Authentication:** Required (JWT Bearer token)

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "content": "Great coffee at this cafe!",
  "location": {
    "latitude": -6.2088,
    "longitude": 106.8456
  },
  "mediaUrls": ["https://example.com/image.jpg"],
  "likeCount": 42,
  "commentCount": 5,
  "isLikedByCurrentUser": false,
  "distance": null,
  "createdAt": "2025-10-29T10:30:00Z",
  "updatedAt": "2025-10-29T10:30:00Z",
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "johndoe",
    "displayName": "John Doe",
    "profilePhotoUrl": "https://example.com/profile.jpg",
    "subscriptionTier": "PREMIUM"
  }
}
```

**Error Responses:**

- **404 Not Found** - Post not found or deleted

### 4. Update Post

Update a post's content (owner only).

**Endpoint:** `PUT /posts/:id`

**Authentication:** Required (JWT Bearer token)

**Request Body:**
```json
{
  "content": "Updated content (1-500 chars)"
}
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "content": "Updated content",
  "location": {
    "latitude": -6.2088,
    "longitude": 106.8456
  },
  "mediaUrls": [],
  "likeCount": 42,
  "commentCount": 5,
  "isLikedByCurrentUser": false,
  "distance": null,
  "createdAt": "2025-10-29T10:30:00Z",
  "updatedAt": "2025-10-29T11:00:00Z",
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "johndoe",
    "displayName": "John Doe",
    "profilePhotoUrl": "https://example.com/profile.jpg",
    "subscriptionTier": "FREE"
  }
}
```

**Error Responses:**

- **403 Forbidden** - Not the post owner
  ```json
  {
    "error": {
      "code": "AUTHORIZATION_ERROR",
      "message": "You can only update your own posts",
      "timestamp": 1234567890,
      "details": null
    }
  }
  ```

- **404 Not Found** - Post not found

### 5. Delete Post

Soft delete a post (owner only).

**Endpoint:** `DELETE /posts/:id`

**Authentication:** Required (JWT Bearer token)

**Success Response (204 No Content)**

**Error Responses:**

- **403 Forbidden** - Not the post owner
- **404 Not Found** - Post not found

---

## Example Usage

**Get Nearby Posts:**
```bash
curl -X GET "http://localhost:8080/posts/nearby?lat=-6.2088&lon=106.8456&radius=1000&limit=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Create Post:**
```bash
curl -X POST http://localhost:8080/posts \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great coffee at this cafe!",
    "location": {
      "latitude": -6.2088,
      "longitude": 106.8456
    }
  }'
```

**Update Post:**
```bash
curl -X PUT http://localhost:8080/posts/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Updated content"
  }'
```

**Delete Post:**
```bash
curl -X DELETE http://localhost:8080/posts/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Changelog

### Version 1.1.0 (2025-10-29)
- Added post endpoints with PostGIS geo queries
  - `GET /posts/nearby` - Get posts within radius using spatial queries
  - `POST /posts` - Create new post
  - `GET /posts/:id` - Get post by ID
  - `PUT /posts/:id` - Update post (owner only)
  - `DELETE /posts/:id` - Delete post (owner only)
- Support for 4 distance levels: 1km, 5km, 10km, 20km
- Premium user restrictions for media uploads
- Distance calculation and sorting

### Version 1.0.1 (2025-10-28)
- Added user profile management endpoints (`GET /users/me`, `PUT /users/me`)
- Clarified API design philosophy (auth actions vs. user resources)
- Removed `/auth/me` from documentation (use `/users/me` instead)

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

