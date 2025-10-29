package id.nearyou.app.post

import domain.model.CreatePostRequest
import domain.model.Location
import domain.model.UpdatePostRequest
import id.nearyou.app.exceptions.AuthenticationException
import id.nearyou.app.exceptions.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Configure post-related routes
 */
fun Route.postRoutes() {
    val postService by inject<PostService>()

    route("/posts") {

        /**
         * GET /posts/nearby
         * Get nearby posts within a specified radius
         * Requires JWT authentication
         *
         * Query parameters:
         * - latitude: User's latitude (required)
         * - longitude: User's longitude (required)
         * - radius: Radius in meters (optional, default: 1000)
         * - limit: Maximum number of posts (optional, default: 50)
         */
        authenticate("auth-jwt") {
            get("/nearby") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")

                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")

                // Parse query parameters
                val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
                    ?: throw ValidationException("Latitude is required", "MISSING_LATITUDE")

                val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
                    ?: throw ValidationException("Longitude is required", "MISSING_LONGITUDE")

                val radius = call.request.queryParameters["radius"]?.toDoubleOrNull() ?: 1000.0
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

                // Validate location
                val userLocation = try {
                    Location(latitude, longitude)
                } catch (e: IllegalArgumentException) {
                    throw ValidationException(e.message ?: "Invalid location", "INVALID_LOCATION")
                }

                // Get nearby posts
                val posts = postService.getNearbyPosts(
                    userLocation = userLocation,
                    radiusMeters = radius,
                    limit = limit,
                    currentUserId = userId
                )

                call.respond(HttpStatusCode.OK, mapOf(
                    "posts" to posts,
                    "count" to posts.size,
                    "radius" to radius,
                    "location" to userLocation
                ))
            }

            /**
             * POST /posts
             * Create a new post
             * Requires JWT authentication
             */
            post {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")

                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")

                // Parse request body
                val request = call.receive<CreatePostRequest>()

                // Create post
                val post = postService.createPost(userId, request)

                call.respond(HttpStatusCode.Created, post)
            }

            /**
             * GET /posts/:id
             * Get post by ID
             * Requires JWT authentication
             */
            get("/{id}") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")

                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")

                // Get post ID from path parameter
                val postId = call.parameters["id"]
                    ?: throw ValidationException("Post ID is required", "MISSING_POST_ID")

                // Get post
                val post = postService.getPostById(postId, userId)

                call.respond(HttpStatusCode.OK, post)
            }

            /**
             * PUT /posts/:id
             * Update post content
             * Requires JWT authentication
             * Only the post owner can update
             */
            put("/{id}") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")

                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")

                // Get post ID from path parameter
                val postId = call.parameters["id"]
                    ?: throw ValidationException("Post ID is required", "MISSING_POST_ID")

                // Parse request body
                val request = call.receive<UpdatePostRequest>()

                // Update post
                val post = postService.updatePost(postId, userId, request)

                call.respond(HttpStatusCode.OK, post)
            }

            /**
             * DELETE /posts/:id
             * Delete a post (soft delete)
             * Requires JWT authentication
             * Only the post owner can delete
             */
            delete("/{id}") {
                // Extract user ID from JWT token
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("Invalid token", "INVALID_TOKEN")

                val userId = principal.payload.subject
                    ?: throw AuthenticationException("Invalid token subject", "INVALID_TOKEN")

                // Get post ID from path parameter
                val postId = call.parameters["id"]
                    ?: throw ValidationException("Post ID is required", "MISSING_POST_ID")

                // Delete post
                postService.deletePost(postId, userId)

                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Post deleted successfully",
                    "postId" to postId
                ))
            }
        }
    }
}

