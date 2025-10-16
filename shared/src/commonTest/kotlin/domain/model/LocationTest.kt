package domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LocationTest {

    @Test
    fun `location creation with valid coordinates`() {
        val location = Location(latitude = -6.2088, longitude = 106.8456)
        assertEquals(-6.2088, location.latitude)
        assertEquals(106.8456, location.longitude)
    }

    @Test
    fun `location creation fails with invalid latitude`() {
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = 91.0, longitude = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = -91.0, longitude = 0.0)
        }
    }

    @Test
    fun `location creation fails with invalid longitude`() {
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = 0.0, longitude = 181.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Location(latitude = 0.0, longitude = -181.0)
        }
    }

    @Test
    fun `distance calculation between same location is zero`() {
        val location = Location(latitude = 0.0, longitude = 0.0)
        val distance = location.distanceTo(location)
        assertTrue(distance < 1.0) // Should be very close to 0
    }

    @Test
    fun `distance calculation between different locations`() {
        val jakarta = Location(latitude = -6.2088, longitude = 106.8456)
        val bandung = Location(latitude = -6.9175, longitude = 107.6191)
        
        val distance = jakarta.distanceTo(bandung)
        
        // Distance between Jakarta and Bandung is approximately 120 km
        assertTrue(distance > 100_000) // > 100 km
        assertTrue(distance < 150_000) // < 150 km
    }

    @Test
    fun `isWithinRadius returns true for nearby locations`() {
        val location1 = Location(latitude = 0.0, longitude = 0.0)
        val location2 = Location(latitude = 0.001, longitude = 0.001)
        
        // ~157 meters apart
        assertTrue(location1.isWithinRadius(location2, 200.0))
    }

    @Test
    fun `isWithinRadius returns false for distant locations`() {
        val location1 = Location(latitude = 0.0, longitude = 0.0)
        val location2 = Location(latitude = 1.0, longitude = 1.0)
        
        // ~157 km apart
        assertFalse(location1.isWithinRadius(location2, 1000.0))
    }

    @Test
    fun `formatDistance shows meters for short distances`() {
        val formatted = formatDistance(500.0)
        assertEquals("500 m", formatted)
    }

    @Test
    fun `formatDistance shows kilometers with decimal for medium distances`() {
        val formatted = formatDistance(1500.0)
        assertEquals("1.5 km", formatted)
    }

    @Test
    fun `formatDistance shows kilometers for long distances`() {
        val formatted = formatDistance(15000.0)
        assertEquals("15 km", formatted)
    }
}

