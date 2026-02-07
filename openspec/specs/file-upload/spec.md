# File Upload Specification

## Purpose
The system provides secure file upload functionality for user profile photos with MIME type validation, size limits, and path traversal prevention.

## Requirements

### Requirement: Profile Photo Upload
The system SHALL accept profile photo uploads via multipart/form-data with JWT authentication.

#### Scenario: Successful profile photo upload
- GIVEN an authenticated user with a valid JWT token
- WHEN they POST a valid image file to /upload/profile-photo
- THEN the system returns UploadResponse with url, fileName, contentType, and size

#### Scenario: Upload without authentication
- GIVEN an unauthenticated request
- WHEN they attempt to POST to /upload/profile-photo
- THEN the system returns 401 Unauthorized

### Requirement: MIME Type Validation
The system SHALL only accept image files with approved MIME types.

#### Scenario: Upload with allowed MIME type
- GIVEN an authenticated user
- WHEN they upload a file with MIME type image/jpeg, image/png, image/gif, or image/webp
- THEN the system accepts and stores the file

#### Scenario: Upload with disallowed MIME type
- GIVEN an authenticated user
- WHEN they upload a file with MIME type application/pdf or text/html
- THEN the system rejects the upload with 400 Bad Request

### Requirement: File Extension Validation
The system SHALL only accept files with approved extensions.

#### Scenario: Upload with allowed extension
- GIVEN an authenticated user
- WHEN they upload a file with extension jpg, jpeg, png, gif, or webp
- THEN the system accepts and stores the file

#### Scenario: Upload with disallowed extension
- GIVEN an authenticated user
- WHEN they upload a file with extension exe, php, or js
- THEN the system rejects the upload with 400 Bad Request

### Requirement: File Size Limit
The system SHALL enforce a maximum file size of 5 MB for profile photo uploads.

#### Scenario: Upload within size limit
- GIVEN an authenticated user
- WHEN they upload a file of 4 MB
- THEN the system accepts and stores the file

#### Scenario: Upload exceeding size limit
- GIVEN an authenticated user
- WHEN they upload a file of 6 MB
- THEN the system rejects the upload with 413 Payload Too Large

### Requirement: Path Traversal Prevention
The system SHALL prevent path traversal attacks in file uploads and retrieval.

#### Scenario: Upload with path traversal attempt in filename
- GIVEN an authenticated user
- WHEN they upload a file with filename containing ../ or ..\
- THEN the system sanitizes the filename and stores the file safely

#### Scenario: Static file access with path traversal attempt
- GIVEN any client
- WHEN they request GET /uploads/../../../etc/passwd
- THEN the system prevents access and returns 400 Bad Request or 404 Not Found

### Requirement: Static File Serving
The system SHALL serve uploaded files via GET requests to /uploads/:path.

#### Scenario: Access existing uploaded file
- GIVEN a file has been uploaded successfully
- WHEN a client requests GET /uploads/{filename}
- THEN the system serves the file with appropriate Content-Type header

#### Scenario: Access non-existent file
- GIVEN no file exists at the requested path
- WHEN a client requests GET /uploads/nonexistent.jpg
- THEN the system returns 404 Not Found
