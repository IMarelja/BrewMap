package hr.algebra.mobileapp.service.review

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.Review

/**
 * **Cache-aside** review service.
 *
 * Reads are served from [PersistentCache] with a 2-minute TTL.
 * [getMyReviews] is **not** cached — the user must always see their own latest.
 * Write operations bypass the cache and also invalidate related cache entries
 * so the next read reflects the change.
 */
class ReviewServicePersistent : IReviewService {

    private val api = ReviewServiceApi()

    companion object {
        private const val TTL_REVIEWS = 2 * 60 * 1_000L
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Review? {
        val key  = "review_id_$id"
        val type = object : TypeToken<Review>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getById($id)")
            return it
        }
        val fresh = api.getById(id)
        if (fresh != null) PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }

    override suspend fun getByLocationId(locationId: String): List<Review> {
        val key  = "review_loc_$locationId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByLocationId($locationId)")
            return it
        }
        val fresh = api.getByLocationId(locationId)
        PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }

    override suspend fun getByDrinkId(drinkId: String): List<Review> {
        val key  = "review_drink_$drinkId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByDrinkId($drinkId)")
            return it
        }
        val fresh = api.getByDrinkId(drinkId)
        PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }

    override suspend fun getMyReviews(): List<Review> = api.getMyReviews()

    override suspend fun getByUserId(userId: String): List<Review> {
        val key  = "review_user_$userId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByUserId($userId)")
            return it
        }
        val fresh = api.getByUserId(userId)
        PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }

    // ── Write — bypass cache, invalidate on success ───────────────────────────

    override suspend fun createForLocation(locationId: String, rating: Int, comment: String?): Review {
        val result = api.createForLocation(locationId, rating, comment)
        PersistentCache.remove("review_loc_$locationId")   // next read will refetch
        return result
    }

    override suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): Review {
        val result = api.createForDrink(drinkId, rating, comment)
        PersistentCache.remove("review_drink_$drinkId")
        return result
    }

    override suspend fun update(reviewId: String, rating: Int?, comment: String?): Review {
        val result = api.update(reviewId, rating, comment)
        PersistentCache.remove("review_id_$reviewId")
        return result
    }

    override suspend fun delete(reviewId: String) {
        api.delete(reviewId)
        PersistentCache.remove("review_id_$reviewId")
    }
}
