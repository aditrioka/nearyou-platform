# Session Management Specification

## Purpose
The session management capability allows users to maintain authenticated sessions through JWT access tokens, refresh expired tokens, and securely logout by revoking refresh tokens.

## Requirements

### Requirement: JWT Access Token Authentication
The system SHALL authenticate protected endpoints using JWT access tokens via the "auth-jwt" authentication scheme with Bearer token format.

#### Scenario: Access protected endpoint with valid token
- GIVEN a user has a valid JWT access token
- WHEN they make a request to a protected endpoint with Authorization: Bearer {token}
- THEN the system validates the token signature and expiration, extracts user claims, and allows the request

#### Scenario: Access protected endpoint without token
- GIVEN a user makes a request to a protected endpoint
- WHEN they do not provide an Authorization header
- THEN the system returns AuthenticationException with error code INVALID_TOKEN

#### Scenario: Access protected endpoint with expired token
- GIVEN a user has a JWT access token that has exceeded its expiration time
- WHEN they make a request to a protected endpoint
- THEN the system rejects the token and returns authentication failure

### Requirement: Token Refresh
The system SHALL allow users to obtain new access and refresh tokens using a valid non-revoked refresh token.

#### Scenario: Refresh token with valid refresh token
- GIVEN a user has a valid refresh token that is not revoked
- WHEN they submit RefreshTokenRequest with the refresh token
- THEN the system validates the token, generates new access and refresh tokens, revokes the old refresh token, stores the new refresh token, and returns TokenResponse

#### Scenario: Refresh token with revoked token
- GIVEN a user has a refresh token that has been revoked
- WHEN they attempt to refresh using that token
- THEN the system returns AuthenticationException with error code REFRESH_FAILED and message "Refresh token has been revoked"

#### Scenario: Refresh token with invalid token
- GIVEN a user provides a malformed or invalid refresh token
- WHEN they submit RefreshTokenRequest
- THEN the system returns AuthenticationException with error code REFRESH_FAILED and message "Invalid refresh token"

#### Scenario: Token rotation on refresh
- GIVEN a successful token refresh operation
- WHEN new tokens are issued
- THEN the old refresh token is marked as revoked with revokedAt timestamp, and the new refresh token is stored with 30-day expiration

### Requirement: Secure Logout
The system SHALL revoke all active refresh tokens for a user when they logout, preventing further token refresh operations.

#### Scenario: Logout with valid session
- GIVEN a user is authenticated with a valid JWT access token
- WHEN they POST to /auth/logout with Bearer token authentication
- THEN the system extracts the user ID from the JWT, revokes all non-revoked refresh tokens for that user, and returns success message

#### Scenario: Logout without authentication
- GIVEN a user attempts to logout
- WHEN they do not provide a valid JWT access token
- THEN the system returns AuthenticationException with error code INVALID_TOKEN

#### Scenario: All tokens revoked on logout
- GIVEN a user has multiple active refresh tokens from different devices
- WHEN they logout from one device
- THEN all refresh tokens for that user are marked as revoked, forcing re-authentication on all devices

### Requirement: Refresh Token Storage and Validation
The system SHALL store refresh tokens in the database with expiration and revocation tracking.

#### Scenario: Store new refresh token
- GIVEN a refresh token is generated during login or token refresh
- WHEN the token is stored
- THEN the system inserts a record in refresh_tokens table with token value, user ID, 30-day expiration, creation timestamp, and isRevoked=false

#### Scenario: Check token revocation status
- GIVEN a refresh token is provided for validation
- WHEN the system checks if it is revoked
- THEN the system queries the refresh_tokens table for matching token with isRevoked=true

## API Contracts

### Endpoint: POST /auth/refresh

#### Authentication
Public endpoint (no JWT required)

#### Request Body
```json
{
  "refreshToken": "string (JWT refresh token)"
}
```

#### Response (200 OK)
```json
{
  "accessToken": "string (JWT)",
  "refreshToken": "string (JWT)"
}
```

#### Error Codes
- REFRESH_FAILED (401): Invalid or revoked refresh token

### Endpoint: POST /auth/logout

#### Authentication
Requires JWT access token via "auth-jwt" scheme

#### Request Headers
```
Authorization: Bearer {access_token}
```

#### Request Body
Empty body

#### Response (200 OK)
```json
{
  "message": "Logged out successfully"
}
```

#### Error Codes
- INVALID_TOKEN (401): Missing, malformed, or expired JWT access token
- LOGOUT_FAILED (401): Logout process failed

## JWT Token Details

### Access Token
- Type: JWT
- Expiration: Short-lived (implementation-specific)
- Claims: User ID (subject), subscription tier
- Usage: Bearer token in Authorization header for protected endpoints

### Refresh Token
- Type: JWT
- Expiration: 30 days
- Storage: Persisted in refresh_tokens table
- Revocation: Can be revoked individually or all tokens for a user
- Usage: One-time use with automatic rotation on refresh
