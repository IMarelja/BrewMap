package hr.algebra.mobileapp.service.review

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.Review

/**
 * **Production** review service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`.
 */
class ReviewServiceApi : IReviewService {

    // ── GET api/Review/{id} ───────────────────────────────────────────────────

    override suspend fun getById(id: String): Review? {
        return try {
            val client = API.createClient()
            val res = client.request(
                endpoint     = "Review/$id",
                method       = HttpMethod.GET,
                responseType = object : TypeToken<Review>() {}
            )
            Log.d("ReviewServiceApi", "getById($id) → $res")
            res
        } catch (e: Exception) {
            Log.d("ReviewServiceApi", "getById($id) → null (${e.message})")
            null
        }
    }

    // ── GET api/Review/location/{locationId} ──────────────────────────────────

    override suspend fun getByLocationId(locationId: String): List<Review> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Review/location/$locationId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        )
        Log.d("ReviewServiceApi", "getByLocationId($locationId) → ${res.size} reviews")
        return res
    }

    // ── GET api/Review/drink/{drinkId} ────────────────────────────────────────

    override suspend fun getByDrinkId(drinkId: String): List<Review> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Review/drink/$drinkId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        )
        Log.d("ReviewServiceApi", "getByDrinkId($drinkId) → ${res.size} reviews")
        return res
    }

    // ── GET api/Review/mine ───────────────────────────────────────────────────

    override suspend fun getMyReviews(): List<Review> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Review/mine",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        )
        Log.d("ReviewServiceApi", "getMyReviews() → ${res.size} reviews")
        return res
    }

    // ── GET api/Review/byUser/{userId} ────────────────────────────────────────

    override suspend fun getByUserId(userId: String): List<Review> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Review/byUser/$userId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Review>>() {}
        )
        Log.d("ReviewServiceApi", "getByUserId($userId) → ${res.size} reviews")
        return res
    }
}
