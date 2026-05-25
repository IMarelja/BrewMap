package hr.algebra.mobileapp.models

/**
 * Mirrors `ReadDrink` DTO from the BrewMap API.
 *
 * `aggregatedRating` is kept as a nested object to match the JSON shape
 * returned by `GET api/Drink/{id}` and `GET api/Drink/location/{locationId}`.
 */
data class Drink(
    val id: String,
    val name: String,
    val description: String?,
    val availableAtLocationId: String,
    val createdByUserId: String,
    val createdAt: String,
    val updatedAt: String,
    val aggregatedRating: AggregatedRating
)

/**
 * Mirrors `ReadAggregatedRating` from the BrewMap API (used inside [Drink]).
 */
data class AggregatedRating(
    val average: Double,
    val count: Int
)

/**
 * Mirrors `ReadBestDrink` DTO — returned by
 * `GET api/Drink/location/{locationId}/best-drink`.
 */
data class BestDrink(
    val id: String,
    val name: String,
    val rating: Double
)
