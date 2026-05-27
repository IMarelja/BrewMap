package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.AggregatedRating
import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.CreateDrinkRequest
import hr.algebra.mobileapp.models.Drink
import hr.algebra.mobileapp.service.HardCodeData
import java.time.Instant

/**
 * **Test / offline** drink service — no network required.
 *
 * Write operations mutate [HardCodeData.drinks] in memory.
 */
class DrinkServiceHardCode(private val data: HardCodeData) : IDrinkService {

    // ── Read ──────────────────────────────────────────────────────────────────

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

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun create(request: CreateDrinkRequest): Drink {
        val now = Instant.now().toString()
        val drink = Drink(
            id                    = "hc-drk-${System.currentTimeMillis()}",
            name                  = request.name,
            description           = request.description,
            availableAtLocationId = request.locationId,
            createdByUserId       = TokenManager.getUserId() ?: "hc-001",
            createdAt             = now,
            updatedAt             = now,
            aggregatedRating      = AggregatedRating(average = 0.0, count = 0)
        )
        data.drinks.add(drink)
        return drink
    }

    override suspend fun update(id: String, name: String, description: String?): Drink {
        val index = data.drinks.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Drink not found: $id")
        val updated = data.drinks[index].copy(
            name        = name,
            description = description,
            updatedAt   = Instant.now().toString()
        )
        data.drinks[index] = updated
        return updated
    }
}
