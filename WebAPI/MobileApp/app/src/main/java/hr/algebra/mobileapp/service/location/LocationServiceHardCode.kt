package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.models.*
import hr.algebra.mobileapp.service.HardCodeData
import java.time.Instant
import kotlin.math.*

/**
 * **Test / offline** location service — no network required.
 *
 * Write operations mutate [HardCodeData.locations] in memory.
 */
class LocationServiceHardCode(private val data: HardCodeData) : ILocationService {

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Location =
        data.locations.find { it.id == id }
            ?: throw NoSuchElementException("Location not found: $id")

    override suspend fun search(
        longitude: Double, latitude: Double,
        query: String?, minRating: Double?, drinkQuery: String?,
        categoryTags: List<String>?, paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): List<Location> = data.locations.filter { loc ->
        haversineMeters(latitude, longitude, loc.latitude, loc.longitude) <= radiusMeters
        && (query == null || loc.name.contains(query, ignoreCase = true)
                          || loc.description?.contains(query, ignoreCase = true) == true)
        && (minRating == null || loc.averageRating >= minRating)
        && (categoryTags.isNullOrEmpty() || loc.categoryTag in categoryTags)
        && (paymentOptionTags.isNullOrEmpty() || loc.paymentOptionTags.containsAll(paymentOptionTags))
    }

    override suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): List<Pin> = data.locations
        .filter { it.longitude in minLon..maxLon && it.latitude in minLat..maxLat }
        .map    { Pin(id = it.id, longitude = it.longitude, latitude = it.latitude) }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun create(request: CreateLocationRequest): Location {
        val now = Instant.now().toString()
        val location = Location(
            id                = "hc-loc-${System.currentTimeMillis()}",
            name              = request.name,
            description       = request.description,
            address           = request.address,
            longitude         = request.longitude,
            latitude          = request.latitude,
            categoryTag       = request.categoryTag,
            paymentOptionTags = request.paymentOptionTags,
            openingHours      = request.openingHours,
            contact           = request.contact,
            isActive          = true,
            averageRating     = 0.0,
            totalReviews      = 0,
            createdAt         = now
        )
        data.locations.add(location)
        return location
    }

    override suspend fun update(id: String, request: UpdateLocationRequest): Location {
        val index = data.locations.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Location not found: $id")
        val updated = data.locations[index].copy(
            name              = request.name,
            description       = request.description,
            address           = request.address,
            categoryTag       = request.categoryTag,
            paymentOptionTags = request.paymentOptionTags,
            openingHours      = request.openingHours,
            contact           = request.contact
        )
        data.locations[index] = updated
        return updated
    }

    // ── Haversine ─────────────────────────────────────────────────────────────

    private fun haversineMeters(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val r    = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a    = sin(dLat / 2).pow(2) +
                   cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
