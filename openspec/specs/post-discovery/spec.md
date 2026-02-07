# Post Discovery Specification

## Purpose
This specification defines the nearby post discovery capability, allowing users to find posts within a specified geographic radius using validated coordinates and distance calculations.

## Requirements

### Requirement: Discover Nearby Posts
The system SHALL allow authenticated users to discover posts within a specified geographic radius.

#### Scenario: User discovers nearby posts with default parameters
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with valid latitude and longitude
- THEN the system returns a 200 status code
- AND returns a response containing posts array, count, radius (1000m), and location

#### Scenario: User discovers nearby posts with custom radius
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with valid latitude, longitude, and radius of 5000 meters
- THEN the system returns a 200 status code
- AND returns posts within 5000 meters of the specified location
- AND returns the response with radius set to 5000

#### Scenario: User discovers nearby posts with custom limit
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with valid latitude, longitude, and limit of 20
- THEN the system returns a 200 status code
- AND returns at most 20 posts in the response

#### Scenario: Unauthenticated user attempts to discover posts
- GIVEN a user is not authenticated
- WHEN the user sends a GET request to /posts/nearby
- THEN the system returns a 401 Unauthorized status code

### Requirement: Validate Geographic Coordinates
The system SHALL validate that latitude is between -90 and 90 degrees and longitude is between -180 and 180 degrees.

#### Scenario: User provides valid coordinates
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with latitude 37.7749 and longitude -122.4194
- THEN the system accepts the coordinates and returns nearby posts

#### Scenario: User provides invalid latitude (too low)
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with latitude -91
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating latitude must be between -90 and 90

#### Scenario: User provides invalid latitude (too high)
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with latitude 91
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating latitude must be between -90 and 90

#### Scenario: User provides invalid longitude (too low)
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with longitude -181
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating longitude must be between -180 and 180

#### Scenario: User provides invalid longitude (too high)
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with longitude 181
- THEN the system returns a 400 Bad Request status code
- AND provides an error message indicating longitude must be between -180 and 180

### Requirement: Calculate Distance Using Haversine Formula
The system SHALL calculate distances between geographic coordinates using the Haversine formula.

#### Scenario: System calculates distance accurately
- GIVEN a user is authenticated with a valid JWT token
- AND posts exist at various distances from the query location
- WHEN the user sends a GET request to /posts/nearby with a specified radius
- THEN the system uses the Haversine formula to calculate distances
- AND returns only posts within the specified radius

### Requirement: Support Standard Distance Levels
The system SHALL support distance levels of 1km, 5km, 10km, and 20km.

#### Scenario: User queries posts at 1km distance level
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with radius 1000 meters
- THEN the system returns posts within 1 kilometer

#### Scenario: User queries posts at 5km distance level
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with radius 5000 meters
- THEN the system returns posts within 5 kilometers

#### Scenario: User queries posts at 10km distance level
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with radius 10000 meters
- THEN the system returns posts within 10 kilometers

#### Scenario: User queries posts at 20km distance level
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with radius 20000 meters
- THEN the system returns posts within 20 kilometers

### Requirement: Apply Distance Granularity
The system SHALL use a distance granularity of 100 meters for distance calculations.

#### Scenario: System applies 100m granularity to distance calculations
- GIVEN a user is authenticated with a valid JWT token
- AND posts exist at various precise distances from the query location
- WHEN the user sends a GET request to /posts/nearby
- THEN the system calculates distances with 100 meter granularity
- AND includes posts within the specified radius based on this granularity

### Requirement: Default Query Parameters
The system SHALL use default values of 1000 meters for radius and 50 for limit when not specified.

#### Scenario: User queries without specifying radius
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with latitude and longitude but no radius parameter
- THEN the system uses 1000 meters as the default radius

#### Scenario: User queries without specifying limit
- GIVEN a user is authenticated with a valid JWT token
- WHEN the user sends a GET request to /posts/nearby with latitude and longitude but no limit parameter
- THEN the system returns at most 50 posts by default
