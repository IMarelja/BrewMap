package hr.algebra.mobileapp.models.review

/**
 * Mirrors `ReadReview` DTO from the BrewMap API.
 *
 * [targetType] is either `"location"` or `"product"` (drink).
 * [targetId]   is the MongoDB ObjectId of the reviewed location or drink.
 * [rating]     is an integer 1–5.
 */
data class Review(
    val id: String,
    val userId: String,
    val targetType: String,
    val targetId: String,
    val rating: Int,
    val comment: String?,
    val isVisible: Boolean,
    val reportCount: Int,
    val createdAt: String,
    val updatedAt: String
)