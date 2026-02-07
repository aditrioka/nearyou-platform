# User Login Specification

## Purpose
The user login capability allows existing verified users to authenticate using email or phone number through a one-time password (OTP) flow, with additional support for OAuth providers.

## Requirements

### Requirement: Contact Method Validation
The system SHALL require either email or phone number for login and SHALL validate the format of provided contact methods.

#### Scenario: Login with valid email
- GIVEN an existing verified user's email address
- WHEN they submit a login request with the email
- THEN the system sends an OTP to the email and returns OtpSentResponse with 300 seconds expiry

#### Scenario: Login with valid phone
- GIVEN an existing verified user's phone number
- WHEN they submit a login request with the phone
- THEN the system sends an OTP to the phone and returns OtpSentResponse with 300 seconds expiry

#### Scenario: Login with invalid email format
- GIVEN a user provides an email that does not match the pattern `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
- WHEN they submit a login request
- THEN the system returns ValidationException with error code INVALID_EMAIL

#### Scenario: Login with unregistered email
- GIVEN an email address that does not exist in the database
- WHEN a user attempts to login with that email
- THEN the system returns NotFoundException with error code EMAIL_NOT_FOUND

### Requirement: Pending Verification Detection
The system SHALL detect users who have registered but not completed OTP verification and SHALL return an appropriate error.

#### Scenario: Login during pending registration
- GIVEN a user has submitted registration but not verified their OTP
- WHEN they attempt to login with the same identifier
- THEN the system returns AuthenticationException with error code VERIFICATION_PENDING and message "Please verify your email/phone first. Check your inbox for the OTP code."

### Requirement: Rate Limiting
The system SHALL enforce separate rate limits on login attempts to prevent brute force attacks.

#### Scenario: First login attempt within window
- GIVEN a user has not attempted login in the last 15 minutes
- WHEN they submit a login request
- THEN the system allows the request and increments the login rate limit counter

#### Scenario: Exceed login rate limit
- GIVEN a user has made 10 login attempts within the last 15 minutes
- WHEN they attempt another login request
- THEN the system returns RateLimitException with message "Too many login attempts. Please try again later."

### Requirement: OTP Verification
The system SHALL verify the OTP code and complete authentication by issuing JWT tokens.

#### Scenario: Verify valid OTP for login
- GIVEN a user has received an OTP for login
- WHEN they submit VerifyOtpRequest with correct identifier, code, and type before expiration
- THEN the system marks OTP as used, generates access and refresh tokens, and returns AuthResponse with tokens and user details

#### Scenario: Verify expired OTP
- GIVEN a user has an OTP that has exceeded its 5-minute expiration
- WHEN they attempt to verify the OTP
- THEN the system returns AuthenticationException with error code INVALID_OTP and message "Invalid or expired OTP"

#### Scenario: Verify already used OTP
- GIVEN a user has previously verified an OTP successfully
- WHEN they attempt to verify the same OTP again
- THEN the system returns AuthenticationException with error code INVALID_OTP

### Requirement: Google OAuth Login
The system SHALL accept Google OAuth login requests but SHALL return NOT_IMPLEMENTED status until the feature is completed.

#### Scenario: Attempt Google OAuth login
- GIVEN a user provides a valid Google ID token
- WHEN they submit a POST request to /auth/login/google with GoogleLoginRequest
- THEN the system returns ServiceUnavailableException with error code NOT_IMPLEMENTED and status 503

## API Contracts

### Endpoint: POST /auth/login

#### Request Body
```json
{
  "email": "string (optional)",
  "phone": "string (optional)"
}
```

#### Response (200 OK)
```json
{
  "message": "OTP sent successfully",
  "identifier": "string (email or phone)",
  "type": "string (email or phone)",
  "expiresInSeconds": 300
}
```

#### Error Codes
- INVALID_EMAIL (400): Email format validation failed
- INVALID_PHONE (400): Phone format validation failed
- EMAIL_NOT_FOUND (404): Email not registered
- PHONE_NOT_FOUND (404): Phone not registered
- VERIFICATION_PENDING (401): User registered but not verified
- LOGIN_FAILED (401): Generic login failure

### Endpoint: POST /auth/verify-otp

#### Request Body
```json
{
  "identifier": "string (email or phone)",
  "code": "string (6 digits)",
  "type": "string (email or phone)"
}
```

#### Response (200 OK)
```json
{
  "accessToken": "string (JWT)",
  "refreshToken": "string (JWT)",
  "user": {
    "id": "string (UUID)",
    "username": "string",
    "displayName": "string",
    "email": "string (optional)",
    "phone": "string (optional)",
    "isVerified": true,
    "subscriptionTier": "string"
  }
}
```

#### Error Codes
- INVALID_OTP (401): Invalid or expired OTP code
- VERIFICATION_FAILED (401): OTP verification process failed
- USER_NOT_FOUND (404): User not found after OTP verification

### Endpoint: POST /auth/login/google

#### Request Body
```json
{
  "idToken": "string (Google ID token)"
}
```

#### Response (503 Service Unavailable)
```json
{
  "errorCode": "NOT_IMPLEMENTED",
  "message": "Google OAuth login not yet implemented"
}
```
