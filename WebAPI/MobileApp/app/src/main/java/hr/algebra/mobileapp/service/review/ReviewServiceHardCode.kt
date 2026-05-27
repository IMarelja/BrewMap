package hr.algebra.mobileapp.service.review

import android.util.Log
import hr.algebra.mobileapp.api.ServiceResult
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

    override suspend fun getById(id: String): ServiceResult<Review?> =
        ServiceResult.success(data.reviews.find { it.id == id && it.isVisible })

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Review>> =
        ServiceResult.success(
            data.reviews.filter { it.targetType == "location" && it.targetId == locationId && it.isVisible }
        )

    override suspend fun getByDrinkId(drinkId: String): ServiceResult<List<Review>> =
        ServiceResult.success(
            data.reviews.filter { it.targetType == "product" && it.targetId == drinkId && it.isVisible }
        )

    override suspend fun getMyReviews(): ServiceResult<List<Review>> {
        val userId = TokenManager.getUserId()
        if (userId == null) {
            Log.w("ReviewServiceHardCode", "getMyReviews() called with no active session")
            return ServiceResult.success(emptyList())
        }
        return ServiceResult.success(data.reviews.filter { it.userId == userId && it.isVisible })
    }

    override suspend fun getByUserId(userId: String): ServiceResult<List<Review>> =
        ServiceResult.success(data.reviews.filter { it.userId == userId && it.isVisible })

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun createForLocation(locationId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
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
        return ServiceResult.success(review)
    }

    override suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val userId = TokenManager.getUserId()
            ?: return ServiceResult.failure("Not authenticated")
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
        return ServiceResult.success(review)
    }

    override suspend fun update(reviewId: String, rating: Int?, comment: String?): ServiceResult<Review> {
        val index = data.reviews.indexOfFirst { it.id == reviewId }
        if (index == -1) return ServiceResult.failure("Review not found: $reviewId")
        val old = data.reviews[index]
        val updated = old.copy(
            rating    = rating ?: old.rating,
            comment   = comment ?: old.comment,
            updatedAt = Instant.now().toString()
        )
        data.reviews[index] = updated
        return ServiceResult.success(updated)
    }

    override suspend fun delete(reviewId: String): ServiceResult<Unit> {
        val removed = data.reviews.removeIf { it.id == reviewId }
        if (!removed) return ServiceResult.failure("Review not found: $reviewId")
        return ServiceResult.success(Unit)
    }
}
