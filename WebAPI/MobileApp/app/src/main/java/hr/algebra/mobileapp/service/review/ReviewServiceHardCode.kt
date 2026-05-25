package hr.algebra.mobileapp.service.review

import android.util.Base64
import android.util.Log
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.Review
import hr.algebra.mobileapp.service.HardCodeData
import org.json.JSONObject

/**
 * **Test / offline** review service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub. This class only contains query logic.
 *
 * [getMyReviews] decodes the current JWT from [TokenManager] to resolve the
 * logged-in user's ID, then filters [HardCodeData.reviews] by that ID.
 * The mock JWT produced by `AuthServiceHardCode` stores the user ID in the
 * `id` claim.
 */
class ReviewServiceHardCode(private val data: HardCodeData) : IReviewService {

    override suspend fun getById(id: String): Review? =
        data.reviews.find { it.id == id && it.isVisible }

    override suspend fun getByLocationId(locationId: String): List<Review> =
        data.reviews.filter { it.targetType == "location" && it.targetId == locationId && it.isVisible }

    override suspend fun getByDrinkId(drinkId: String): List<Review> =
        data.reviews.filter { it.targetType == "product" && it.targetId == drinkId && it.isVisible }

    override suspend fun getMyReviews(): List<Review> {
        val userId = currentUserId()
        if (userId == null) {
            Log.w("ReviewServiceHardCode", "getMyReviews() called with no active session")
            return emptyList()
        }
        return data.reviews.filter { it.userId == userId && it.isVisible }
    }

    override suspend fun getByUserId(userId: String): List<Review> =
        data.reviews.filter { it.userId == userId && it.isVisible }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Decode the `id` claim from the mock JWT.
     * Returns null if no token is stored or the token is malformed.
     */
    private fun currentUserId(): String? {
        val token = TokenManager.getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                Charsets.UTF_8
            )
            JSONObject(payload).optString("id").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("ReviewServiceHardCode", "Failed to decode user ID from JWT", e)
            null
        }
    }
}
