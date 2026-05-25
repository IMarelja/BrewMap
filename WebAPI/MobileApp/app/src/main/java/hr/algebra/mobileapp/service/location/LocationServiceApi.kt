package hr.algebra.mobileapp.service.location

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
import java.net.URLEncoder

/**
 * **Production** product service — delegates every call to the BrewMap REST API.
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`
 * (see [hr.algebra.mobileapp.api.API.Companion.createClient]).
 */
class LocationServiceApi : ILocationService {

    // ── GET api/Locations/{id} ────────────────────────────────────────────────

    override suspend fun getById(id: String): Location {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Locations/$id",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Location>() {}
        )
        Log.d("ProductServiceApi", "getById($id) → $res")
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
            // required geo params always come first
            add("longitude=$longitude")
            add("latitude=$latitude")
            add("radiusMeters=$radiusMeters")

            query?.let           { add("query=${encode(it)}") }
            minRating?.let       { add("minRating=$it") }
            drinkQuery?.let      { add("drinkQuery=${encode(it)}") }
            categoryTags?.forEach    { add("categoryTags=${encode(it)}") }
            paymentOptionTags?.forEach { add("paymentOptionTags=${encode(it)}") }
        }

        val endpoint = "Locations/search?" + params.joinToString("&")
        val client = API.createClient()
        val res = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Location>>() {}
        )
        Log.d("ProductServiceApi", "search → ${res.size} results")
        return res
    }

    // ── GET api/Locations/pins ────────────────────────────────────────────────

    override suspend fun getPins(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): List<Pin> {
        val endpoint = "Locations/pins?minLon=$minLon&maxLon=$maxLon&minLat=$minLat&maxLat=$maxLat"
        val client = API.createClient()
        val res = client.request(
            endpoint     = endpoint,
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Pin>>() {}
        )
        Log.d("ProductServiceApi", "getPins → ${res.size} pins")
        return res
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
}
