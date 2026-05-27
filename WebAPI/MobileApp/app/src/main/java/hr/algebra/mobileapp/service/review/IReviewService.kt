package hr.algebra.mobileapp.service.review

import hr.algebra.mobileapp.models.Review

/**
 * Contract for review read **and write** operations.
 *
 * Three implementations:
 *  - [ReviewServiceApi]        — live BrewMap REST API  (requires auth token)
 *  - [ReviewServiceHardCode]   — in-memory stub, no network
 *  - [ReviewServicePersistent] — API-backed with on-device cache
 */
interface IReviewService {

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun getById(id: String): Review?
    suspend fun getByLocationId(locationId: String): List<Review>
    suspend fun getByDrinkId(drinkId: String): List<Review>
    suspend fun getMyReviews(): List<Review>
    suspend fun getByUserId(userId: String): List<Review>

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Review/location/{locationId}` — rating must be 1–5. */
    suspend fun createForLocation(locationId: String, rating: Int, comment: String?): Review

    /** `POST api/Review/drink/{drinkId}` — rating must be 1–5. */
    suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): Review

    /** `PUT api/Review/{id}` — pass null for fields that should not change. */
    suspend fun update(reviewId: String, rating: Int?, comment: String?): Review

    /** `DELETE api/Review/{id}`. */
    suspend fun delete(reviewId: String)
}
