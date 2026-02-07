# Post Management Specification

## Purpose
This specification defines the post CRUD operations, including creation, retrieval, updating, and deletion with proper authentication, authorization, validation, and security controls.

## Requirements

### Requirement: Create Post
The system SHALL allow authenticated users to create new posts with content, location, and optional media URLs.

#### Scenario: User creates post successfully
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with CreatePostRequest containing valid content, location, and mediaUrls
- THEN the system creates the post
- AND returns a 201 Created status code
- AND returns the created Post object

#### Scenario: User creates post without media URLs
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with CreatePostRequest containing valid content and location but no mediaUrls
- THEN the system creates the post without media
- AND returns a 201 Created status code

#### Scenario: Unauthenticated user attempts to create post
- GIVEN a user is not authenticated
- WHEN the user sends a POST request to /posts
- THEN the system returns a 401 Unauthorized status code

### Requirement: Retrieve Post
The system SHALL allow authenticated users to retrieve individual posts by ID.

#### Scenario: User retrieves existing post
- GIVEN a user is authenticated with a valid JWT token
- AND a post exists with the specified ID
- WHEN the user sends a GET request to /posts/{id}
- THEN the system returns a 200 status code
- AND returns the Post object

#### Scenario: User attempts to retrieve non-existent post
- GIVEN a user is authenticated with a valid JWT token
- AND no post exists with the specified ID
- WHEN the user sends a GET request to /posts/{id}
- THEN the system returns a 404 Not Found status code

#### Scenario: Unauthenticated user attempts to retrieve post
- GIVEN a user is not authenticated
- WHEN the user sends a GET request to /posts/{id}
- THEN the system returns a 401 Unauthorized status code

### Requirement: Update Post
The system SHALL allow authenticated users to update their own posts with new content.

#### Scenario: Post owner updates post successfully
- GIVEN a user is authenticated with a valid JWT token
- AND the user owns the post with the specified ID
- WHEN the user sends a PUT request to /posts/{id} with UpdatePostRequest containing valid content
- THEN the system updates the post content
- AND returns a 200 status code with the updated Post object

#### Scenario: Non-owner attempts to update post
- GIVEN a user is authenticated with a valid JWT token
- AND the user does not own the post with the specified ID
- WHEN the user sends a PUT request to /posts/{id}
- THEN the system returns a 403 Forbidden status code

#### Scenario: Unauthenticated user attempts to update post
- GIVEN a user is not authenticated
- WHEN the user sends a PUT request to /posts/{id}
- THEN the system returns a 401 Unauthorized status code

### Requirement: Delete Post
The system SHALL allow authenticated users to soft delete their own posts.

#### Scenario: Post owner deletes post successfully
- GIVEN a user is authenticated with a valid JWT token
- AND the user owns the post with the specified ID
- WHEN the user sends a DELETE request to /posts/{id}
- THEN the system soft deletes the post
- AND returns a 200 status code with a confirmation message

#### Scenario: Non-owner attempts to delete post
- GIVEN a user is authenticated with a valid JWT token
- AND the user does not own the post with the specified ID
- WHEN the user sends a DELETE request to /posts/{id}
- THEN the system returns a 403 Forbidden status code

#### Scenario: Unauthenticated user attempts to delete post
- GIVEN a user is not authenticated
- WHEN the user sends a DELETE request to /posts/{id}
- THEN the system returns a 401 Unauthorized status code

### Requirement: Validate Post Content Length
The system SHALL enforce that post content is between 1 and 500 characters.

#### Scenario: User creates post with valid content length
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with content of 250 characters
- THEN the system accepts the content and creates the post

#### Scenario: User creates post with content too short
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with empty content
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating content must be at least 1 character

#### Scenario: User creates post with content too long
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with content exceeding 500 characters
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating content must not exceed 500 characters

### Requirement: Validate Media URLs
The system SHALL validate that media URLs are valid URLs when provided.

#### Scenario: User creates post with valid media URLs
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with valid HTTPS URLs in mediaUrls
- THEN the system accepts the URLs and creates the post

#### Scenario: User creates post with invalid media URL
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with an invalid URL in mediaUrls
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating the URL is invalid

### Requirement: Sanitize Post Content
The system SHALL sanitize post content to prevent XSS attacks.

#### Scenario: User creates post with HTML tags in content
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a POST request to /posts with content containing HTML tags
- THEN the system sanitizes the content by removing or escaping HTML tags
- AND returns the created Post object with sanitized content

#### Scenario: User updates post with script tags in content
- GIVEN a user is authenticated with a valid JWT token
- AND the user owns the post
- WHEN the user sends a PUT request to /posts/{id} with content containing script tags
- THEN the system sanitizes the content by removing or escaping script tags
- AND returns the updated Post object with sanitized content
