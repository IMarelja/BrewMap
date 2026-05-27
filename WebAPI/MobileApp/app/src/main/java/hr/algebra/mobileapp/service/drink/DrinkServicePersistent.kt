package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.BestDrink
import hr.algebra.mobileapp.models.CreateDrinkRequest
import hr.algebra.mobileapp.models.Drink

/**
 * **Cache-aside** drink service.
 *
 * Reads served from [PersistentCache] with a 15-minute TTL.
 * Write operations bypass the cache and invalidate related entries.
 */
class DrinkServicePersistent : IDrinkService {

    private val api = DrinkServiceApi()

    // ── Read ──────────────────────────────────────────────────────────────────

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
            Log.d("DrinkServicePersistent", "cache hit: getByLocationId($locationId)")
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
        if (fresh != null) PersistentCache.put(key, fresh, PersistentCache.TTL_DRINKS)
        return fresh
    }

    // ── Write — bypass cache, invalidate on success ───────────────────────────

    override suspend fun create(request: CreateDrinkRequest): Drink {
        val result = api.create(request)
        PersistentCache.remove("drink_loc_${request.locationId}")
        PersistentCache.remove("drink_best_${request.locationId}")
        return result
    }

    override suspend fun update(id: String, name: String, description: String?): Drink {
        val result = api.update(id, name, description)
        PersistentCache.remove("drink_id_$id")
        PersistentCache.remove("drink_loc_${result.availableAtLocationId}")
        return result
    }
}
