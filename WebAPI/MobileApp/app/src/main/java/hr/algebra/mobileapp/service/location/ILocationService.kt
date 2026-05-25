package hr.algebra.mobileapp.service.location

import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin

/**
 * Contract for the three read-only location ("product") endpoints exposed by
 * `LocationsController` in the BrewMap API.
 *
 * Two implementations are available:
 *  - [LocationServiceApi]      — real HTTP calls to the BrewMap backend
 *  - [LocationServiceHardCode] — in-memory stub with seeded Zagreb venues, no network needed
 *
 * Switch between them in [ProductServiceProvider].
 */
interface ILocationService {

    /**
     * Fetch a single location by its ID.
     *
     * `GET api/Locations/{id}`
     *
     * @param id MongoDB ObjectId string of the location.
     * @return the matching [Location].
     * @throws NoSuchElementException if no location with that id exists.
     */
    suspend fun getById(id: String): Location

    /**
     * Search locations near a coordinate, with optional text and filter params.
     *
     * `GET api/Locations/search`
     *
     * @param query          free-text match against name / description (optional)
     * @param minRating      minimum average rating, inclusive (optional)
     * @param drinkQuery     free-text match against drinks at the location (optional)
     * @param categoryTags   keep only locations whose `categoryTag` is in this list (optional)
     * @param paymentOptionTags keep only locations that accept **all** of these payment methods (optional)
     * @param longitude      centre of the search radius (required)
     * @param latitude       centre of the search radius (required)
     * @param radiusMeters   search radius in metres; defaults to 5 000 m
     * @return list of matching [Location]s, may be empty.
     */
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

    /**
     * Fetch lightweight map pins for all active locations within a bounding box.
     *
     * `GET api/Locations/pins`
     *
     * @param minLon western edge of the bounding box
     * @param maxLon eastern edge
     * @param minLat southern edge
     * @param maxLat northern edge
     * @return list of [Pin]s whose coordinates fall inside the box, may be empty.
     */
    suspend fun getPins(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): List<Pin>
}
