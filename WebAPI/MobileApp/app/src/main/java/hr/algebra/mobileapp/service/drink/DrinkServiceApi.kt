package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.Drink

/**
 * **Production** drink service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`.
 */
class DrinkServiceApi : IDrinkService {

    // ── GET api/Drink/{id} ────────────────────────────────────────────────────

    override suspend fun getById(id: String): Drink {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Drink/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Drink>() {}
        )
        Log.d("DrinkServiceApi", "getById($id) → $res")
        return res
    }

    // ── GET api/Drink/location/{locationId} ───────────────────────────────────

    override suspend fun getByLocationId(locationId: String): List<Drink> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Drink/location/$locationId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Drink>>() {}
        )
        Log.d("DrinkServiceApi", "getByLocationId($locationId) → ${res.size} drinks")
        return res
    }

    // ── GET api/Drink/location/{locationId}/best-drink ────────────────────────

    override suspend fun getBestDrinkByLocationId(locationId: String): BestDrink? {
        return try {
            val client = API.createClient()
            val res = client.request(
                endpoint     = "Drink/location/$locationId/best-drink",
                method       = HttpMethod.GET,
                responseType = object : TypeToken<BestDrink>() {}
            )
            Log.d("DrinkServiceApi", "getBestDrink($locationId) → $res")
            res
        } catch (e: Exception) {
            // 404 — location has no rated drinks
            Log.d("DrinkServiceApi", "getBestDrink($locationId) → null (${e.message})")
            null
        }
    }
}
