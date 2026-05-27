package hr.algebra.mobileapp.service.review

import android.util.Log
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.Review
import hr.algebra.mobileapp.service.HardCodeData
import java.time.Instant

/**
 * **Test / offline** review service — no network required.
 *
 * Write operations mutate [HardCodeData.reviews] in memory.
 * [getMyReviews] and write operations decode the logged-in user's ID via
 * [TokenManager.getUserId].
 */
class ReviewServiceHardCode(private val data: HardCodeData) : IReviewService {

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Review? =
        data.reviews.find { it.id == id && it.isVisible }

    override suspend fun getByLocationId(locationId: String): List<Review> =
        data.reviews.filter { it.targetType == "location" && it.targetId == locationId && it.isVisible }

    override suspend fun getByDrinkId(drinkId: String): List<Review> =
        data.reviews.filter { it.targetType == "product" && it.targetId == drinkId && it.isVisible }

    override suspend fun getMyReviews(): List<Review> {
        val userId = TokenManager.getUserId()
        if (userId == null) {
            Log.w("ReviewServiceHardCode", "getMyReviews() called with no active session")
            return emptyList()
        }
        return data.reviews.filter { it.userId == userId && it.isVisible }
    }

    override suspend fun getByUserId(userId: String): List<Review> =
        data.reviews.filter { it.userId == userId && it.isVisible }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun createForLocation(locationId: String, rating: Int, comment: String?): Review {
        val userId = TokenManager.getUserId() ?: throw IllegalStateException("Not authenticated")
        val now = Instant.now().toString()
        val review = Review(
            id          = "hc-rev-${System.currentTimeMillis()}",
            userId      = userId,
            targetType  = "location",
            targetId    = locationId,
            rating      = rating,
            comment     = comment,
            isVisible   = true,
            reportCount = 0,
            createdAt   = now,
            updatedAt   = now
        )
        data.reviews.add(review)
        return review
    }

    override suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): Review {
        val userId = TokenManager.getUserId() ?: throw IllegalStateException("Not authenticated")
        val now = Instant.now().toString()
        val review = Review(
            id          = "hc-rev-${System.currentTimeMillis()}",
            userId      = userId,
            targetType  = "product",
            targetId    = drinkId,
            rating      = rating,
            comment     = comment,
            isVisible   = true,
            reportCount = 0,
            createdAt   = now,
            updatedAt   = now
        )
        data.reviews.add(review)
        return review
    }

    override suspend fun update(reviewId: String, rating: Int?, comment: String?): Review {
        val index = data.reviews.indexOfFirst { it.id == reviewId }
        if (index == -1) throw NoSuchElementException("Review not found: $reviewId")
        val old = data.reviews[index]
        val updated = old.copy(
            rating    = rating ?: old.rating,
            comment   = comment ?: old.comment,
            updatedAt = Instant.now().toString()
        )
        data.reviews[index] = updated
        return updated
    }

    override suspend fun delete(reviewId: String) {
        val removed = data.reviews.removeIf { it.id == reviewId }
        if (!removed) throw NoSuchElementException("Review not found: $reviewId")
    }
}
