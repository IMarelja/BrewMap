package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.Drink

/**
 * **Cache-aside** drink service — serves data from [PersistentCache] when
 * available and fresh, falling back to [DrinkServiceApi] on a cache miss.
 *
 * ## Cache keys & TTLs
 * | Operation              | Key pattern                          | TTL           |
 * |------------------------|--------------------------------------|---------------|
 * | [getById]              | `drink_id_<id>`                      | 15 min        |
 * | [getByLocationId]      | `drink_loc_<locationId>`             | 15 min        |
 * | [getBestDrinkByLocationId] | `drink_best_<locationId>`        | 15 min        |
 *
 * Cache entries survive across app restarts (SharedPreferences-backed) and are
 * evicted lazily once their TTL expires.
 */
class DrinkServicePersistent : IDrinkService {

    private val api = DrinkServiceApi()

    // ── IDrinkService ─────────────────────────────────────────────────────────

    override suspend fun getById(id: String): Drink {
        val key  = "drink_id_$id"
        val type = object : TypeToken<Drink>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getById($id)")
            return it
        }

        val fresh = api.getById(id)
        PersistentCache.put(key, fresh, PersistentCache.TTL_DRINKS)
        return fresh
    }

    override suspend fun getByLocationId(locationId: String): List<Drink> {
        val key  = "drink_loc_$locationId"
        val type = object : TypeToken<List<Drink>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getByLocationId($locationId) → ${it.size}")
            return it
        }

        val fresh = api.getByLocationId(locationId)
        PersistentCache.put(key, fresh, PersistentCache.TTL_DRINKS)
        return fresh
    }

    override suspend fun getBestDrinkByLocationId(locationId: String): BestDrink? {
        val key  = "drink_best_$locationId"
        val type = object : TypeToken<BestDrink>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getBestDrink($locationId)")
            return it
        }

        val fresh = api.getBestDrinkByLocationId(locationId)
        if (fresh != null) {
            PersistentCache.put(key, fresh, PersistentCache.TTL_DRINKS)
        }
        return fresh
    }
}
