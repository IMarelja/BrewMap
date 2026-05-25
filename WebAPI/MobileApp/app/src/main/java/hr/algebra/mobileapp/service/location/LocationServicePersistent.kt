package hr.algebra.mobileapp.service.location

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.Location
import hr.algebra.mobileapp.models.Pin

/**
 * **Cache-aside** location service — serves data from [PersistentCache] when
 * available and fresh, falling back to [LocationServiceApi] on a cache miss.
 *
 * ## Cache keys & TTLs
 * | Operation    | Key pattern                                           | TTL   |
 * |--------------|-------------------------------------------------------|-------|
 * | [getById]    | `loc_id_<id>`                                         | 5 min |
 * | [search]     | `loc_search_<lon>_<lat>_<radius>_<query?>`            | 5 min |
 * | [getPins]    | `loc_pins_<minLon>_<maxLon>_<minLat>_<maxLat>`        | 5 min |
 *
 * Search results include all active filter parameters in the cache key to avoid
 * serving stale results for different filter combinations.
 *
 * Cache entries survive across app restarts (SharedPreferences-backed) and are
 * evicted lazily once their TTL expires.
 */
class LocationServicePersistent : ILocationService {

    private val api = LocationServiceApi()

    // ── ILocationService ──────────────────────────────────────────────────────

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
        longitude: Double,
        latitude: Double,
        query: String?,
        minRating: Double?,
        drinkQuery: String?,
        categoryTags: List<String>?,
        paymentOptionTags: List<String>?,
        radiusMeters: Double
    ): List<Location> {
        // Build a deterministic cache key from all filter parameters.
        val key = buildString {
            append("loc_search")
            append("_${lon4(longitude)}_${lat4(latitude)}_${radiusMeters.toInt()}")
            query?.let             { append("_q${it.hashCode()}") }
            minRating?.let         { append("_r$it") }
            drinkQuery?.let        { append("_dq${it.hashCode()}") }
            categoryTags?.sorted()?.forEach { append("_c$it") }
            paymentOptionTags?.sorted()?.forEach { append("_p$it") }
        }
        val type = object : TypeToken<List<Location>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("LocationServicePersistent", "cache hit: search → ${it.size}")
            return it
        }

        val fresh = api.search(
            longitude, latitude, query, minRating,
            drinkQuery, categoryTags, paymentOptionTags, radiusMeters
        )
        PersistentCache.put(key, fresh, PersistentCache.TTL_LOCATIONS)
        return fresh
    }

    override suspend fun getPins(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Truncate a coordinate to 4 decimal places for cache-key bucketing (~11 m precision). */
    private fun lon4(v: Double) = "%.4f".format(v)
    private fun lat4(v: Double) = "%.4f".format(v)
}
