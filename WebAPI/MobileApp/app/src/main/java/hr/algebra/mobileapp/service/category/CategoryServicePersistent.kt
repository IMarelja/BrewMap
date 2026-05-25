package hr.algebra.mobileapp.service.category

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.Category

/**
 * **Cache-aside** category service — serves data from [PersistentCache] when
 * available and fresh, falling back to [CategoryServiceApi] on a cache miss.
 *
 * Categories rarely change so they are cached for 24 hours.
 *
 * ## Cache keys & TTLs
 * | Operation    | Key              | TTL   |
 * |--------------|------------------|-------|
 * | [getAll]     | `category_all`   | 24 h  |
 * | [getByTag]   | `category_<tag>` | 24 h  |
 *
 * Cache entries survive across app restarts (SharedPreferences-backed) and are
 * evicted lazily once their TTL expires.
 */
class CategoryServicePersistent : ICategoryService {

    private val api = CategoryServiceApi()

    // ── ICategoryService ──────────────────────────────────────────────────────

    override suspend fun getAll(): List<Category> {
        val key  = "category_all"
        val type = object : TypeToken<List<Category>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("CategoryServicePersistent", "cache hit: getAll() → ${it.size}")
            return it
        }

        val fresh = api.getAll()
        PersistentCache.put(key, fresh, PersistentCache.TTL_CATEGORIES)
        return fresh
    }

    override suspend fun getByTag(tag: String): Category? {
        val key  = "category_$tag"
        val type = object : TypeToken<Category>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("CategoryServicePersistent", "cache hit: getByTag($tag)")
            return it
        }

        val fresh = api.getByTag(tag)
        if (fresh != null) {
            PersistentCache.put(key, fresh, PersistentCache.TTL_CATEGORIES)
        }
        return fresh
    }
}
