package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.Drink
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** drink service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains query logic.
 *
 * [getBestDrinkByLocationId] returns the drink with the highest
 * [hr.algebra.mobileapp.models.AggregatedRating.average], or null when a
 * location has no drinks with at least one review.
 */
class DrinkServiceHardCode(private val data: HardCodeData) : IDrinkService {

    // ── IDrinkService ─────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Drink =
        data.drinks.find { it.id == id }
            ?: throw NoSuchElementException("Drink not found: $id")

    override suspend fun getByLocationId(locationId: String): List<Drink> =
        data.drinks.filter { it.availableAtLocationId == locationId }

    override suspend fun getBestDrinkByLocationId(locationId: String): BestDrink? {
        return data.drinks
            .filter { it.availableAtLocationId == locationId && it.aggregatedRating.count > 0 }
            .maxByOrNull { it.aggregatedRating.average }
            ?.let { BestDrink(id = it.id, name = it.name, rating = it.aggregatedRating.average) }
    }
}
