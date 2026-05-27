package hr.algebra.mobileapp.service.location

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.CreateLocationRequest
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin
import hr.algebra.mobileapp.models.UpdateLocationRequest

/**
 * **Cache-aside** location service.
 *
 * Reads are served from [PersistentCache] with a 5-minute TTL.
 * Write operations bypass the cache and invalidate related entries
 * so subsequent reads reflect the change.
 */
class LocationServicePersistent : ILocationService {

    private val api = LocationServiceApi()

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Location {
        val key  = "loc_id_$id"
        val type = object : TypeToken<Location>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("LocationServicePersistent", "cache hit: getById($id)")
            return it
        }
        val fresh = api.getById(id)
        PersistentCache.put(key, fresh, PersistentCache.TTL_LOCATIONS)
        return fresh
    }

    override suspend fun search(
        longitude: Double, latitude: Double,
        query: String?, minRating: Double?, drinkQuery: String?,
        categoryTags: List<String>?, paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): List<Location> {
        val key = buildString {
            append("loc_search")
            append("_${lon4(longitude)}_${lat4(latitude)}_${radiusMeters.toInt()}")
            query?.let              { append("_q${it.hashCode()}") }
            minRating?.let          { append("_r$it") }
            drinkQuery?.let         { append("_dq${it.hashCode()}") }
            categoryTags?.sorted()?.forEach    { append("_c$it") }
            paymentOptionTags?.sorted()?.forEach { append("_p$it") }
        }
        val type = object : TypeToken<List<Location>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("LocationServicePersistent", "cache hit: search → ${it.size}")
            return it
        }
        val fresh = api.search(longitude, latitude, query, minRating, drinkQuery, categoryTags, paymentOptionTags, radiusMeters)
        PersistentCache.put(key, fresh, PersistentCache.TTL_LOCATIONS)
        return fresh
    }

    override suspend fun getPins(
        minLon: Double, maxLon: Double,
        minLat: Double, maxLat: Double
    ): List<Pin> {
        val key  = "loc_pins_${lon4(minLon)}_${lon4(maxLon)}_${lat4(minLat)}_${lat4(maxLat)}"
        val type = object : TypeToken<List<Pin>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("LocationServicePersistent", "cache hit: getPins → ${it.size}")
            return it
        }
        val fresh = api.getPins(minLon, maxLon, minLat, maxLat)
        PersistentCache.put(key, fresh, PersistentCache.TTL_LOCATIONS)
        return fresh
    }

    // ── Write — bypass cache, invalidate on success ───────────────────────────

    override suspend fun create(request: CreateLocationRequest): Location {
        val result = api.create(request)
        // Invalidate pin/search caches — new location should appear on next fetch
        PersistentCache.clear()
        return result
    }

    override suspend fun update(id: String, request: UpdateLocationRequest): Location {
        val result = api.update(id, request)
        PersistentCache.remove("loc_id_$id")
        // Searches may have cached this location's old data
        PersistentCache.clear()
        return result
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun lon4(v: Double) = "%.4f".format(v)
    private fun lat4(v: Double) = "%.4f".format(v)
}
