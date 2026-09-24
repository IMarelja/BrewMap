package hr.algebra.mobileapp.models.review

/**
 * Mirrors `ReadReview` DTO from the BrewMap API.
 *
 * [targetType] is either `"locationService"` or `"product"` (drinkService).
 * [targetId]   is the MongoDB ObjectId of the reviewed locationService or drinkService.
 * [rating]     is an integer 1–5.
 */
data class Review(
    val id: String,
    val userId: String,
    val username: String?,
    val targetType: String,
    val targetId: String,
    val rating: Int,
    val comment: String?,
    val isVisible: Boolean,
    val reportCount: Int,
    val createdAt: String,
    val updatedAt: String
)
