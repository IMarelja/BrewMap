package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.CreateDrinkRequest
import hr.algebra.mobileapp.models.Drink

/**
 * **Production** drink service — delegates every call to the BrewMap REST API.
 */
class DrinkServiceApi : IDrinkService {

    // ── GET api/Drink/{id} ────────────────────────────────────────────────────

    override suspend fun getById(id: String): Drink {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Drink/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Drink>() {}
        ).getOrThrow()
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
        ).getOrThrow()
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
            ).getOrThrow()
            Log.d("DrinkServiceApi", "getBestDrink($locationId) → $res")
            res
        } catch (e: Exception) {
            Log.d("DrinkServiceApi", "getBestDrink($locationId) → null (${e.message})")
            null
        }
    }

    // ── POST api/Drink ────────────────────────────────────────────────────────

    override suspend fun create(request: CreateDrinkRequest): Drink {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Drink",
            method       = HttpMethod.POST,
            body         = request,
            responseType = object : TypeToken<Drink>() {}
        ).getOrThrow()
        Log.d("DrinkServiceApi", "create → ${res.id}")
        return res
    }

    // ── PUT api/Drink/{id} ────────────────────────────────────────────────────

    override suspend fun update(id: String, name: String, description: String?): Drink {
        val client = API.createClient()
        val body = mapOf("name" to name, "description" to description)
        val res = client.request(
            endpoint     = "Drink/$id",
            method       = HttpMethod.PUT,
            body         = body,
            responseType = object : TypeToken<Drink>() {}
        ).getOrThrow()
        Log.d("DrinkServiceApi", "update($id) → done")
        return res
    }
}
