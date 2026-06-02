package hr.algebra.mobileapp.service.drink

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.drink.BestDrink
import hr.algebra.mobileapp.models.drink.CreateDrinkRequest
import hr.algebra.mobileapp.models.drink.Drink

/**
 * **Cache-aside** drinkService service.
 *
 * Reads served from [PersistentCache] with a 15-minute TTL.
 * Write operations bypass the cache and invalidate related entries on success.
 */
class DrinkServicePersistent : IDrinkService {

    private val api = DrinkServiceApi()

    // ── Read ──────────────────────────────────────────────────────────────────

    override suspend fun getById(id: String): ServiceResult<Drink> {
        val key  = "drink_id_$id"
        val type = object : TypeToken<Drink>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getById($id)")
            return ServiceResult.success(it)
        }
        val result = api.getById(id)
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, PersistentCache.TTL_DRINKS)
        }
        return result
    }

    override suspend fun getByLocationId(locationId: String): ServiceResult<List<Drink>> {
        val key  = "drink_loc_$locationId"
        val type = object : TypeToken<List<Drink>>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getByLocationId($locationId)")
            return ServiceResult.success(it)
        }
        val result = api.getByLocationId(locationId)
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, PersistentCache.TTL_DRINKS)
        }
        return result
    }

    override suspend fun getBestDrinkByLocationId(locationId: String): ServiceResult<BestDrink?> {
        val key  = "drink_best_$locationId"
        val type = object : TypeToken<BestDrink>() {}
        PersistentCache.get(key, type)?.let {
            Log.d("DrinkServicePersistent", "cache hit: getBestDrink($locationId)")
            return ServiceResult.success(it)
        }
        val result = api.getBestDrinkByLocationId(locationId)
        // data == null means "no rated drinks" — valid, do not cache null so we re-check next time
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, PersistentCache.TTL_DRINKS)
        }
        return result
    }

    // ── Write — bypass cache, invalidate on success ───────────────────────────

    override suspend fun create(request: CreateDrinkRequest): ServiceResult<Drink> {
        val result = api.create(request)
        if (result.isSuccess) {
            PersistentCache.remove("drink_loc_${request.locationId}")
            PersistentCache.remove("drink_best_${request.locationId}")
        }
        return result
    }

    override suspend fun update(id: String, name: String, description: String?): ServiceResult<Drink> {
        val result = api.update(id, name, description)
        if (result.isSuccess && result.data != null) {
            PersistentCache.remove("drink_id_$id")
            PersistentCache.remove("drink_loc_${result.data.availableAtLocationId}")
        }
        return result
    }
}
