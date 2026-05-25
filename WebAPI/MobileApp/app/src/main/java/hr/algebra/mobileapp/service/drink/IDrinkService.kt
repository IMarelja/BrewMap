package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.Drink

/**
 * Contract for drink read operations.
 *
 * Three implementations are available:
 *  - [DrinkServiceApi]        — real HTTP calls to `api/Drink`  (requires auth token)
 *  - [DrinkServiceHardCode]   — in-memory stub, no network needed
 *  - [DrinkServicePersistent] — API-backed with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache])
 *
 * Switch between them via [hr.algebra.mobileapp.service.ServiceProvider].
 */
interface IDrinkService {

    /**
     * Fetch a single drink by its MongoDB ObjectId.
     * Maps to `GET api/Drink/{id}`.
     */
    suspend fun getById(id: String): Drink

    /**
     * Fetch all visible drinks that belong to a location.
     * Maps to `GET api/Drink/location/{locationId}`.
     */
    suspend fun getByLocationId(locationId: String): List<Drink>

    /**
     * Fetch the highest-rated drink for a location.
     * Maps to `GET api/Drink/location/{locationId}/best-drink`.
     * Returns null when the location has no drinks with any reviews.
     */
    suspend fun getBestDrinkByLocationId(locationId: String): BestDrink?
}
