package hr.algebra.mobileapp.service.review

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.review.Review

/**
 * **Cache-aside** reviewService service.
 *
 * Reads are served from [PersistentCache] with a 2-minute TTL.
 * [getMyReviews] is **not** cached — the userService must always see their own latest.
 * Write operations bypass the cache and also invalidate related cache entries
 * so the next read reflects the change.
 */
class ReviewServicePersistent : IReviewService {

    private val api = ReviewServiceApi()

    companion object {
        private const val TTL_REVIEWS = 2 * 60 * 1_000L
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Review?> {
        val key  = "review_id_$id"
        val type = object : TypeToken<Review>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getById($id)")
            return ServiceResult.success(it)
        }
        val result = api.getById(id)
        // data == null means "reviewService not found" — valid, do not cache null
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, TTL_REVIEWS)
        }
        return result
    }

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Review>> {
        val key  = "review_loc_$locationId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByLocationId($locationId)")
            return ServiceResult.success(it)
        }
        val result = api.getByLocationId(locationId)
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, TTL_REVIEWS)
        }
        return result
    }

    override suspend fun getByDrinkId(drinkId: String): ServiceResult<List<Review>> {
        val key  = "review_drink_$drinkId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByDrinkId($drinkId)")
            return ServiceResult.success(it)
        }
        val result = api.getByDrinkId(drinkId)
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, TTL_REVIEWS)
        }
        return result
    }

    override suspend fun getMyReviews(): ServiceResult<List<Review>> = api.getMyReviews()

    override suspend fun getByUserId(userId: String): ServiceResult<List<Review>> {
        val key  = "review_user_$userId"
        val type = object : TypeToken<List<Review>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("ReviewServicePersistent", "cache hit: getByUserId($userId)")
            return ServiceResult.success(it)
        }
        val result = api.getByUserId(userId)
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, TTL_REVIEWS)
        }
        return result
    }

    // ── Write — bypass cache, invalidate on success ───────────────────────────

    override suspend fun createForLocation(locationId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val result = api.createForLocation(locationId, rating, comment)
        if (result.isSuccess) {
            PersistentCache.remove("review_loc_$locationId")
        }
        return result
    }

    override suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val result = api.createForDrink(drinkId, rating, comment)
        if (result.isSuccess) {
            PersistentCache.remove("review_drink_$drinkId")
            // A new reviewService changes the drink's aggregatedRating (count + average).
            // We don't know which locationService this drinkService belongs to here, so evict every
            // cached drinkService entry — "drink_id_*", "drink_loc_*" and "drink_best_*" — rather
            // than risk the drinkService list / detail showing a stale count or score.
            PersistentCache.clearByPrefix("drink_")
        }
        return result
    }

    override suspend fun update(reviewId: String, rating: Int?, comment: String?): ServiceResult<Review> {
        val result = api.update(reviewId, rating, comment)
        if (result.isSuccess) {
            PersistentCache.remove("review_id_$reviewId")
        }
        return result
    }

    override suspend fun delete(reviewId: String): ServiceResult<Unit> {
        val result = api.delete(reviewId)
        if (result.isSuccess) {
            PersistentCache.remove("review_id_$reviewId")
        }
        return result
    }
}
