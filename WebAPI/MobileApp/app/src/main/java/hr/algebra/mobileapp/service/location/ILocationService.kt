package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.models.CreateLocationRequest
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
import hr.algebra.mobileapp.models.UpdateLocationRequest

/**
 * Contract for location read **and write** operations.
 *
 * Three implementations:
 *  - [LocationServiceApi]        — live BrewMap REST API
 *  - [LocationServiceHardCode]   — in-memory stub, no network
 *  - [LocationServicePersistent] — API-backed with on-device cache
 */
interface ILocationService {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** `GET api/Locations/{id}` */
    suspend fun getById(id: String): Location

    /** `GET api/Locations/search` */
    suspend fun search(
        longitude: Double,
        latitude: Double,
        query: String? = null,
        minRating: Double? = null,
        drinkQuery: String? = null,
        categoryTags: List<String>? = null,
        paymentOptionTags: List<String>? = null,
        radiusMeters: Double = 5_000.0
    ): List<Location>

    /** `GET api/Locations/pins` */
    suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): List<Pin>

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Locations` — creates a new location. Returns the created resource. */
    suspend fun create(request: CreateLocationRequest): Location

    /** `PUT api/Locations/{id}` — updates an existing location. Returns the updated resource. */
    suspend fun update(id: String, request: UpdateLocationRequest): Location
}
