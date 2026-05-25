package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
import hr.algebra.mobileapp.service.HardCodeData
import kotlin.math.*

/**
 * **Test / offline** location service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains query logic.
 *
 * - [search] applies a Haversine radius check plus in-memory text / tag / rating filters.
 * - [getPins] uses a simple bounding-box test.
 * - `drinkQuery` is ignored (no drink data in the stub).
 */
class LocationServiceHardCode(private val data: HardCodeData) : ILocationService {

    // ── ILocationService ──────────────────────────────────────────────────────

    override suspend fun getById(id: String): Location =
        data.locations.find { it.id == id }
            ?: throw NoSuchElementException("Location not found: $id")

    override suspend fun search(
        longitude: Double,
        latitude: Double,
        query: String?,
        minRating: Double?,
        drinkQuery: String?,
        categoryTags: List<String>?,
        paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): List<Location> = data.locations.filter { loc ->
        haversineMeters(latitude, longitude, loc.latitude, loc.longitude) <= radiusMeters
        && (query == null || loc.name.contains(query, ignoreCase = true)
                          || loc.description?.contains(query, ignoreCase = true) == true)
        && (minRating == null || loc.averageRating >= minRating)
        && (categoryTags.isNullOrEmpty() || loc.categoryTag in categoryTags)
        && (paymentOptionTags.isNullOrEmpty() || loc.paymentOptionTags.containsAll(paymentOptionTags))
        // drinkQuery: no drink data in stub — ignored
    }

    override suspend fun getPins(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): List<Pin> = data.locations
        .filter { it.longitude in minLon..maxLon && it.latitude in minLat..maxLat }
        .map    { Pin(id = it.id, longitude = it.longitude, latitude = it.latitude) }

    // ── Haversine ─────────────────────────────────────────────────────────────

    private fun haversineMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r    = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a    = sin(dLat / 2).pow(2) +
                   cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
