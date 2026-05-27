package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.drink.BestDrink
import hr.algebra.mobileapp.models.drink.CreateDrinkRequest
import hr.algebra.mobileapp.models.drink.Drink
import hr.algebra.mobileapp.service.RequestBodyValidator

/**
 * **Production** drinkService service — delegates every call to the BrewMap REST API.
 */
class DrinkServiceApi : IDrinkService {

    // ── GET api/Drink/{id} ────────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Drink> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Drink/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Drink>() {}
        ).toServiceResult("Could not load drinkService.")
        Log.d("DrinkServiceApi", "getById($id) → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Drink/locationService/{locationId} ───────────────────────────────────

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Drink>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Drink/locationService/$locationId",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Drink>>() {}
        ).toServiceResult("Could not load drinks.")
        Log.d("DrinkServiceApi", "getByLocationId($locationId) → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Drink/locationService/{locationId}/best-drinkService ────────────────────────

    override suspend fun getBestDrinkByLocationId(locationId: String): ServiceResult<BestDrink?> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Drink/locationService/$locationId/best-drinkService",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<BestDrink?>() {}
        ).toServiceResult(treatNotFoundAsEmpty = true)
        Log.d("DrinkServiceApi", "getBestDrink($locationId) → data=${result.data}")
        return result
    }

    // ── POST api/Drink ────────────────────────────────────────────────────────

    override suspend fun create(request: CreateDrinkRequest): ServiceResult<Drink> {
        val validationErrors = RequestBodyValidator.validateCreateDrink(request.name, request.locationId)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val client = API.createClient()
        val result = client.request(
            endpoint     = "Drink",
            method       = HttpMethod.POST,
            body         = request,
            responseType = object : TypeToken<Drink>() {}
        ).toServiceResult("Could not create drinkService.")
        Log.d("DrinkServiceApi", "create → success=${result.isSuccess}")
        return result
    }

    // ── PUT api/Drink/{id} ────────────────────────────────────────────────────

    override suspend fun update(id: String, name: String, description: String?): ServiceResult<Drink> {
        val validationErrors = RequestBodyValidator.validateUpdateDrink(name)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val client = API.createClient()
        val body = mapOf("name" to name, "description" to description)
        val result = client.request(
            endpoint     = "Drink/$id",
            method       = HttpMethod.PUT,
            body         = body,
            responseType = object : TypeToken<Drink>() {}
        ).toServiceResult("Could not update drinkService.")
        Log.d("DrinkServiceApi", "update($id) → success=${result.isSuccess}")
        return result
    }
}
