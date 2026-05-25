package hr.algebra.mobileapp.service.review

import hr.algebra.mobileapp.models.Review

/**
 * Contract for review read operations.
 *
 * Three implementations are available:
 *  - [ReviewServiceApi]        — real HTTP calls to `api/Review`  (requires auth token)
 *  - [ReviewServiceHardCode]   — in-memory stub, no network needed
 *  - [ReviewServicePersistent] — API-backed with on-device cache ([hr.algebra.mobileapp.cache.PersistentCache])
 *
 * Switch between them via [hr.algebra.mobileapp.service.ServiceProvider].
 */
interface IReviewService {

    /**
     * Fetch a single review by its MongoDB ObjectId.
     * Maps to `GET api/Review/{id}`.
     * Returns null when the review does not exist or is not visible.
     */
    suspend fun getById(id: String): Review?

    /**
     * Fetch all visible reviews for a location.
     * Maps to `GET api/Review/location/{locationId}`.
     */
    suspend fun getByLocationId(locationId: String): List<Review>

    /**
     * Fetch all visible reviews for a drink.
     * Maps to `GET api/Review/drink/{drinkId}`.
     */
    suspend fun getByDrinkId(drinkId: String): List<Review>

    /**
     * Fetch all reviews written by the currently authenticated user.
     * Maps to `GET api/Review/mine` (JWT identity is read server-side).
     */
    suspend fun getMyReviews(): List<Review>

    /**
     * Fetch all visible reviews written by any user.
     * Maps to `GET api/Review/byUser/{userId}`.
     */
    suspend fun getByUserId(userId: String): List<Review>
}
