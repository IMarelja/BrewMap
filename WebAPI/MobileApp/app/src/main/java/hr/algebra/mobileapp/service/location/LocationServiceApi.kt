package hr.algebra.mobileapp.service.location

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.location.CreateLocationRequest
import hr.algebra.mobileapp.models.location.Location
import hr.algebra.mobileapp.models.location.Pin
import hr.algebra.mobileapp.models.location.UpdateLocationRequest
import hr.algebra.mobileapp.service.RequestBodyValidator
import java.net.URLEncoder

/**
 * **Production** location service — delegates every call to the BrewMap REST API.
 */
class LocationServiceApi : ILocationService {

    // ── GET api/Locations/{id} ────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Location> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Locations/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Location>() {}
        ).toServiceResult("Could not load locationService.")
        Log.d("LocationServiceApi", "getById($id) → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Locations/search ──────────────────────────────────────────────

    override suspend fun search(
        longitude: Double,
        latitude: Double,
        query: String?,
        minRating: Double?,
        drinkQuery: String?,
        categoryTags: List<String>?,
        paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): ServiceResult<List<Location>> {
        val params = buildList {
            add("longitude=$longitude")
            add("latitude=$latitude")
            add("radiusMeters=$radiusMeters")
            query?.let              { add("query=${encode(it)}") }
            minRating?.let          { add("minRating=$it") }
            drinkQuery?.let         { add("drinkQuery=${encode(it)}") }
            categoryTags?.forEach   { add("categoryTags=${encode(it)}") }
            paymentOptionTags?.forEach { add("paymentOptionTags=${encode(it)}") }
        }
        val endpoint = "Locations/search?" + params.joinToString("&")
        val client = API.createClient()
        val result = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Location>>() {}
        ).toServiceResult("Search failed.")
        Log.d("LocationServiceApi", "search → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Locations/pins ────────────────────────────────────────────────

    override suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): ServiceResult<List<Pin>> {
        val endpoint = "Locations/pins?minLon=$minLon&maxLon=$maxLon&minLat=$minLat&maxLat=$maxLat"
        val client = API.createClient()
        val result = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Pin>>() {}
        ).toServiceResult("Could not load map pins.")
        Log.d("LocationServiceApi", "getPins → success=${result.isSuccess}")
        return result
    }

    // ── POST api/Locations ────────────────────────────────────────────────────

    override suspend fun create(request: CreateLocationRequest): ServiceResult<Location> {
        val validationErrors = RequestBodyValidator.validateCreateLocation(request)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val client = API.createClient()
        val result = client.request(
            endpoint     = "Locations",
            method       = HttpMethod.POST,
            body         = request,
            responseType = object : TypeToken<Location>() {}
        ).toServiceResult("Could not create location.")
        Log.d("LocationServiceApi", "create → success=${result.isSuccess}")
        return result
    }

    // ── PUT api/Locations/{id} ────────────────────────────────────────────────

    override suspend fun update(id: String, request: UpdateLocationRequest): ServiceResult<Location> {
        val validationErrors = RequestBodyValidator.validateUpdateLocation(request)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val client = API.createClient()
        val result = client.request(
            endpoint     = "Locations/$id",
            method       = HttpMethod.PUT,
            body         = request,
            responseType = object : TypeToken<Location>() {}
        ).toServiceResult("Could not update location.")
        Log.d("LocationServiceApi", "update($id) → success=${result.isSuccess}")
        return result
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
