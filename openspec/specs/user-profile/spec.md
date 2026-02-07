# User Profile Management Specification

## Purpose
This specification defines the user profile management capabilities, including retrieving and updating user information with proper authentication, validation, and security controls.

## Requirements

### Requirement: Retrieve Current User Profile
The system SHALL allow authenticated users to retrieve their own profile information.

#### Scenario: User retrieves their profile successfully
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /users/me
- THEN the system returns a 200 status code
- AND the response contains User object with id, username, displayName, email, phone, bio, profilePhotoUrl, isVerified, subscriptionTier, createdAt, and updatedAt fields

#### Scenario: Unauthenticated user attempts to retrieve profile
- GIVEN a user is not authenticated
- WHEN the user sends a GET request to /users/me without a JWT token
- THEN the system returns a 401 Unauthorized status code

### Requirement: Update User Profile
The system SHALL allow authenticated users to update their profile information.

#### Scenario: User updates profile successfully
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with UpdateUserRequest containing displayName, bio, and profilePhotoUrl
- THEN the system updates the user's profile
- AND returns a 200 status code with the updated User object

#### Scenario: User updates profile with partial data
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with only displayName in UpdateUserRequest
- THEN the system updates only the displayName field
- AND returns a 200 status code with the updated User object

#### Scenario: Unauthenticated user attempts to update profile
- GIVEN a user is not authenticated
- WHEN the user sends a PUT request to /users/me
- THEN the system returns a 401 Unauthorized status code

### Requirement: Validate Profile Photo URL
The system SHALL validate that profilePhotoUrl is a valid URL when provided.

#### Scenario: User provides valid profile photo URL
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with a valid HTTPS URL in profilePhotoUrl
- THEN the system accepts the URL and updates the profile

#### Scenario: User provides invalid profile photo URL
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with an invalid URL in profilePhotoUrl
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating the URL is invalid

### Requirement: Sanitize User Input
The system SHALL sanitize bio and displayName fields to prevent XSS attacks.

#### Scenario: User submits bio with HTML tags
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with bio containing HTML tags
- THEN the system sanitizes the bio by removing or escaping HTML tags
- AND returns the updated User object with sanitized bio

#### Scenario: User submits displayName with script tags
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a PUT request to /users/me with displayName containing script tags
- THEN the system sanitizes the displayName by removing or escaping script tags
- AND returns the updated User object with sanitized displayName

### Requirement: Enforce Contact Information Invariant
The system SHALL ensure that every user has either an email address or a phone number.

#### Scenario: User has email address
- GIVEN a user is authenticated with a valid JWT token
- AND the user has an email address but no phone number
- WHEN the user sends a GET request to /users/me
- THEN the system returns the User object with email field populated
- AND the phone field is null

#### Scenario: User has phone number
- GIVEN a user is authenticated with a valid JWT token
- AND the user has a phone number but no email address
- WHEN the user sends a GET request to /users/me
- THEN the system returns the User object with phone field populated
- AND the email field is null

### Requirement: Support Subscription Tiers
The system SHALL maintain subscription tier information for each user.

#### Scenario: Free tier user retrieves profile
- GIVEN a user is authenticated with a valid JWT token
- AND the user has a FREE subscription tier
- WHEN the user sends a GET request to /users/me
- THEN the system returns the User object with subscriptionTier set to FREE

#### Scenario: Premium tier user retrieves profile
- GIVEN a user is authenticated with a valid JWT token
- AND the user has a PREMIUM subscription tier
- WHEN the user sends a GET request to /users/me
- THEN the system returns the User object with subscriptionTier set to PREMIUM
