package domain.model

import kotlinx.serialization.Serializable
import kotlin.math.*

/**
 * Geographic location model
 *
 * @property latitude Latitude in degrees (-90 to 90)
 * @property longitude Longitude in degrees (-180 to 180)
 */
@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90 degrees, got $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180 degrees, got $longitude"
        }
    }

    /**
     * Calculate distance to another location using Haversine formula
     *
     * @param other The other location
     * @return Distance in meters
     */
    fun distanceTo(other: Location): Double {
        val earthRadiusMeters = 6371000.0 // Earth's radius in meters

        val lat1Rad = latitude * PI / 180.0
        val lat2Rad = other.latitude * PI / 180.0
        val deltaLatRad = (other.latitude - latitude) * PI / 180.0
        val deltaLonRad = (other.longitude - longitude) * PI / 180.0

        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLonRad / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }

    /**
     * Check if this location is within a certain radius of another location
     *
     * @param other The other location
     * @param radiusMeters The radius in meters
     * @return True if within radius, false otherwise
     */
    fun isWithinRadius(other: Location, radiusMeters: Double): Boolean {
        return distanceTo(other) <= radiusMeters
    }

    companion object {
        /**
         * Distance levels for nearby feed (in meters)
         */
        val DISTANCE_LEVEL_1 = 1000.0      // 1 km
        val DISTANCE_LEVEL_2 = 5000.0      // 5 km
        val DISTANCE_LEVEL_3 = 10000.0     // 10 km
        val DISTANCE_LEVEL_4 = 20000.0     // 20 km

        /**
         * Granularity for distance display (in meters)
         */
        val DISTANCE_GRANULARITY = 100.0   // 0.1 km
    }
}

/**
 * Format distance for display
 *
 * @param distanceMeters Distance in meters
 * @return Formatted distance string (e.g., "1.2 km", "500 m")
 */
fun formatDistance(distanceMeters: Double): String {
    return when {
        distanceMeters < 1000 -> "${distanceMeters.toInt()} m"
        distanceMeters < 10000 -> {
            val km = (distanceMeters / 100).roundToInt() / 10.0
            "$km km"
        }
        else -> "${(distanceMeters / 1000).roundToInt()} km"
    }
}

