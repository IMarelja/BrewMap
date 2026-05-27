package hr.algebra.mobileapp.service.drink

import hr.algebra.mobileapp.api.ServiceResult
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

    override suspend fun getById(id: String): ServiceResult<Drink> {
        val drink = data.drinks.find { it.id == id }
            ?: return ServiceResult.failure("Drink not found: $id")
        return ServiceResult.success(drink)
    }

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Drink>> =
        ServiceResult.success(data.drinks.filter { it.availableAtLocationId == locationId })

    override suspend fun getBestDrinkByLocationId(locationId: String): ServiceResult<BestDrink?> {
        val best = data.drinks
            .filter { it.availableAtLocationId == locationId && it.aggregatedRating.count > 0 }
            .maxByOrNull { it.aggregatedRating.average }
            ?.let { BestDrink(id = it.id, name = it.name, rating = it.aggregatedRating.average) }
        return ServiceResult.success(best)
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun create(request: CreateDrinkRequest): ServiceResult<Drink> {
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
        return ServiceResult.success(drink)
    }

    override suspend fun update(id: String, name: String, description: String?): ServiceResult<Drink> {
        val index = data.drinks.indexOfFirst { it.id == id }
        if (index == -1) return ServiceResult.failure("Drink not found: $id")
        val updated = data.drinks[index].copy(
            name        = name,
            description = description,
            updatedAt   = Instant.now().toString()
        )
        data.drinks[index] = updated
        return ServiceResult.success(updated)
    }
}
