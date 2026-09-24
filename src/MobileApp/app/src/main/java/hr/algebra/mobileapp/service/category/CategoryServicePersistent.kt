package hr.algebra.mobileapp.service.category

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.cache.PersistentCache
import hr.algebra.mobileapp.models.category.Category

/**
 * **Cache-aside** categoryService service — serves data from [PersistentCache] when
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

    override suspend fun getAll(): ServiceResult<List<Category>> {
        val key  = "category_all"
        val type = object : TypeToken<List<Category>>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("CategoryServicePersistent", "cache hit: getAll() → ${it.size}")
            return ServiceResult.success(it)
        }

        val result = api.getAll()
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, PersistentCache.TTL_CATEGORIES)
        }
        return result
    }

    override suspend fun getByTag(tag: String): ServiceResult<Category?> {
        val key  = "category_$tag"
        val type = object : TypeToken<Category>() {}

        PersistentCache.get(key, type)?.let {
            Log.d("CategoryServicePersistent", "cache hit: getByTag($tag)")
            return ServiceResult.success(it)
        }

        val result = api.getByTag(tag)
        // data == null means "tag not found" — valid, do not cache null so we re-check next time
        if (result.isSuccess && result.data != null) {
            PersistentCache.put(key, result.data, PersistentCache.TTL_CATEGORIES)
        }
        return result
    }
}
