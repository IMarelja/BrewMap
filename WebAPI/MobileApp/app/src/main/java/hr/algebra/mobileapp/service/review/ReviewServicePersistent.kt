package hr.algebra.mobileapp.service.review

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.Review

/**
 * **Cache-aside** review service — serves data from [PersistentCache] when
 * available and fresh, falling back to [ReviewServiceApi] on a cache miss.
 *
 * Reviews are user-generated content that changes frequently, so a short
 * 2-minute TTL is used. [getMyReviews] is intentionally **not cached** —
 * the user always sees their own latest reviews immediately.
 *
 * ## Cache keys & TTLs
 * | Operation          | Key                         | TTL    |
 * |--------------------|-----------------------------|--------|
 * | [getById]          | `review_id_<id>`            | 2 min  |
 * | [getByLocationId]  | `review_loc_<locationId>`   | 2 min  |
 * | [getByDrinkId]     | `review_drink_<drinkId>`    | 2 min  |
 * | [getMyReviews]     | *(not cached)*              | —      |
 * | [getByUserId]      | `review_user_<userId>`      | 2 min  |
 */
class ReviewServicePersistent : IReviewService {

    private val api = ReviewServiceApi()

    companion object {
        private const val TTL_REVIEWS = 2 * 60 * 1_000L  // 2 minutes
    }

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
            Log.d("ReviewServicePersistent", "cache hit: getByLocationId($locationId) → ${it.size}")
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
            Log.d("ReviewServicePersistent", "cache hit: getByDrinkId($drinkId) → ${it.size}")
            return it
        }

        val fresh = api.getByDrinkId(drinkId)
        PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }

    /** Not cached — the current user must always see their latest reviews. */
    override suspend fun getMyReviews(): List<Review> = api.getMyReviews()

    override suspend fun getByUserId(userId: String): List<Review> {
        val key  = "review_user_$userId"
        val type = object : TypeToken<List<Review>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByUserId($userId) → ${it.size}")
            return it
        }

        val fresh = api.getByUserId(userId)
        PersistentCache.put(key, fresh, TTL_REVIEWS)
        return fresh
    }
}
