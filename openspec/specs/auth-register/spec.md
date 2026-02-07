# User Registration Specification

## Purpose
The user registration capability allows new users to create an account using email or phone number verification through a one-time password (OTP) flow.

## Requirements

### Requirement: Contact Method Validation
The system SHALL require either email or phone number for registration and SHALL validate the format of provided contact methods.

#### Scenario: Register with valid email
- GIVEN a new user provides a valid email address
- WHEN they submit a registration request with username, displayName, and email
- THEN the system sends an OTP to the email and returns OtpSentResponse with 300 seconds expiry

#### Scenario: Register with valid phone
- GIVEN a new user provides a valid phone number
- WHEN they submit a registration request with username, displayName, and phone
- THEN the system sends an OTP to the phone and returns OtpSentResponse with 300 seconds expiry

#### Scenario: Register with invalid email format
- GIVEN a user provides an email that does not match the pattern `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
- WHEN they submit a registration request
- THEN the system returns ValidationException with error code INVALID_EMAIL

#### Scenario: Register with invalid phone format
- GIVEN a user provides a phone that does not match the pattern `^\+?[1-9]\d{6,14}$`
- WHEN they submit a registration request
- THEN the system returns ValidationException with error code INVALID_PHONE

### Requirement: Uniqueness Enforcement
The system SHALL prevent duplicate usernames, emails, and phone numbers from being registered.

#### Scenario: Register with existing email
- GIVEN an email address that is already registered in the database
- WHEN a user attempts to register with that email
- THEN the system returns ConflictException with error code EMAIL_EXISTS

#### Scenario: Register with existing phone
- GIVEN a phone number that is already registered in the database
- WHEN a user attempts to register with that phone
- THEN the system returns ConflictException with error code PHONE_EXISTS

#### Scenario: Register with existing username
- GIVEN a username that is already taken in the database
- WHEN a user attempts to register with that username
- THEN the system returns ConflictException with error code USERNAME_EXISTS

### Requirement: OTP Generation and Storage
The system SHALL generate a secure 6-digit OTP code using SecureRandom and SHALL store it with a 5-minute expiration period.

#### Scenario: OTP generation for new registration
- GIVEN a valid registration request
- WHEN the system processes the request
- THEN the system generates a 6-digit OTP using SecureRandom, stores it in the otp_codes table with 5-minute expiry, and sends it to the user

#### Scenario: OTP code characteristics
- GIVEN the OTP generation process
- WHEN an OTP is created
- THEN the code is exactly 6 digits in the range 100000-999999

### Requirement: Pending Registration Storage
The system SHALL store pending registration data in Redis with a 5-minute TTL until OTP verification is completed.

#### Scenario: Store pending registration
- GIVEN a valid registration request with password
- WHEN the OTP is sent successfully
- THEN the system stores username, displayName, email, phone, and hashed password in Redis key `pending_registration:{identifier}` with 300 seconds TTL

#### Scenario: Password security in pending storage
- GIVEN a registration request includes a password
- WHEN the pending registration is stored
- THEN the password is hashed using BCrypt before storage in Redis

### Requirement: Rate Limiting
The system SHALL enforce rate limits on OTP requests to prevent abuse.

#### Scenario: First OTP request within window
- GIVEN a user has not requested an OTP in the last hour
- WHEN they submit a registration request
- THEN the system allows the request and increments the rate limit counter

#### Scenario: Exceed OTP rate limit
- GIVEN a user has reached the configured OTP rate limit within the current hour
- WHEN they attempt another registration request
- THEN the system returns RateLimitException with message "Too many OTP requests. Please try again later."

## API Contract

### Endpoint
POST /auth/register

### Request Body
```json
{
  "username": "string (required)",
  "displayName": "string (required)",
  "email": "string (optional)",
  "phone": "string (optional)",
  "password": "string (optional)"
}
```

### Response (200 OK)
```json
{
  "message": "OTP sent successfully",
  "identifier": "string (email or phone)",
  "type": "string (email or phone)",
  "expiresInSeconds": 300
}
```

### Error Codes
- INVALID_EMAIL (400): Email format validation failed
- INVALID_PHONE (400): Phone format validation failed
- EMAIL_EXISTS (409): Email already registered
- PHONE_EXISTS (409): Phone already registered
- USERNAME_EXISTS (409): Username already taken
- REGISTRATION_FAILED (400): Generic registration failure
