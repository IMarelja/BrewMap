package hr.algebra.mobileapp.service.review

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.review.Review

/**
 * Contract for reviewService read **and write** operations.
 *
 * Three implementations:
 *  - [ReviewServiceApi]        — live BrewMap REST API  (requires authService token)
 *  - [ReviewServiceHardCode]   — in-memory stub, no network
 *  - [ReviewServicePersistent] — API-backed with on-device cache
 *
 * Every method returns [ServiceResult]<T>. On success [ServiceResult.data] holds the result;
 * on failure [ServiceResult.errors] contains one or more human-readable messages.
 */
interface IReviewService {

    // ── Read ──────────────────────────────────────────────────────────────────

    /** data=null when no reviewService exists with this id (not an error). */
    suspend fun getById(id: String): ServiceResult<Review?>
    suspend fun getByLocationId(locationId: String): ServiceResult<List<Review>>
    suspend fun getByDrinkId(drinkId: String): ServiceResult<List<Review>>
    suspend fun getMyReviews(): ServiceResult<List<Review>>
    suspend fun getByUserId(userId: String): ServiceResult<List<Review>>

    // ── Write ─────────────────────────────────────────────────────────────────

    /** `POST api/Review/locationService/{locationId}` — rating must be 1–5. */
    suspend fun createForLocation(locationId: String, rating: Int, comment: String?): ServiceResult<Review>

    /** `POST api/Review/drinkService/{drinkId}` — rating must be 1–5. */
    suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): ServiceResult<Review>

    /** `PUT api/Review/{id}` — pass null for fields that should not change. */
    suspend fun update(reviewId: String, rating: Int?, comment: String?): ServiceResult<Review>

    /** `DELETE api/Review/{id}`. */
    suspend fun delete(reviewId: String): ServiceResult<Unit>
}
