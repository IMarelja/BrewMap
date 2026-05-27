package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.CreateDrinkRequest
import hr.algebra.mobileapp.models.Drink

/**
 * Contract for drink read **and write** operations.
 *
 * Three implementations:
 *  - [DrinkServiceApi]        — live BrewMap REST API  (requires auth token)
 *  - [DrinkServiceHardCode]   — in-memory stub, no network
 *  - [DrinkServicePersistent] — API-backed with on-device cache
 */
interface IDrinkService {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** `GET api/Drink/{id}` */
    suspend fun getById(id: String): Drink

    /** `GET api/Drink/location/{locationId}` */
    suspend fun getByLocationId(locationId: String): List<Drink>

    /** `GET api/Drink/location/{locationId}/best-drink` — null when no rated drinks exist. */
    suspend fun getBestDrinkByLocationId(locationId: String): BestDrink?

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Drink` — creates a new drink for a location. Returns the created resource. */
    suspend fun create(request: CreateDrinkRequest): Drink

    /**
     * `PUT api/Drink/{id}` — updates name and/or description.
     * Pass null for [description] to leave it unchanged.
     */
    suspend fun update(id: String, name: String, description: String?): Drink
}
