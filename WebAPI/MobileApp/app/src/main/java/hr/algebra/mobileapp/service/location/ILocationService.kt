package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.location.CreateLocationRequest
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.models.location.Pin
import hr.algebra.mobileapp.models.location.UpdateLocationRequest

/**
 * Contract for locationService read **and write** operations.
 *
 * Three implementations:
 *  - [LocationServiceApi]        — live BrewMap REST API
 *  - [LocationServiceHardCode]   — in-memory stub, no network
 *  - [LocationServicePersistent] — API-backed with on-device cache
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface ILocationService {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** `GET api/Locations/{id}` */
    suspend fun getById(id: String): ServiceResult<Location>

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
    ): ServiceResult<List<Location>>

    /** `GET api/Locations/pins` */
    suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): ServiceResult<List<Pin>>

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Locations` — creates a new location. Returns the created resource. */
    suspend fun create(request: CreateLocationRequest): ServiceResult<Location>

    /** `PUT api/Locations/{id}` — updates an existing location. Returns the updated resource. */
    suspend fun update(id: String, request: UpdateLocationRequest): ServiceResult<Location>
}
