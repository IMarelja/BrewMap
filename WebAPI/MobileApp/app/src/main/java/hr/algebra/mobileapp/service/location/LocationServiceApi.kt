package hr.algebra.mobileapp.service.location

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.CreateLocationRequest
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
import hr.algebra.mobileapp.models.UpdateLocationRequest
import java.net.URLEncoder

/**
 * **Production** location service — delegates every call to the BrewMap REST API.
 */
class LocationServiceApi : ILocationService {

    // ── GET api/Locations/{id} ────────────────────────────────────────────────

    override suspend fun getById(id: String): Location {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Locations/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Location>() {}
        ).getOrThrow()
        Log.d("LocationServiceApi", "getById($id) → $res")
        return res
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
    ): List<Location> {
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
        val res = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Location>>() {}
        ).getOrThrow()
        Log.d("LocationServiceApi", "search → ${res.size} results")
        return res
    }

    // ── GET api/Locations/pins ────────────────────────────────────────────────

    override suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): List<Pin> {
        val endpoint = "Locations/pins?minLon=$minLon&maxLon=$maxLon&minLat=$minLat&maxLat=$maxLat"
        val client = API.createClient()
        val res = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Pin>>() {}
        ).getOrThrow()
        Log.d("LocationServiceApi", "getPins → ${res.size} pins")
        return res
    }

    // ── POST api/Locations ────────────────────────────────────────────────────

    override suspend fun create(request: CreateLocationRequest): Location {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Locations",
            method       = HttpMethod.POST,
            body         = request,
            responseType = object : TypeToken<Location>() {}
        ).getOrThrow()
        Log.d("LocationServiceApi", "create → ${res.id}")
        return res
    }

    // ── PUT api/Locations/{id} ────────────────────────────────────────────────

    override suspend fun update(id: String, request: UpdateLocationRequest): Location {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Locations/$id",
            method       = HttpMethod.PUT,
            body         = request,
            responseType = object : TypeToken<Location>() {}
        ).getOrThrow()
        Log.d("LocationServiceApi", "update($id) → done")
        return res
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
