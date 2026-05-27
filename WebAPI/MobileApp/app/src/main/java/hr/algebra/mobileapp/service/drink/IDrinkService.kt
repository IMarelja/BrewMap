package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.drink.BestDrink
import hr.algebra.mobileapp.models.drink.CreateDrinkRequest
import hr.algebra.mobileapp.models.drink.Drink

/**
 * Contract for drink read **and write** operations.
 *
 * Three implementations:
 *  - [DrinkServiceApi]        — live BrewMap REST API  (requires auth token)
 *  - [DrinkServiceHardCode]   — in-memory stub, no network
 *  - [DrinkServicePersistent] — API-backed with on-device cache
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface IDrinkService {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** `GET api/Drink/{id}` */
    suspend fun getById(id: String): ServiceResult<Drink>

    /** `GET api/Drink/location/{locationId}` */
    suspend fun getByLocationId(locationId: String): ServiceResult<List<Drink>>

    /** `GET api/Drink/location/{locationId}/best-drink` — data=null when no rated drinks exist. */
    suspend fun getBestDrinkByLocationId(locationId: String): ServiceResult<BestDrink?>

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Drink` — creates a new drink for a location. Returns the created resource. */
    suspend fun create(request: CreateDrinkRequest): ServiceResult<Drink>

    /**
     * `PUT api/Drink/{id}` — updates name and/or description.
     * Pass null for [description] to leave it unchanged.
     */
    suspend fun update(id: String, name: String, description: String?): ServiceResult<Drink>
}
