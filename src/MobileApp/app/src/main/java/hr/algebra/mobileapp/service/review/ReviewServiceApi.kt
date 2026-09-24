package hr.algebra.mobileapp.service.review

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.review.Review

/**
 * **Production** reviewService service — delegates every call to the BrewMap REST API.
 */
class ReviewServiceApi : IReviewService {

    // ── GET api/Review/{id} ───────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Review?> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Review?>() {}
        ).toServiceResult(treatNotFoundAsEmpty = true)
        Log.d("ReviewServiceApi", "getById($id) → data=${result.data}")
        return result
    }

    // ── GET api/Review/location/{locationId} ──────────────────────────────────

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Review>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/location/$locationId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        ).toServiceResult("Could not load reviews.")
        Log.d("ReviewServiceApi", "getByLocationId($locationId) → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Review/drink/{drinkId} ────────────────────────────────────────

    override suspend fun getByDrinkId(drinkId: String): ServiceResult<List<Review>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/drink/$drinkId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        ).toServiceResult("Could not load reviews.")
        Log.d("ReviewServiceApi", "getByDrinkId($drinkId) → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Review/mine ───────────────────────────────────────────────────

    override suspend fun getMyReviews(): ServiceResult<List<Review>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/mine",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        ).toServiceResult("Could not load your reviews.")
        Log.d("ReviewServiceApi", "getMyReviews() → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Review/byUser/{userId} ────────────────────────────────────────

    override suspend fun getByUserId(userId: String): ServiceResult<List<Review>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/byUser/$userId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        ).toServiceResult("Could not load reviews.")
        Log.d("ReviewServiceApi", "getByUserId($userId) → success=${result.isSuccess}")
        return result
    }

    // ── POST api/Review/location/{locationId} ─────────────────────────────────

    override suspend fun createForLocation(locationId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/location/$locationId",
            method       = HttpMethod.POST,
            body         = mapOf("rating" to rating, "comment" to comment),
            responseType = object : TypeToken<Review>() {}
        ).toServiceResult("Could not submit review.")
        Log.d("ReviewServiceApi", "createForLocation($locationId) → success=${result.isSuccess}")
        return result
    }

    // ── POST api/Review/drink/{drinkId} ──────────────────────────────────────

    override suspend fun createForDrink(drinkId: String, rating: Int, comment: String?): ServiceResult<Review> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/drink/$drinkId",
            method       = HttpMethod.POST,
            body         = mapOf("rating" to rating, "comment" to comment),
            responseType = object : TypeToken<Review>() {}
        ).toServiceResult("Could not submit review.")
        Log.d("ReviewServiceApi", "createForDrink($drinkId) → success=${result.isSuccess}")
        return result
    }

    // ── PUT api/Review/{id} ───────────────────────────────────────────────────

    override suspend fun update(reviewId: String, rating: Int?, comment: String?): ServiceResult<Review> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/$reviewId",
            method       = HttpMethod.PUT,
            body         = mapOf("rating" to rating, "comment" to comment),
            responseType = object : TypeToken<Review>() {}
        ).toServiceResult("Could not update reviewService.")
        Log.d("ReviewServiceApi", "update($reviewId) → success=${result.isSuccess}")
        return result
    }

    // ── DELETE api/Review/{id} ────────────────────────────────────────────────

    override suspend fun delete(reviewId: String): ServiceResult<Unit> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Review/$reviewId",
            method       = HttpMethod.DELETE,
            responseType = object : TypeToken<Map<String, Any>>() {}
        ).toServiceResult("Could not delete review.").mapToUnit()
        Log.d("ReviewServiceApi", "delete($reviewId) → success=${result.isSuccess}")
        return result
    }
}
