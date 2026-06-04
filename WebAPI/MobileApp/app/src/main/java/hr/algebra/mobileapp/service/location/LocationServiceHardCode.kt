package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.*
import hr.algebra.mobileapp.models.location.CreateLocationRequest
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.models.location.Pin
import hr.algebra.mobileapp.models.location.UpdateLocationRequest
import hr.algebra.mobileapp.service.HardCodeData
import java.time.Instant
import kotlin.math.*

/**
 * **Test / offline** locationService service — no network required.
 *
 * Write operations mutate [HardCodeData.locations] in memory.
 */
class LocationServiceHardCode(private val data: HardCodeData) : ILocationService {

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Location> {
        val location = data.locations.find { it.id == id }
            ?: return ServiceResult.failure("Location not found: $id")
        return ServiceResult.success(location)
    }

    override suspend fun search(
        longitude: Double, latitude: Double,
        query: String?, minRating: Double?, drinkQuery: String?,
        categoryTags: List<String>?, paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): ServiceResult<List<Location>> {
        val results = data.locations.filter { loc ->
            haversineMeters(latitude, longitude, loc.latitude, loc.longitude) <= radiusMeters
            && (query == null || loc.name.contains(query, ignoreCase = true)
                              || loc.description?.contains(query, ignoreCase = true) == true)
            && (minRating == null || loc.averageRating >= minRating)
            && (categoryTags.isNullOrEmpty() || loc.categoryTag in categoryTags)
            && (paymentOptionTags.isNullOrEmpty() || loc.paymentOptionTags.containsAll(paymentOptionTags))
        }
        return ServiceResult.success(results)
    }

    override suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): ServiceResult<List<Pin>> {
        val pins = data.locations
            .filter { it.longitude in minLon..maxLon && it.latitude in minLat..maxLat }
            .map    { Pin(id = it.id, longitude = it.longitude, latitude = it.latitude) }
        return ServiceResult.success(pins)
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun create(request: CreateLocationRequest): ServiceResult<Location> {
        val now = Instant.now().toString()
        val location = Location(
            id = "hc-loc-${System.currentTimeMillis()}",
            name = request.name,
            description = request.description,
            address = request.address,
            longitude = request.longitude,
            latitude = request.latitude,
            categoryTag = request.categoryTag,
            paymentOptionTags = request.paymentOptionTags,
            openingHours = request.openingHours,
            contact = request.contact,
            isActive = true,
            averageRating = 0.0,
            totalReviews = 0,
            createdAt = now
        )
        data.locations.add(location)
        return ServiceResult.success(location)
    }

    override suspend fun update(id: String, request: UpdateLocationRequest): ServiceResult<Location> {
        val index = data.locations.indexOfFirst { it.id == id }
        if (index == -1) return ServiceResult.failure("Location not found: $id")
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
        return ServiceResult.success(updated)
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
